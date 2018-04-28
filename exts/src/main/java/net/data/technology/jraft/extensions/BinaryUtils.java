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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;

import net.data.technology.jraft.LogEntry;
import net.data.technology.jraft.LogValueType;
import net.data.technology.jraft.RaftMessageType;
import net.data.technology.jraft.RaftRequestMessage;
import net.data.technology.jraft.RaftResponseMessage;
/**
 * 二进制转换工具类
 */
public class BinaryUtils {
	/**
	 * Raft 回包固定 头 大小(当前为整个包大小)
	 */
    public static final int RAFT_RESPONSE_HEADER_SIZE = Integer.BYTES * 2 + Long.BYTES * 2 + 2;
    /**
     * Raft 请求包 头固定 大小(当前只为固定包头大小  不包含日志体具体内容)
     */
    public static final int RAFT_REQUEST_HEADER_SIZE = Integer.BYTES * 3 + Long.BYTES * 4 + 1;
    /**
     * 将参数转为{@link Long#BYTES} 8 个字节长度字节数组
     * @param value
     * @return
     */
    public static byte[] longToBytes(long value){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(value);
        return buffer.array();
    }
    /**
     * 从数组 偏移位置起(包含) 取{@link Long#BYTES} 8 个字节 转为long值 
     * @param bytes
     * @param offset 偏移位置起(包含)
     * @return
     */
    public static long bytesToLong(byte[] bytes, int offset){
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, Long.BYTES);
        return buffer.getLong();
    }
    /**
     * 将参数转为{@link Integer#BYTES} 4 个字节长度字节数组
     * @param value
     * @return
     */
    public static byte[] intToBytes(int value){
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(value);
        return buffer.array();
    }
    /**
     * 从数组 偏移位置起(包含) 取{@link Integer#BYTES} 4 个字节 转为int值 
     * @param bytes
     * @param offset 偏移位置起(包含)
     * @return
     */
    public static int bytesToInt(byte[] bytes, int offset){
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, Integer.BYTES);
        return buffer.getInt();
    }
    /**
     * 将boolean值转为1字节
     * @param value
     * @return
     */
    public static byte booleanToByte(boolean value){
        return value ? (byte)1 : (byte)0;
    }
    /**
     * 将一个字节值转为boolean值
     * @param value
     * @return
     */
    public static boolean byteToBoolean(byte value){
        return value != 0;
    }
    
    /**
     * 将参数转为{@link Long#BYTES} 8 个字节长度字节数组
     * 
     * @param value
     * @return
     */
    public static byte[] stringToBytes(String value) {
	return value.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 冲
     * 
     * @param buffer
     * @return
     */
    public static String bufferGetString(ByteBuffer buffer) {
	byte[] data = new byte[buffer.getInt()];
	buffer.get(data);
	return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * 将Raft回包对象写入字节数组中
     * 
     * @param response
     * @return
     */
    public static byte[] messageToBytes(RaftResponseMessage response){
	byte[] source = stringToBytes(response.getSource());
	byte[] destination = stringToBytes(response.getDestination());
	ByteBuffer buffer = ByteBuffer.allocate(RAFT_RESPONSE_HEADER_SIZE + source.length + destination.length);
        buffer.put(response.getMessageType().toByte()); // 1
	buffer.put(intToBytes(source.length)); // 4
	buffer.put(source);
	buffer.put(intToBytes(destination.length)); // 4
	buffer.put(destination);
        buffer.put(longToBytes(response.getTerm())); // 8
        buffer.put(longToBytes(response.getNextIndex())); // 8
        buffer.put(booleanToByte(response.isAccepted())); // 1
        return buffer.array();
    }
    /**
     * 将字节数组转换为 Raft回包对象
     * @param data
     * @return
     */
    public static RaftResponseMessage bytesToResponseMessage(byte[] data){
	if (data == null || data.length <= RAFT_RESPONSE_HEADER_SIZE) {
	    throw new IllegalArgumentException(
		    String.format("data must have %d bytes for a raft response message", RAFT_RESPONSE_HEADER_SIZE));
	}
        ByteBuffer buffer = ByteBuffer.wrap(data);
        RaftResponseMessage response = new RaftResponseMessage();
        response.setMessageType(RaftMessageType.fromByte(buffer.get()));
	response.setSource(bufferGetString(buffer));
	response.setDestination(bufferGetString(buffer));
        response.setTerm(buffer.getLong());
        response.setNextIndex(buffer.getLong());
        response.setAccepted(buffer.get() == 1);
        return response;
    }
    
    /**
     * 将请求日志对象转成byte字节进行传输
     * @param request
     * @return
     */
    public static byte[] messageToBytes(RaftRequestMessage request){
        LogEntry[] logEntries = request.getLogEntries();
        int logSize = 0;
        List<byte[]> buffersForLogs = null;
        if(logEntries != null && logEntries.length > 0){
            buffersForLogs = new ArrayList<byte[]>(logEntries.length);
            for(LogEntry logEntry : logEntries){
                byte[] logData = logEntryToBytes(logEntry);
                logSize += logData.length;
                buffersForLogs.add(logData);
            }
        }
	byte[] source = stringToBytes(request.getSource());
	byte[] destination = stringToBytes(request.getDestination());
	ByteBuffer requestBuffer = ByteBuffer
		.allocate(RAFT_REQUEST_HEADER_SIZE + source.length + destination.length + logSize);
        requestBuffer.put(request.getMessageType().toByte());
	requestBuffer.put(intToBytes(source.length)); // 4
	requestBuffer.put(source);
	requestBuffer.put(intToBytes(destination.length)); // 4
	requestBuffer.put(destination);
        requestBuffer.put(longToBytes(request.getTerm()));
        requestBuffer.put(longToBytes(request.getLastLogTerm()));
        requestBuffer.put(longToBytes(request.getLastLogIndex()));
        requestBuffer.put(longToBytes(request.getCommitIndex()));
        requestBuffer.put(intToBytes(logSize));
        if(buffersForLogs != null){
            for(byte[] logData : buffersForLogs){
                requestBuffer.put(logData);
            }
        }

        return requestBuffer.array();
    }
    
    /**
     * 根据Raft消息请求 头字节数组 将内容生成 {@link Pair} 对象
     * <br>将除日志数据外 其他头内容写入 {@link RaftRequestMessage}中即({@link Pair#first}), 后续日志长度 写入 Integer中即({@link Pair#second})
     * @param data Raft消息头内容
     * @return
     * @throws IllegalArgumentException data为null 或 长度不满足 {@link #RAFT_REQUEST_HEADER_SIZE} (Raft消息请求头长度)
     */
    public static Pair<RaftRequestMessage, Integer> bytesToRequestMessage(byte[] data){
	if (data == null || data.length <= RAFT_REQUEST_HEADER_SIZE) {
            throw new IllegalArgumentException("invalid request message header.");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        RaftRequestMessage request = new RaftRequestMessage();
        request.setMessageType(RaftMessageType.fromByte(buffer.get()));
	request.setSource(bufferGetString(buffer));
	request.setDestination(bufferGetString(buffer));
        request.setTerm(buffer.getLong());
        request.setLastLogTerm(buffer.getLong());
        request.setLastLogIndex(buffer.getLong());
        request.setCommitIndex(buffer.getLong());
        int logDataSize = buffer.getInt();
        return new Pair<RaftRequestMessage, Integer>(request, logDataSize);
    }
    
    /**
     * 日志对象转为 字节数组
     * @param logEntry
     * @return
     */
    public static byte[] logEntryToBytes(LogEntry logEntry){
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try{
            output.write(longToBytes(logEntry.getTerm()));
            output.write(logEntry.getValueType().toByte());
            output.write(intToBytes(logEntry.getValue().length));
            output.write(logEntry.getValue());
            output.flush();
            return output.toByteArray();
        }catch(IOException exception){
            LogManager.getLogger("BinaryUtil").error("failed to serialize LogEntry to memory", exception);
            throw new RuntimeException("Running into bad situation, where memory may not be sufficient", exception);
        }
    }
    /**
     *  字节数组转为 日志内容 对象数组
     * @param data
     * @return
     * @throws IllegalArgumentException data为null 或长度 小于 最基本  term({@link Long#BYTES}) + valueType({@link Byte#BYTES}) + valueSize({@link Integer#BYTES}) 字段长度时
     */
    public static LogEntry[] bytesToLogEntries(byte[] data){
        if(data == null || data.length < Long.BYTES + Integer.BYTES + Byte.BYTES){// 
            throw new IllegalArgumentException("invalid log entries data");
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        List<LogEntry> logEntries = new ArrayList<LogEntry>();
        while(buffer.hasRemaining()){
            long term = buffer.getLong(); // 任期
            byte valueType = buffer.get(); // 日志类型
            int valueSize = buffer.getInt(); // 日志内容大小
            byte[] value = new byte[valueSize];
            if(valueSize > 0){
                buffer.get(value);
            }
            logEntries.add(new LogEntry(term, value, LogValueType.fromByte(valueType)));
        }

        return logEntries.toArray(new LogEntry[0]);
    }
}
