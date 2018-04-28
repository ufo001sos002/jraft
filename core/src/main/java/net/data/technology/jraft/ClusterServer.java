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
import java.nio.charset.StandardCharsets;

/**
 * Cluster server configuration a class to hold the configuration information
 * for a server in a cluster <br>
 * 集群Server 配置对象
 * 
 * <pre>
 * {

    "id":"3",
    "endpoint":"tcp://localhost:9003"

 * }
 * </pre>
 * 
 * @author Data Technology LLC
 *
 */
public class ClusterServer {
    /**
     * 集群Sever ID(唯一)
     */
    private String id;
    /**
     * 集群Server 终端字符串 格式：tcp://localhost:9003
     */
    private String endpoint;

    /**
     * 
     * <p>
     * Description: 构造成员变量均为null {@link ClusterServer} 对象
     * </p>
     */
    public ClusterServer() {
    }

    /**
     * 
     * <p>
     * Description: 根据参数构造 {@link ClusterServer} 对象
     * </p>
     * 
     * @param id
     *            集群节点id
     * @param ip
     *            集群节点Raft通讯(RPC) ip
     * @param port
     *            集群节点Raft通讯(RPC) port
     */
    public ClusterServer(String id, String ip, int port) {
	this.id = id;
	this.endpoint = "tcp://" + ip + ":" + port;
    }

    /**
     * 
     * <p>
     * Description: 从参数中 获取集群Server对象
     * </p>
     * 
     * @param buffer
     *            数组[格式：length|id|length|data]
     */
    public ClusterServer(ByteBuffer buffer){
	int dataSize = buffer.getInt();
	byte[] data = new byte[dataSize];
	buffer.get(data);
	this.id = new String(data, StandardCharsets.UTF_8);
	dataSize = buffer.getInt();
	data = new byte[dataSize];
	buffer.get(data);
	this.endpoint = new String(data, StandardCharsets.UTF_8);
    }

    /**
     * @return {@link #id} 的值
     */
    public String getId() {
        return id;
    }

    /**
     * @param id 根据 id 设置 {@link #id}的值
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return {@link #endpoint} 的值
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param endpoint 根据 endpoint 设置 {@link #endpoint}的值
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Serialize a server configuration to binary data <br/>
     * 序列化服务配置 至 字节 数组[格式：id|length|data]
     * 
     * @return the binary data that represents the server configuration 服务器配置字节数组形式
     */
    public byte[] toBytes(){
	byte[] idData = this.id.getBytes(StandardCharsets.UTF_8);
        byte[] endpointData = this.endpoint.getBytes(StandardCharsets.UTF_8);
	ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES * 2 + idData.length + endpointData.length);
	buffer.putInt(idData.length);
	buffer.put(idData);
        buffer.putInt(endpointData.length);
        buffer.put(endpointData);
        return buffer.array();
    }

    /**
     * 
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ClusterServer [id=" + id + ", endpoint=" + endpoint + "]";
    }

}
