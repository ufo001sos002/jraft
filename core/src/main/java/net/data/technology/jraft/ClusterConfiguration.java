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

package net.data.technology.jraft;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Cluster configuration, a class to hold the cluster configuration information
 * <br>
 * 集群配置对象<br>
 * 
 * <pre>
{
    "logIndex": 0,
    "lastLogIndex": 0,
    "servers": [
        {
            "id": "1",
            "endpoint": "tcp://localhost:9001"
        },
        {
            "id": "2",
            "endpoint": "tcp://localhost:9002"
        },
        {
            "id": "3",
            "endpoint": "tcp://localhost:9003"
        }
    ]
}
 * </pre>
 * 
 * @author Data Technology LLC
 *
 */
public class ClusterConfiguration {
    /**
     * 日志索引
     */
    private long logIndex;
    /**
     * 最后日志索引
     */
    private long lastLogIndex;
    /**
     * 集群Server集合
     */
    private List<ClusterServer> servers;

    public ClusterConfiguration(){
        this.servers = new LinkedList<ClusterServer>();
        this.logIndex = 0;
        this.lastLogIndex = 0;
    }

    /**
     * De-serialize the data stored in buffer to cluster configuration this is used for the peers to
     * get the cluster configuration from log entry value <br>
     * 从buffer中获取集群配置信息
     * 
     * @param buffer the binary data
     * @return cluster configuration
     */
    public static ClusterConfiguration fromBytes(ByteBuffer buffer){
        ClusterConfiguration configuration = new ClusterConfiguration();
        configuration.setLogIndex(buffer.getLong());
        configuration.setLastLogIndex(buffer.getLong());
        while(buffer.hasRemaining()){
            configuration.getServers().add(new ClusterServer(buffer));
        }

        return configuration;
    }

    /**
     * De-serialize the data stored in buffer to cluster configuration this is used for the peers to
     * get the cluster configuration from log entry value <br>
     * 从byte数组中获取集群配置信息
     * 
     * @param data the binary data
     * @return cluster configuration
     */
    public static ClusterConfiguration fromBytes(byte[] data){
        return fromBytes(ByteBuffer.wrap(data));
    }


    /**
     * @return {@link #logIndex} 的值
     */
    public long getLogIndex() {
        return logIndex;
    }

    /**
     * @param logIndex 根据 logIndex 设置 {@link #logIndex}的值
     */
    public void setLogIndex(long logIndex) {
        this.logIndex = logIndex;
    }


    /**
     * Gets the log index that contains the previous cluster configuration
     * 
     * @return log index {@link #lastLogIndex} 的值
     * 
     */
    public long getLastLogIndex() {
        return lastLogIndex;
    }

    /**
     * @param lastLogIndex 根据 lastLogIndex 设置 {@link #lastLogIndex}的值
     */
    public void setLastLogIndex(long lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }


    /**
     * @return {@link #servers} 的值
     */
    public List<ClusterServer> getServers() {
        return servers;
    }

    /**
     * Try to get a cluster server configuration from cluster configuration
     * 
     * @param id
     *            the server id
     * @return a cluster server configuration or null if id is not found<br>
     *         返回id对应集群Server配置对象, id未对应时返回null
     */
    public ClusterServer getServer(String id) {
        for(ClusterServer server : this.servers){
	    if (server.getId().equals(id)) {
                return server;
            }
        }

        return null;
    }

    /**
     * Serialize the cluster configuration into a buffer
     * this is used for the leader to serialize a new cluster configuration and replicate to peers
     * @return binary data that represents the cluster configuration
     */
    public byte[] toBytes(){
        int totalSize = Long.BYTES * 2;
        List<byte[]> serversData = new ArrayList<byte[]>(this.servers.size());
        for(int i = 0; i < this.servers.size(); ++i){
            ClusterServer server = this.servers.get(i);
            byte[] dataForServer = server.toBytes();
            totalSize += dataForServer.length;
            serversData.add(dataForServer);
        }

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.putLong(this.logIndex);
        buffer.putLong(this.lastLogIndex);
        for(int i = 0; i < serversData.size(); ++i){
            buffer.put(serversData.get(i));
        }

        return buffer.array();
    }

    /**
     * 
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ClusterConfiguration [logIndex=" + logIndex + ", lastLogIndex=" + lastLogIndex
                + ", servers=" + servers + "]";
    }

}
