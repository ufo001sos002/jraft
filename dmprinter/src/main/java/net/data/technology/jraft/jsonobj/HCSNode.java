package net.data.technology.jraft.jsonobj;

import com.alibaba.fastjson.JSON;

/**
 * 集群节点信息
 */
public class HCSNode {
    /**
     * hcs节点 Id
     */
    private String hcsId;
    /**
     * 集群 hcs节点 IP
     */
    private String ip;
    /**
     * 集群hcs 节点 Raft通讯端口
     */
    private Integer port;
    /**
     * 服务器状态: 0为在线可用(默认), 1为离线
     */
    private Integer status;
    /**
     * 集群hcs 节点 ssh通讯端口
     */
    private Integer sshPort;
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
     * @return {@link #hcsId} 的值
     */
    public String getHcsId() {
	return hcsId;
    }

    /**
     * @param hcsId
     *            根据 hcsId 设置 {@link #hcsId}的值
     */
    public void setHcsId(String hcsId) {
	this.hcsId = hcsId;
    }

    /**
     * @return {@link #ip} 的值
     */
    public String getIp() {
	return ip;
    }

    /**
     * @param ip
     *            根据 ip 设置 {@link #ip}的值
     */
    public void setIp(String ip) {
	this.ip = ip;
    }

    /**
     * @return {@link #port} 的值
     */
    public Integer getPort() {
	return port;
    }

    /**
     * @param port
     *            根据 port 设置 {@link #port}的值
     */
    public void setPort(Integer port) {
	this.port = port;
    }

    /**
     * @return {@link #status} 的值
     */
    public Integer getStatus() {
	return status;
    }

    /**
     * @param status
     *            根据 status 设置 {@link #status}的值
     */
    public void setStatus(Integer status) {
	this.status = status;
    }

    /**
     * @return {@link #userName} 的值
     */
    public String getUserName() {
	return userName;
    }

    /**
     * @param userName
     *            根据 userName 设置 {@link #userName}的值
     */
    public void setUserName(String userName) {
	this.userName = userName;
    }

    /**
     * @return {@link #isUsedPrvkey} 的值
     */
    public boolean isUsedPrvkey() {
	return isUsedPrvkey;
    }

    /**
     * @param isUsedPrvkey
     *            根据 isUsedPrvkey 设置 {@link #isUsedPrvkey}的值
     */
    public void setUsedPrvkey(boolean isUsedPrvkey) {
	this.isUsedPrvkey = isUsedPrvkey;
    }

    /**
     * @return {@link #prvkeyFileContent} 的值
     */
    public byte[] getPrvkeyFileContent() {
	return prvkeyFileContent;
    }

    /**
     * @param prvkeyFileContent
     *            根据 prvkeyFileContent 设置 {@link #prvkeyFileContent}的值
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
     * @return {@link #sshPort} 的值
     */
    public Integer getSshPort() {
	return sshPort;
    }

    /**
     * @param sshPort
     *            根据 sshPort 设置 {@link #sshPort}的值
     */
    public void setSshPort(Integer sshPort) {
	this.sshPort = sshPort;
    }

    /**
     * @param password
     *            根据 password 设置 {@link #password}的值
     */
    public void setPassword(String password) {
	this.password = password;
    }

    @Override
    public String toString() {
	return JSON.toJSONString(this);
    }
}

