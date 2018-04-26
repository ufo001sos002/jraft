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

}

