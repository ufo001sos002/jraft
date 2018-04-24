package net.data.technology.jraft.jsonobj;

/**
 * 集群节点信息
 */
public class HCSNode {
    /**
     * hcs节点 Id
     */
    public String hcsId;
    /**
     * 集群 hcs节点 IP
     */
    public String ip;
    /**
     * 集群hcs 节点 Raft通讯端口
     */
    public Integer port;
    /**
     * 服务器状态: 0为在线可用(默认), 1为离线
     */
    public Integer status;
}

