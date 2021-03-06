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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.data.technology.jraft.ClusterConfiguration;
import net.data.technology.jraft.SequentialLogStore;
import net.data.technology.jraft.ServerState;
import net.data.technology.jraft.ServerStateManager;

/**
 * 基于文件服务器状态管理
 */
public class FileBasedServerStateManager implements ServerStateManager {
    /**
     * 日志对象
     */
    private static final Logger logger = LoggerFactory.getLogger(FileBasedServerStateManager.class);
    /**
     * 状态文件名常量
     */
    private static final String STATE_FILE = "server.state";
    /**
     * 配置文件名常量
     */
    private static final String CONFIG_FILE = "config.properties";
    /**
     * 集群配置文件名常量
     */
    private static final String CLUSTER_CONFIG_FILE = "cluster.json";
    /**
     * 服务器状态文件访问对象， 访问文件 {@link #STATE_FILE}
     */
    private RandomAccessFile serverStateFile;
    /**
     * 基于文件的顺序数据记录存储对象
     */
    private FileBasedSequentialLogStore logStore;
    /**
     * 数据目录对象
     */
    private Path container;
    /**
     * 服务端ID 从{@link #CONFIG_FILE} 获取
     */
    private String serverId;

    /**
     * 
     * 根据参数构造 类{@link FileBasedServerStateManager} 对象
     * 
     * @param dataDirectory
     *            文件目录
     */
    public FileBasedServerStateManager(Path dataDirectory) {
	this(dataDirectory, null);
    }
    
    /**
     * 
     * 根据参数构造 类{@link FileBasedServerStateManager} 对象
     * 
     * @param dataDirectory
     *            文件目录
     * @param serverId
     *            当前集群节点ID
     */
    public FileBasedServerStateManager(Path dataDirectory, String serverId) {
        this.logStore = new FileBasedSequentialLogStore(dataDirectory);// 构造数据记录 存储对象
	this.container = dataDirectory;
        try{
	    if (serverId == null) {
		Properties props = new Properties();
		FileInputStream configInput = new FileInputStream(this.container.resolve(CONFIG_FILE).toString());
		props.load(configInput);
		String serverIdValue = props.getProperty("server.id");
		this.serverId = serverIdValue == null || serverIdValue.length() == 0 ? "-1" : serverIdValue.trim(); // 读取当前集群服务端程序ID
		configInput.close();
	    } else {
		this.serverId = serverId;
	    }
            this.serverStateFile = new RandomAccessFile(this.container.resolve(STATE_FILE).toString(), "rw");
	    this.serverStateFile.seek(0);// 打开并定位 服务器状态文件 写入位置
        }catch(IOException exception){
            logger.error("failed to create/open server state file", exception);
            throw new IllegalArgumentException("cannot create/open the state file", exception);
        }
    }

    /**
     * 
     * 根据参数构造 类{@link FileBasedServerStateManager} 对象
     * 
     * @param dataDirectory
     *            文件目录
     */
    public FileBasedServerStateManager(String dataDirectory) {
	this(Paths.get(dataDirectory));
    }

    @Override
    public ClusterConfiguration loadClusterConfiguration(){
        Gson gson = new GsonBuilder().create();
        FileInputStream stream = null;

        try{
            stream = new FileInputStream(this.container.resolve(CLUSTER_CONFIG_FILE).toString());
            ClusterConfiguration config = gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), ClusterConfiguration.class);
            return config;
        }catch(IOException error){
            logger.error("failed to read cluster configuration", error);
            throw new RuntimeException("failed to read in cluster config", error);
        }finally{
            if(stream != null){
                try{
                    stream.close();
                }catch(Exception e){
                    //ignore the error
                }
            }
        }
    }
    
    @Override
    public void saveClusterConfiguration(ClusterConfiguration configuration){
        Gson gson = new GsonBuilder().create();
        String configData = gson.toJson(configuration);
        try {
            Files.deleteIfExists(this.container.resolve(CLUSTER_CONFIG_FILE));
            FileOutputStream output = new FileOutputStream(this.container.resolve(CLUSTER_CONFIG_FILE).toString());
            output.write(configData.getBytes(StandardCharsets.UTF_8));
            output.flush();
            output.close();
        } catch (IOException error) {
            logger.error("failed to save cluster config to file", error);
        }
    }

    @Override
    public String getServerId() {
        return this.serverId;
    }

    @Override
    public synchronized void persistState(ServerState serverState) {
        try{
	    byte[] votedFor = BinaryUtils.stringToBytes(serverState.getVotedFor());
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * 2 + Integer.BYTES + votedFor.length);
            buffer.putLong(serverState.getTerm());
            buffer.putLong(serverState.getCommitIndex());
	    buffer.put(BinaryUtils.intToBytes(votedFor.length)); // 4
	    buffer.put(votedFor);
            this.serverStateFile.write(buffer.array());
            this.serverStateFile.seek(0);
        }catch(IOException ioError){
            logger.error("failed to write to the server state file", ioError);
            throw new RuntimeException("fatal I/O error while writing to the state file", ioError);
        }
    }

    @Override
    public synchronized ServerState readState() {
        try{
            if(this.serverStateFile.length() == 0){
                return null;
            }

            byte[] stateData = new byte[Long.BYTES * 2 + Integer.BYTES];
            this.read(stateData);
            ByteBuffer buffer = ByteBuffer.wrap(stateData);
            ServerState state = new ServerState();
            state.setTerm(buffer.getLong());
            state.setCommitIndex(buffer.getLong());
	    int num = buffer.getInt();
	    if (num > 0) {
		stateData = new byte[num];
		this.read(stateData);
		buffer = ByteBuffer.wrap(stateData);
		state.setVotedFor(new String(stateData, StandardCharsets.UTF_8));
	    }
	    this.serverStateFile.seek(0);
            return state;
        }catch(IOException ioError){
            logger.error("failed to read from the server state file", ioError);
            throw new RuntimeException("fatal I/O error while reading from state file", ioError);
        }
    }

    /**
     * 返回集群服务端 日志存储 对象 {@link #logStore}
     */
    @Override
    public SequentialLogStore loadLogStore() {
        return this.logStore;
    }

    public void close(){
        try{
            this.serverStateFile.close();
            this.logStore.close();
        }catch(IOException exception){
            logger.info("failed to shutdown the server state manager due to io error", exception);
        }
    }

    private void read(byte[] buffer){
        try{
            int offset = 0;
            int bytesRead = 0;
            while(offset < buffer.length && (bytesRead = this.serverStateFile.read(buffer, offset, buffer.length - offset)) != -1){
                offset += bytesRead;
            }

            if(offset < buffer.length){
                logger.error(String.format("only %d bytes are read while %d bytes are desired, bad file", offset, buffer.length));
                throw new RuntimeException("bad file, insufficient file data for reading");
            }
        }catch(IOException exception){
            logger.error("failed to read and fill the buffer", exception);
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    /**
     * 
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FileBasedServerStateManager [serverId=" + serverId + ", serverStateFile="
                + serverStateFile + ", logStore=" + logStore + ", container=" + container + "]";
    }

    /**
     * 
     * @return
     * @see net.data.technology.jraft.ServerStateManager#existsClusterConfiguration()
     */
    @Override
    public boolean existsClusterConfiguration() {
	return Files.exists(this.container.resolve(CLUSTER_CONFIG_FILE));
    }

}
