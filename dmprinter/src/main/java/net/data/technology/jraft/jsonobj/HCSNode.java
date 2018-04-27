package net.data.technology.jraft.jsonobj;

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
     * 服务器 用户名 远程ssh登录用的信息
     */
    private String userName;
    /**
     * 服务器 密码
     */
    private String userPassord;

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
     * @return {@link #userPassord} 的值
     */
    public String getUserPassord() {
	return userPassord;
    }

    /**
     * @param userPassord
     *            根据 userPassord 设置 {@link #userPassord}的值
     */
    public void setUserPassord(String userPassord) {
	this.userPassord = userPassord;
    }

}

