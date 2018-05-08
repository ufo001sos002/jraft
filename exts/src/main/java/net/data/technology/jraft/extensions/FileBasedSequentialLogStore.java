/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  The ASF licenses 
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.data.technology.jraft.extensions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.data.technology.jraft.LogEntry;
import net.data.technology.jraft.LogValueType;
import net.data.technology.jraft.SequentialLogStore;
/**
 * 基于文件的顺序数据记录存储
 */
public class FileBasedSequentialLogStore implements SequentialLogStore {
	/**
	 * 数据记录存储索引文件名
	 */
    private static final String LOG_INDEX_FILE = "store.idx";
    /**
     * 数据记录存储数据文件名
     */
    private static final String LOG_STORE_FILE = "store.data";
    /**
     * 数据记录存储开始索引文件名
     */
    private static final String LOG_START_INDEX_FILE = "store.sti";
    /**
     * 数据记录存储索引文件 备份文件名
     */
    private static final String LOG_INDEX_FILE_BAK = "store.idx.bak";
    /**
     * 数据记录存储数据文件 备份文件名
     */
    private static final String LOG_STORE_FILE_BAK = "store.data.bak";
    /**
     * 数据记录存储开始索引备份文件名
     */
    private static final String LOG_START_INDEX_FILE_BAK = "store.sti.bak";
    /**
     * 任期为0 数据为null的数据记录对象
     */
    private static final LogEntry zeroEntry = new LogEntry();
    /**
     * 默认buffer 大小 默认:{@value} 
     */
    private static final int BUFFER_SIZE = 1000;
    /**
     * 原生log对象，需判断 {@link Logger#isDebugEnabled()}/{@link Logger#isInfoEnabled()}
     */
    private Logger logger;
    /**
     * 文件对象 文件名： {@link #LOG_STORE_FILE} <br>
     * 存储数据：每条 数据记录 的数据内容 .<br>
     * 由{@link #indexFile}对应每条日志的长度
     */
    private RandomAccessFile dataFile;
    /**
     * 文件对象 文件名： {@link #LOG_INDEX_FILE} <br>
     * 存储数据：每{@link Long#BYTES} 长度对应 某 数据记录 存储之前 时 {@link #dataFile}的 数据记录 长度:<br>
     * 文件长度 {@link RandomAccessFile#length()} / {@link Long#BYTES} 为 数据记录 数,<br>
     * 每次存储数据之前 都会记录 之前 数据文件 长度,即最后一条 为 倒数第二条的数据记录长度,当前数据文件长度-此值 = 最后一条数据记录长度
     */
    private RandomAccessFile indexFile;
    /**
     * 文件对象 文件名： {@link #LOG_START_INDEX_FILE} <br>
     * 存储数据：数据记录 <b>当前开始</b> 索引 值
     */
    private RandomAccessFile startIndexFile;
    /**
     * 存储的数据记录数
     */
    private long entriesInStore;
    /**
     * 数据记录 <b>当前开始</b>索引 值
     */
    private long startIndex;
    /**
     * 数据记录存储目录
     */
    private Path logContainer;
    /**
     * 存储文件读写锁
     */
    private ReentrantReadWriteLock storeLock;
    /**
     * 存储文件读锁
     */
    private ReadLock storeReadLock;
    /**
     * 存储文件写锁
     */
    private WriteLock storeWriteLock;
    /**
     * 数据记录缓存类
     */
    private LogBuffer buffer;
    /**
     * 当前数据记录buffer大小
     */
    private int bufferSize;

    /**
     * 
     * 根据参数构造 类{@link FileBasedSequentialLogStore} 对象
     * 
     * @param logContainer
     *            数据记录文件目录
     * @param bufferSize
     *            数据记录buffer大小
     */
    public FileBasedSequentialLogStore(Path logContainer, int bufferSize) {
	this.storeLock = new ReentrantReadWriteLock();// 构造存储 读写锁
        this.storeReadLock = this.storeLock.readLock(); // 获取 存储 读锁
        this.storeWriteLock = this.storeLock.writeLock(); // 获取存储 写锁
	this.logContainer = logContainer; // 构架数据记录存储目录
        this.bufferSize = bufferSize;
        this.logger = LogManager.getLogger(getClass());
        try{
	    if (!Files.isDirectory(this.logContainer, LinkOption.NOFOLLOW_LINKS)) {
		Files.createDirectories(this.logContainer);
	    }
            this.indexFile = new RandomAccessFile(this.logContainer.resolve(LOG_INDEX_FILE).toString(), "rw");
            this.dataFile = new RandomAccessFile(this.logContainer.resolve(LOG_STORE_FILE).toString(), "rw");
            this.startIndexFile = new RandomAccessFile(this.logContainer.resolve(LOG_START_INDEX_FILE).toString(), "rw");
            if(this.startIndexFile.length() == 0){// 当前起始索引文件内容为0 则默认为1 并写入文件
                this.startIndex = 1;
                this.startIndexFile.writeLong(this.startIndex);
            }else{// 否则读取 起始索引值
                this.startIndex = this.startIndexFile.readLong();
            }
            // 计算数据记录数 (indexFile 文件中  每一个存储的long值 表示一个日志文件大小， 除以 long值占的byte值 即可得已存储多少数据记录数  )
            this.entriesInStore = this.indexFile.length() / Long.BYTES;
	    // 当前数据记录存储大于 buffer大小，则起始位置为 ? TODO ?不理解
            this.buffer = new LogBuffer(this.entriesInStore > this.bufferSize ? (this.startIndex + (this.entriesInStore  - this.bufferSize)) : this.startIndex, this.bufferSize);
            this.fillBuffer();
            if(logger.isDebugEnabled()) {
            	this.logger.debug(String.format("log store started with entriesInStore=%d, startIndex=%d", this.entriesInStore, this.startIndex));
            }
        }catch(IOException exception){
            this.logger.error("failed to access log store", exception);
        }
    }

    /**
     * 
     * 根据参数构造 类{@link FileBasedSequentialLogStore} 对象
     * @param logContainer 数据记录文件目录
     * @param bufferSize 数据记录buffer大小
     */
    public FileBasedSequentialLogStore(String logContainer, int bufferSize){
	this(Paths.get(logContainer), bufferSize);
    }

    /**
     * 
     * 根据参数构造 类{@link FileBasedSequentialLogStore} 对象<br>
     * 复用 {@link #FileBasedSequentialLogStore(String, int)} 构造方法, int 默认为
     * {@link #BUFFER_SIZE} 的值
     * 
     * @param logContainer
     *            数据记录目录
     */
    public FileBasedSequentialLogStore(Path logContainer) {
	this(logContainer, BUFFER_SIZE);
    }

    /**
     * 
     * 根据参数构造 类{@link FileBasedSequentialLogStore} 对象<br>
     * 复用 {@link #FileBasedSequentialLogStore(String, int)} 构造方法, int 默认为
     * {@link #BUFFER_SIZE} 的值
     * 
     * @param logContainer
     *            数据记录目录
     */
    public FileBasedSequentialLogStore(String logContainer) {
	this(logContainer, BUFFER_SIZE);
    }

    @Override
    public long getFirstAvailableIndex() {
        try{
            this.storeReadLock.lock();
	    return this.entriesInStore + this.startIndex; // TODO ?不理解
        }finally{
            this.storeReadLock.unlock();
        }
    }

    @Override
    public long getStartIndex() {
        try{
            this.storeReadLock.lock();
            return this.startIndex;
        }finally{
            this.storeReadLock.unlock();
        }
    }

    @Override
    public LogEntry getLastLogEntry() {
        LogEntry lastEntry = this.buffer.lastEntry();
        return lastEntry == null ? zeroEntry : lastEntry;
    }

    @Override
    public long append(LogEntry logEntry) {
        try{
            this.storeWriteLock.lock();
            this.indexFile.seek(this.indexFile.length());// 设置当前写入位置 为当前文件长度(末尾) 
            long dataFileLength = this.dataFile.length();
            this.indexFile.writeLong(dataFileLength); // 写入当前数据文件长度
            this.dataFile.seek(dataFileLength);// 设置 数据文件 当前写入位置 为文件长度(末尾)
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + 1 + logEntry.getValue().length);
            buffer.putLong(logEntry.getTerm());// 任期
            buffer.put(logEntry.getValueType().toByte()); // 数据记录类型
            buffer.put(logEntry.getValue()); //数据记录实际内容
            this.dataFile.write(buffer.array());// 将数据记录写入文件
            this.entriesInStore += 1;// 数据记录数+1
            this.buffer.append(logEntry);// 追加数据记录
            return this.entriesInStore + this.startIndex - 1;// 数据记录所在索引
        }catch(IOException exception){
            this.logger.error("failed to append a log entry to store", exception);
            throw new RuntimeException(exception.getMessage(), exception);
        }finally{
            this.storeWriteLock.unlock();
        }
    }

    /**
     * write the log entry at the specific index, all log entries after index will be discarded
     * @param logIndex must be &gt;= this.getStartIndex()
     * @param logEntry the log entry to write
     */
    @Override
    public void writeAt(long logIndex, LogEntry logEntry) {
        this.throwWhenNotInRange(logIndex);

        try{
            this.storeWriteLock.lock();
            long index = logIndex - this.startIndex + 1; //start index is one based
            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES + 1 + logEntry.getValue().length);
            buffer.putLong(logEntry.getTerm());
            buffer.put(logEntry.getValueType().toByte());
            buffer.put(logEntry.getValue());

            // find the positions for index and data files
            long dataPosition = this.dataFile.length();
            long indexPosition = (index - 1) * Long.BYTES;
            if(indexPosition < this.indexFile.length()){
                this.indexFile.seek(indexPosition);
                dataPosition = this.indexFile.readLong();
            }

            // write the data at the specified position
            this.indexFile.seek(indexPosition);
            this.dataFile.seek(dataPosition);
            this.indexFile.writeLong(dataPosition);
            this.dataFile.write(buffer.array());

            // trim the files if necessary
            if(this.indexFile.length() > this.indexFile.getFilePointer()){
                this.indexFile.setLength(this.indexFile.getFilePointer());
            }

            if(this.dataFile.length() > this.dataFile.getFilePointer()){
                this.dataFile.setLength(this.dataFile.getFilePointer());
            }

            if(index <= this.entriesInStore){
                this.buffer.trim(logIndex);
            }
            
            this.buffer.append(logEntry);
            this.entriesInStore = index;
        }catch(IOException exception){
            this.logger.error("failed to write a log entry at a specific index to store", exception);
            throw new RuntimeException(exception.getMessage(), exception);
        }finally{
            this.storeWriteLock.unlock();
        }
    }

    @Override
    public LogEntry[] getLogEntries(long startIndex, long endIndex) {
        this.throwWhenNotInRange(startIndex);

        // start and adjustedEnd are zero based, targetEndIndex is this.startIndex based
        long start, adjustedEnd, targetEndIndex;
        try{
            this.storeReadLock.lock();
            start = startIndex - this.startIndex;
            adjustedEnd = endIndex - this.startIndex;
            adjustedEnd = adjustedEnd > this.entriesInStore ? this.entriesInStore : adjustedEnd;
            targetEndIndex = endIndex > this.entriesInStore + this.startIndex + 1 ? this.entriesInStore + this.startIndex + 1 : endIndex;
        }finally{
            this.storeReadLock.unlock();
        }
        
        try{
            LogEntry[] entries = new LogEntry[(int)(adjustedEnd - start)];
            if(entries.length == 0){
                return entries;
            }

            // fill with buffer
            long bufferFirstIndex = this.buffer.fill(startIndex, targetEndIndex, entries);
            
            // Assumption: buffer.lastIndex() == this.entriesInStore + this.startIndex
            // (Yes, for sure, we need to enforce this assumption to be true)
            if(startIndex < bufferFirstIndex){
                // in this case, we need to read from store file
                try{
                    // we need to move the file pointer
                    this.storeWriteLock.lock();
                    long end = bufferFirstIndex - this.startIndex;
                    this.indexFile.seek(start * Long.BYTES);
                    long dataStart = this.indexFile.readLong();
                    for(int i = 0; i < (int)(end - start); ++i){
                        long dataEnd = this.indexFile.readLong();
                        int dataSize = (int)(dataEnd - dataStart);
                        byte[] logData = new byte[dataSize];
                        this.dataFile.seek(dataStart);
                        this.read(this.dataFile, logData);
                        entries[i] = new LogEntry(BinaryUtils.bytesToLong(logData, 0), Arrays.copyOfRange(logData, Long.BYTES + 1, logData.length), LogValueType.fromByte(logData[Long.BYTES]));
                        dataStart = dataEnd;
                    }
                }finally{
                    this.storeWriteLock.unlock();
                }
            }

            return entries;
        }catch(IOException exception){
            this.logger.error("failed to read entries from store", exception);
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    @Override
    public LogEntry getLogEntryAt(long logIndex) {
        this.throwWhenNotInRange(logIndex);

        long index = 0;
        try{
            this.storeReadLock.lock();
            index = logIndex - this.startIndex + 1;
            if(index > this.entriesInStore){
                return null;
            }
        }finally{
            this.storeReadLock.unlock();
        }

        LogEntry entry = this.buffer.entryAt(logIndex);
        if(entry != null){
            return entry;
        }
        
        try{
            this.storeWriteLock.lock();
            long indexPosition = (index - 1) * Long.BYTES;
            this.indexFile.seek(indexPosition);
            long dataPosition = this.indexFile.readLong();
            long endDataPosition = this.indexFile.readLong();
            this.dataFile.seek(dataPosition);
            byte[] logData = new byte[(int)(endDataPosition - dataPosition)];
            this.read(this.dataFile, logData);
            return new LogEntry(BinaryUtils.bytesToLong(logData, 0), Arrays.copyOfRange(logData, Long.BYTES + 1, logData.length), LogValueType.fromByte(logData[Long.BYTES]));
        }catch(IOException exception){
            this.logger.error("failed to read files to get the specified entry");
            throw new RuntimeException(exception.getMessage(), exception);
        }finally{
            this.storeWriteLock.unlock();
        }
    }

    @Override
    public byte[] packLog(long logIndex, int itemsToPack){
        this.throwWhenNotInRange(logIndex);
        
        try{
            this.storeWriteLock.lock();
            long index = logIndex - this.startIndex + 1;
            if(index > this.entriesInStore){
                return new byte[0];
            }

            long endIndex = Math.min(index + itemsToPack, this.entriesInStore + 1);
            boolean readToEnd = (endIndex == this.entriesInStore + 1);
            long indexPosition = (index - 1) * Long.BYTES;
            this.indexFile.seek(indexPosition);
            byte[] indexBuffer = new byte[(int)(Long.BYTES * (endIndex - index))];
            this.read(this.indexFile, indexBuffer);
            long endOfLog = this.dataFile.length();
            if(!readToEnd){
                endOfLog = this.indexFile.readLong();
            }

            long startOfLog = BinaryUtils.bytesToLong(indexBuffer, 0);
            byte[] logBuffer = new byte[(int)(endOfLog - startOfLog)];
            this.dataFile.seek(startOfLog);
            this.read(this.dataFile, logBuffer);
            ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
            GZIPOutputStream gzipStream = new GZIPOutputStream(memoryStream);
            gzipStream.write(BinaryUtils.intToBytes(indexBuffer.length));
            gzipStream.write(BinaryUtils.intToBytes(logBuffer.length));
            gzipStream.write(indexBuffer);
            gzipStream.write(logBuffer);
            gzipStream.flush();
            memoryStream.flush();
            gzipStream.close();
            return memoryStream.toByteArray();
        }catch(IOException exception){
            this.logger.error("failed to read files to read data for packing");
            throw new RuntimeException(exception.getMessage(), exception);
        }finally{
            this.storeWriteLock.unlock();
        }
    }

    @Override
    public void applyLogPack(long logIndex, byte[] logPack){
        this.throwWhenNotInRange(logIndex);

        try{
            this.storeWriteLock.lock();
            long index = logIndex - this.startIndex + 1;
            ByteArrayInputStream memoryStream = new ByteArrayInputStream(logPack);
            GZIPInputStream gzipStream = new GZIPInputStream(memoryStream);
            byte[] sizeBuffer = new byte[Integer.BYTES];
            this.read(gzipStream, sizeBuffer);
            int indexDataSize = BinaryUtils.bytesToInt(sizeBuffer, 0);
            this.read(gzipStream, sizeBuffer);
            int logDataSize = BinaryUtils.bytesToInt(sizeBuffer, 0);
            byte[] indexBuffer = new byte[indexDataSize];
            this.read(gzipStream, indexBuffer);
            byte[] logBuffer = new byte[logDataSize];
            this.read(gzipStream, logBuffer);
            long indexFilePosition, dataFilePosition;
            if(index == this.entriesInStore + 1){
                indexFilePosition = this.indexFile.length();
                dataFilePosition = this.dataFile.length();
            }else{
                indexFilePosition = (index - 1) * Long.BYTES;
                this.indexFile.seek(indexFilePosition);
                dataFilePosition = this.indexFile.readLong();
            }

            this.indexFile.seek(indexFilePosition);
            this.indexFile.write(indexBuffer);
            this.indexFile.setLength(this.indexFile.getFilePointer());
            this.dataFile.seek(dataFilePosition);
            this.dataFile.write(logBuffer);
            this.dataFile.setLength(this.dataFile.getFilePointer());
            this.entriesInStore = index - 1 + indexBuffer.length / Long.BYTES;
            gzipStream.close();
            this.buffer.reset(this.entriesInStore > this.bufferSize ? this.entriesInStore + this.startIndex - this.bufferSize : this.startIndex);
            this.fillBuffer();
        }catch(IOException exception){
            this.logger.error("failed to write files to unpack logs for data");
            throw new RuntimeException(exception.getMessage(), exception);
        }finally{
            this.storeWriteLock.unlock();
        }
    }

    @Override
    public boolean compact(long lastLogIndex){
        this.throwWhenNotInRange(lastLogIndex);

        try{
            this.storeWriteLock.lock();
            this.backup();
            long lastIndex = lastLogIndex - this.startIndex;
            if(lastLogIndex >= this.getFirstAvailableIndex() - 1){
                this.indexFile.setLength(0);
                this.dataFile.setLength(0);
                this.startIndexFile.seek(0);
                this.startIndexFile.writeLong(lastLogIndex + 1);
                this.startIndex = lastLogIndex + 1;
                this.entriesInStore = 0;
                this.buffer.reset(lastLogIndex + 1);
                return true;
            }else{
                long dataPosition = -1;
                long indexPosition = Long.BYTES * (lastIndex + 1);
                byte[] dataPositionBuffer = new byte[Long.BYTES];
                this.indexFile.seek(indexPosition);
                this.read(this.indexFile, dataPositionBuffer);
                dataPosition = ByteBuffer.wrap(dataPositionBuffer).getLong();
                long indexFileNewLength = this.indexFile.length() - indexPosition;
                long dataFileNewLength = this.dataFile.length() - dataPosition;
    
                // copy the log data
                RandomAccessFile backupFile = new RandomAccessFile(this.logContainer.resolve(LOG_STORE_FILE_BAK).toString(), "r");
                FileChannel backupChannel = backupFile.getChannel();
                backupChannel.position(dataPosition);
                FileChannel channel = this.dataFile.getChannel();
                channel.transferFrom(backupChannel, 0, dataFileNewLength);
                this.dataFile.setLength(dataFileNewLength);
                backupFile.close();
    
                // copy the index data
                backupFile = new RandomAccessFile(this.logContainer.resolve(LOG_INDEX_FILE_BAK).toString(), "r");
                backupFile.seek(indexPosition);
                this.indexFile.seek(0);
                for(int i = 0; i < indexFileNewLength / Long.BYTES; ++i){
                    this.indexFile.writeLong(backupFile.readLong() - dataPosition);
                }
    
                this.indexFile.setLength(indexFileNewLength);
                backupFile.close();
    
                // save the starting index
                this.startIndexFile.seek(0);
                this.startIndexFile.write(ByteBuffer.allocate(Long.BYTES).putLong(lastLogIndex + 1).array());
                this.entriesInStore -= (lastLogIndex - this.startIndex + 1);
                this.startIndex = lastLogIndex + 1;
                this.buffer.reset(this.entriesInStore > this.bufferSize ? this.entriesInStore + this.startIndex - this.bufferSize : this.startIndex);
                this.fillBuffer();
                return true;
            }
        }catch(Throwable error){
            this.logger.error("fail to compact the logs due to error", error);
            this.restore();
            return false;
        }finally{
            this.storeWriteLock.unlock();
        }
    }

    public void close(){
        try{
            this.storeWriteLock.lock();
            this.dataFile.close();
            this.indexFile.close();
            this.startIndexFile.close();
        }catch(IOException exception){
            this.logger.error("failed to close data/index file(s)", exception);
        }finally{
            this.storeWriteLock.unlock();
        }
    }
    
    private void throwWhenNotInRange(long index){
        try{
            this.storeReadLock.lock();
            if(index < this.startIndex){
                throw new IllegalArgumentException("logIndex out of range");
            }
        }finally{
            this.storeReadLock.unlock();
        }
    }

    private void restore(){
        try{
            this.indexFile.close();
            this.dataFile.close();
            this.startIndexFile.close();
            Files.copy(this.logContainer.resolve(LOG_INDEX_FILE_BAK), this.logContainer.resolve(LOG_INDEX_FILE), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(this.logContainer.resolve(LOG_STORE_FILE_BAK), this.logContainer.resolve(LOG_STORE_FILE), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(this.logContainer.resolve(LOG_START_INDEX_FILE_BAK), this.logContainer.resolve(LOG_START_INDEX_FILE), StandardCopyOption.REPLACE_EXISTING);
            this.indexFile = new RandomAccessFile(this.logContainer.resolve(LOG_INDEX_FILE).toString(), "rw");
            this.dataFile = new RandomAccessFile(this.logContainer.resolve(LOG_STORE_FILE).toString(), "rw");
            this.startIndexFile = new RandomAccessFile(this.logContainer.resolve(LOG_START_INDEX_FILE).toString(), "rw");
        }catch(Exception error){
            // this is fatal...
            this.logger.fatal("cannot restore from failure, please manually restore the log files");
            System.exit(-1);
        }
    }

    private void backup(){
        try {
            Files.deleteIfExists(this.logContainer.resolve(LOG_INDEX_FILE_BAK));
            Files.deleteIfExists(this.logContainer.resolve(LOG_STORE_FILE_BAK));
            Files.deleteIfExists(this.logContainer.resolve(LOG_START_INDEX_FILE_BAK));
            Files.copy(this.logContainer.resolve(LOG_INDEX_FILE), this.logContainer.resolve(LOG_INDEX_FILE_BAK), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(this.logContainer.resolve(LOG_STORE_FILE), this.logContainer.resolve(LOG_STORE_FILE_BAK), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(this.logContainer.resolve(LOG_START_INDEX_FILE), this.logContainer.resolve(LOG_START_INDEX_FILE_BAK), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            this.logger.error("failed to create a backup folder", e);
            throw new RuntimeException("failed to create a backup folder");
        }
    }

    private void read(InputStream stream, byte[] buffer){
        try{
            int offset = 0;
            int bytesRead = 0;
            while(offset < buffer.length && (bytesRead = stream.read(buffer, offset, buffer.length - offset)) != -1){
                offset += bytesRead;
            }

            if(offset < buffer.length){
                this.logger.error(String.format("only %d bytes are read while %d bytes are desired, bad file", offset, buffer.length));
                throw new RuntimeException("bad file, insufficient file data for reading");
            }
        }catch(IOException exception){
            this.logger.error("failed to read and fill the buffer", exception);
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }
    
    /**
     * 从文件中当前位置起读满 buffer 
     * @param stream 某位置处 文件访问对象
     * @param buffer 需要被写满的buffer 未被写满则报错
     */
    private void read(RandomAccessFile stream, byte[] buffer){
        try{
            int offset = 0;
            int bytesRead = 0;
            while(offset < buffer.length && (bytesRead = stream.read(buffer, offset, buffer.length - offset)) != -1){
                offset += bytesRead;
            }

            if(offset < buffer.length){// 为读满buffer则报错
                this.logger.error(String.format("only %d bytes are read while %d bytes are desired, bad file", offset, buffer.length));
                throw new RuntimeException("bad file, insufficient file data for reading");
            }
        }catch(IOException exception){
            this.logger.error("failed to read and fill the buffer", exception);
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }
    /**
     * 填充buffer
     * @throws IOException
     */
    private void fillBuffer() throws IOException{
        long startIndex = this.buffer.firstIndex();// 获取buffer的当前起始位置
        long indexFileSize = this.indexFile.length();// 数据记录索引文件长度
        if(indexFileSize > 0){
            long indexPosition = (startIndex - this.startIndex) * Long.BYTES;
            this.indexFile.seek(indexPosition);
            byte[] indexData = new byte[(int)(indexFileSize - indexPosition)];
            this.read(this.indexFile, indexData);
            ByteBuffer indexBuffer = ByteBuffer.wrap(indexData);
            long dataStart = indexBuffer.getLong();
            this.dataFile.seek(dataStart);
            while(indexBuffer.hasRemaining()){
                long dataEnd = indexBuffer.getLong();
                this.buffer.append(this.readEntry((int)(dataEnd - dataStart)));
                dataStart = dataEnd;
            }
            
            // a little ugly, load last entry into buffer
            long dataEnd = this.dataFile.length();
            this.buffer.append(this.readEntry((int)(dataEnd - dataStart)));
        }
    }
    
    private LogEntry readEntry(int size){
        byte[] entryData = new byte[size];
        this.read(this.dataFile, entryData);
        ByteBuffer entryBuffer = ByteBuffer.wrap(entryData);
        long term = entryBuffer.getLong();
        byte valueType = entryBuffer.get();
        return new LogEntry(term, Arrays.copyOfRange(entryData, Long.BYTES + 1, entryData.length), LogValueType.fromByte(valueType));
    }
    
    /**
     * 数据记录缓存类
     */
    public static class LogBuffer{
    	/**
    	 * 数据记录对象 集合
    	 */
        private List<LogEntry> buffer;
        /**
         * 读写锁 对象
         */
        private ReentrantReadWriteLock bufferLock;
        /**
         * 读锁 对象
         */
        private ReadLock bufferReadLock;
        /**
         * 写锁 对象
         */
        private WriteLock bufferWriteLock;
        /**
         * 起始索引位置
         */
        private long startIndex;
        /**
         * 最大大小
         */
        private int maxSize;
        
        /**
         * 
         * 根据参数构造 类{@link #startIndex} {@link #maxSize}对象
         * @param startIndex
         * @param maxSize
         */
        public LogBuffer(long startIndex, int maxSize){
            this.startIndex = startIndex;
            this.maxSize = maxSize;
            this.bufferLock = new ReentrantReadWriteLock();
            this.bufferReadLock = this.bufferLock.readLock();
            this.bufferWriteLock = this.bufferLock.writeLock();
            this.buffer = new ArrayList<LogEntry>();
        }
        /**
         * 
         * @return 最后索引的位置 {@link #startIndex} + {@link #buffer}的size
         */
        public long lastIndex(){
            try{
                this.bufferReadLock.lock();
                return this.startIndex + this.buffer.size();
            }finally{
                this.bufferReadLock.unlock();
            }
        }
        /**
         * 
         * @return 当前起始位置 {@link #startIndex} 
         */
        public long firstIndex(){
            try{
                this.bufferReadLock.lock();
                return this.startIndex;
            }finally{
                this.bufferReadLock.unlock();
            }
        }
        /**
         * 
         * @return 最后数据记录 无则返回null
         */
        public LogEntry lastEntry(){
            try{
                this.bufferReadLock.lock();
                if(this.buffer.size() > 0){
                    return this.buffer.get(this.buffer.size() - 1);
                }
                
                return null;
            }finally{
                this.bufferReadLock.unlock();
            }
        }
        /**
         * 返回索引位置的数据记录
         * @param index
         * @return 对应位置无则返回null
         */
        public LogEntry entryAt(long index){
            try{
                this.bufferReadLock.lock();
                int indexWithinBuffer = (int)(index - this.startIndex);
                if(indexWithinBuffer >= this.buffer.size() || indexWithinBuffer < 0){
                    return null;
                }
                
                return this.buffer.get(indexWithinBuffer);
            }finally{
                this.bufferReadLock.unlock();
            }
        }
        
        // [start, end), returns the startIndex
        /**
         * 从起始结束位置起 填充 result数组, 仅填充有效部分
         * @param start
         * @param end
         * @param result
         * @return 起始位置 {@link #startIndex}
         */
        public long fill(long start, long end, LogEntry[] result){
            try{
                this.bufferReadLock.lock();
                if(result == null) {
                	return this.startIndex; 
                }
                if(end < this.startIndex){
                    return this.startIndex;
                }
                
                int offset = (int)(start - this.startIndex);
                if(offset > 0){
                    for(int i = 0; i < (int)(end - start) && offset < buffer.size() && i < result.length; ++i, ++offset){
                        result[i] = this.buffer.get(offset);
                    }
                }else{
                    offset *= -1;
                    for(int i = 0; i < (int)(end - this.startIndex) && i < buffer.size() && offset < result.length; ++i, ++offset){
                        result[offset] = this.buffer.get(i);
                    }
                }
                
                return this.startIndex;
            }finally{
                this.bufferReadLock.unlock();
            }
        }
        
        // trimming the buffer [fromIndex, end)
        /**
         * 清空从起始位置的 数据记录 
         * @param fromIndex
         */
        public void trim(long fromIndex){
            try{
                this.bufferWriteLock.lock();
                int index = (int)(fromIndex - this.startIndex);
                if(index < this.buffer.size()){
                    this.buffer.subList(index, this.buffer.size()).clear();
                }
            }finally{
                this.bufferWriteLock.unlock();
            }
        }
        
        // trimming the buffer [fromIndex, endIndex)
        /**
         * 清除起始位置 至结束 位置的日志 如果起始位置 等于 {@link #startIndex} 则将重置{@link #startIndex} 为结束位置
         * @param fromIndex
         * @param endIndex
         */
        public void trim(long fromIndex, long endIndex){
            try{
                this.bufferWriteLock.lock();
                int index = (int)(fromIndex - this.startIndex);
                int end = (int)(endIndex - this.startIndex);
                if(index < this.buffer.size()){
                    this.buffer.subList(index, end).clear();
                }
                
                // we may need to reset the start index
                if(fromIndex == this.startIndex){
                    this.startIndex = endIndex > this.startIndex + end ? this.startIndex + end : endIndex;
                }
            }finally{
                this.bufferWriteLock.unlock();
            }
        }
        
        /**
         * 追加数据记录，并保证不超过{@link #maxSize}大小
         * @param entry
         */
        public void append(LogEntry entry){
            try{
                this.bufferWriteLock.lock();
                this.buffer.add(entry);
                if(this.maxSize < this.buffer.size()){
                    this.buffer.remove(0);// 移除顶部
                    this.startIndex += 1; // 起始索引+1
                }
            }finally{
                this.bufferWriteLock.unlock();
            }
        }
        /**
         * 重置起始位置，并清除{@link #buffer} 内容
         * @param startIndex
         */
        public void reset(long startIndex){
            try{
                this.bufferWriteLock.lock();
                this.buffer.clear();
                this.startIndex = startIndex;
            }finally{
                this.bufferWriteLock.unlock();
            }
        }

        /**
         * 
         * @return
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "LogBuffer [startIndex=" + startIndex + ", maxSize=" + maxSize + ", bufferLock="
                    + bufferLock + ", bufferReadLock=" + bufferReadLock + ", bufferWriteLock="
                    + bufferWriteLock + ", buffer=" + buffer.size() + "]";
        }


    }

    /**
     * 
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FileBasedSequentialLogStore [indexFile=" + indexFile + ", dataFile=" + dataFile
                + ", startIndexFile=" + startIndexFile + ", entriesInStore=" + entriesInStore
                + ", startIndex=" + startIndex + ", logContainer=" + logContainer + ", storeLock="
                + storeLock + ", storeReadLock=" + storeReadLock + ", storeWriteLock="
                + storeWriteLock + ", buffer=" + buffer + ", bufferSize=" + bufferSize + "]";
    }


}
