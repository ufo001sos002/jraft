/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package net.data.technology.jraft;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Cluster server configuration a class to hold the configuration information for a server in a cluster <br>
 * 集群Server 配置对象
 * 
 * <pre>
 * {

    "id":"3",
    "endpoint":"tcp://localhost:9003"

 * }
 * </pre>
 * 
 * TODO 或许需要增加 机器账号密码 密钥文件 用于 远程 SSH VIP 绑定
 * 
 * @author Data Technology LLC
 *
 */
public class ClusterServer {
    private static final Logger logger = LoggerFactory.getLogger(ClusterServer.class);
    /**
     * 集群Sever ID(唯一) not null
     */
    private String id;
    /**
     * 集群Server 终端字符串 格式：tcp://localhost:9003
     */
    private String endpoint;
    /**
     * 集群hcs 节点 ssh通讯端口
     */
    private int sshPort;
    /**
     * 是否使用私钥 true 使用
     */
    private boolean isUsedPrvkey;
    /**
     * 服务器 用户名 远程ssh登录用的信息
     */
    private String userName;
    /**
     * 认证的私钥文件内容(如果为null 则默认使用本地id_rsa私钥文件内容)
     */
    private byte[] prvkeyFileContent;
    /**
     * 登录用户密码或私钥密码(密文密码需使用id转换)
     */
    private String password;
    /**
     * 集群节点 状态 (参照：{@link StateMachine#STATUS_ONLINE} / {@link StateMachine#STATUS_OFFLINE})
     */
    private int status = StateMachine.STATUS_ONLINE;
    /**
     * 管理端port
     */
    private int managerPort;
    /**
     * 管理端用户
     */
    private String managerUser;
    /**
     * 管理端密码 根据{@link #cryptMandatory} 判断是否加密
     */
    private String managerPassword;

    /**
     * 
     * <p>
     * Description: 构造成员变量均为null {@link ClusterServer} 对象
     * </p>
     */
    public ClusterServer() {}

    /**
     * 
     * <p>
     * Description: 根据参数构造 {@link ClusterServer} 对象
     * </p>
     * 
     * @param id 集群节点id
     * @param ip 集群节点Raft通讯(RPC) ip
     * @param port 集群节点Raft通讯(RPC) port
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
     *        数组[格式：length|id|length|endpoint|sshPort|isUsedPrvkey|length|userName|length|prvkeyFileContent|
     *        length|password|managerPort|length|managerUserlength|managerPassword]
     */
    public ClusterServer(ByteBuffer buffer) {
        int dataSize = buffer.getInt();
        byte[] data = new byte[dataSize];
        buffer.get(data);
        this.id = new String(data, StandardCharsets.UTF_8);
        dataSize = buffer.getInt();
        data = new byte[dataSize];
        buffer.get(data);
        this.endpoint = new String(data, StandardCharsets.UTF_8);
        if (buffer.remaining() >= Integer.BYTES) {
            this.sshPort = buffer.getInt();
        } else {
            return;
        }
        if (buffer.hasRemaining()) {
            this.isUsedPrvkey = buffer.get() == 1;
        } else {
            return;
        }
        if (buffer.remaining() >= Integer.BYTES) {
            dataSize = buffer.getInt();
            if (dataSize > 0 && buffer.remaining() >= dataSize) {
                data = new byte[dataSize];
                buffer.get(data);
                if (isUsedPrvkey) {
                    this.prvkeyFileContent = data;
                } else {
                    this.userName = new String(data, StandardCharsets.UTF_8);
                }
            }
        } else {
            return;
        }
        if (buffer.remaining() >= Integer.BYTES) {
            dataSize = buffer.getInt();
            if (dataSize > 0 && buffer.remaining() >= dataSize) {
                data = new byte[dataSize];
                buffer.get(data);
                this.password = new String(data, StandardCharsets.UTF_8);
            }
        } else {
            return;
        }
        if (buffer.remaining() >= Integer.BYTES) {
            this.managerPort = buffer.getInt();
        } else {
            return;
        }
        if (buffer.remaining() >= Integer.BYTES) {
            dataSize = buffer.getInt();
            if (dataSize > 0 && buffer.remaining() >= dataSize) {
                data = new byte[dataSize];
                buffer.get(data);
                this.managerUser = new String(data, StandardCharsets.UTF_8);
            }
        } else {
            return;
        }
        if (buffer.remaining() >= Integer.BYTES) {
            dataSize = buffer.getInt();
            if (dataSize > 0 && buffer.remaining() >= dataSize) {
                data = new byte[dataSize];
                buffer.get(data);
                this.managerPassword = new String(data, StandardCharsets.UTF_8);
            }
        } else {
            return;
        }
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
     * 返回IP
     * 
     * @return
     * @throws Exception @ 析失败
     */
    public String getIp() {
        String ip = null;
        try {
            URI uri = new URI(this.endpoint);
            ip = uri.getHost();
        } catch (Exception e) {
	    logger.warn("serverId(" + id + ") getIp(from:" + this.endpoint + ") is error:" + e.getMessage(), e);
        }
        if (ip == null) {
            Matcher m = Pattern.compile("tcp://(\\S+):(\\d*+)").matcher(this.endpoint);
            if (m.find()) {
                ip = m.group(1);
            }
        }
        return ip;
    }

    /**
     * 返回Raft port
     * 
     * @return
     * @throws Exception @ 析失败
     */
    public int getRaftPort() {
        int port = -1;
        try {
            URI uri = new URI(this.endpoint);
            port = uri.getPort();
        } catch (Exception e) {
	    logger.warn("serverId(" + id + ") getRaftPort(from:" + this.endpoint + ") is error:" + e.getMessage(),
                    e);
        }
        if (port <= 0) {
            Matcher m = Pattern.compile("tcp://(\\S+):(\\d*+)").matcher(this.endpoint);
            if (m.find()) {
                port = Integer.parseInt(m.group(2));
            }
        }
        return port;
    }

    /**
     * Serialize a server configuration to binary data <br/>
     * 序列化服务配置 至 字节 数组<br>
     * [格式：length|id|length|endpoint|sshPort|isUsedPrvkey|length|userName|length|prvkeyFileContent|length|
     * password|managerPort|length|managerUser|length|managerPassword] TODO 考虑错误异常捕获情况
     * 
     * @return the binary data that represents the server configuration 服务器配置字节数组形式
     */
    public byte[] toBytes() {
        byte[] idData = this.id.getBytes(StandardCharsets.UTF_8);
        byte[] endpointData = this.endpoint.getBytes(StandardCharsets.UTF_8);
        int bufferLength = Integer.BYTES * 8 + Byte.BYTES + idData.length + endpointData.length;
        byte[] data = null;
        if (isUsedPrvkey) {
            if (this.prvkeyFileContent != null) {
                data = this.prvkeyFileContent;
            }
        } else {
            if (this.userName != null) {
                data = this.userName.getBytes(StandardCharsets.UTF_8);
            }
        }
        if (data != null) {
            bufferLength += data.length;
        }
        byte[] passwordData = null;
        if (this.password != null) {
            passwordData = this.password.getBytes(StandardCharsets.UTF_8);
            bufferLength += passwordData.length;
        }
        byte[] managerUserData = null;
        if (this.managerUser != null) {
            managerUserData = this.managerUser.getBytes(StandardCharsets.UTF_8);
            bufferLength += managerUserData.length;
        }
        byte[] managerPasswordData = null;
        if (this.managerPassword != null) {
            managerPasswordData = this.managerPassword.getBytes(StandardCharsets.UTF_8);
            bufferLength += managerPasswordData.length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(bufferLength);
        buffer.putInt(idData.length);
        buffer.put(idData);
        buffer.putInt(endpointData.length);
        buffer.put(endpointData);
        buffer.putInt(this.sshPort);
        buffer.put((byte) (this.isUsedPrvkey ? 1 : 0));
        if (data != null && data.length > 0) {
            buffer.putInt(data.length);
            buffer.put(data);
        } else {
            buffer.putInt(0);
        }
        if (passwordData != null && passwordData.length > 0) {
            buffer.putInt(passwordData.length);
            buffer.put(passwordData);
        } else {
            buffer.putInt(0);
        }
        buffer.putInt(this.managerPort);
        if (managerUserData != null && managerUserData.length > 0) {
            buffer.putInt(managerUserData.length);
            buffer.put(managerUserData);
        } else {
            buffer.putInt(0);
        }
        if (managerPasswordData != null && managerPasswordData.length > 0) {
            buffer.putInt(managerPasswordData.length);
            buffer.put(managerPasswordData);
        } else {
            buffer.putInt(0);
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
        return "ClusterServer [id=" + id + ", endpoint=" + endpoint + ", sshPort=" + sshPort
                + ", isUsedPrvkey=" + isUsedPrvkey + ", userName=" + userName + ", prvkeyFileContent="
                + Arrays.toString(prvkeyFileContent) + ", password=" + password + ", status=" + status
                + ", managerPort=" + managerPort + ", managerUser=" + managerUser + ", managerPassword="
                + managerPassword + "]";
    }

    /**
     * @return {@link #sshPort} 的值
     */
    public int getSshPort() {
        return sshPort;
    }

    /**
     * @param sshPort 根据 sshPort 设置 {@link #sshPort}的值
     */
    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    /**
     * @return {@link #isUsedPrvkey} 的值
     */
    public boolean isUsedPrvkey() {
        return isUsedPrvkey;
    }

    /**
     * @param isUsedPrvkey 根据 isUsedPrvkey 设置 {@link #isUsedPrvkey}的值
     */
    public void setUsedPrvkey(boolean isUsedPrvkey) {
        this.isUsedPrvkey = isUsedPrvkey;
    }

    /**
     * @return {@link #userName} 的值
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName 根据 userName 设置 {@link #userName}的值
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return {@link #prvkeyFileContent} 的值
     */
    public byte[] getPrvkeyFileContent() {
        return prvkeyFileContent;
    }

    /**
     * @param prvkeyFileContent 根据 prvkeyFileContent 设置 {@link #prvkeyFileContent}的值
     */
    public void setPrvkeyFileContent(byte[] prvkeyFileContent) {
        this.prvkeyFileContent = prvkeyFileContent;
    }

    /**
     * @return {@link #password} 的值
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password 根据 password 设置 {@link #password}的值
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return {@link #status} 的值
     */
    public int getStatus() {
        return status;
    }

    /**
     * @param status 根据 status 设置 {@link #status}的值
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * @return {@link #managerPort} 的值
     */
    public int getManagerPort() {
        return managerPort;
    }

    /**
     * @param managerPort 根据 managerPort 设置 {@link #managerPort}的值
     */
    public void setManagerPort(int managerPort) {
        this.managerPort = managerPort;
    }

    /**
     * @return {@link #managerUser} 的值
     */
    public String getManagerUser() {
        return managerUser;
    }

    /**
     * @param managerUser 根据 managerUser 设置 {@link #managerUser}的值
     */
    public void setManagerUser(String managerUser) {
        this.managerUser = managerUser;
    }

    /**
     * @return {@link #managerPassword} 的值
     */
    public String getManagerPassword() {
        return managerPassword;
    }

    /**
     * @param managerPassword 根据 managerPassword 设置 {@link #managerPassword}的值
     */
    public void setManagerPassword(String managerPassword) {
        this.managerPassword = managerPassword;
    }

    /**
     * 
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ClusterServer) {
            if (this.getId().equals(((ClusterServer) obj).getId())) {
                return true;
            }
        }
        return false;
    }

}
