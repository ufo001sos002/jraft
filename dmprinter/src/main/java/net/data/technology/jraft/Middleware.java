package net.data.technology.jraft;

import java.util.concurrent.CompletableFuture;

/**
 * <pre>
 * ★需要做的
 * 1、配置同步 
 * 2、集群状态(leader发送rpc失败即表示 节点故障,可能需要达到参数配置的次数 或者 添加其他判断依据，不然可能切换频繁)
 * 3、初始化数据源
 * 4、VIP 绑定 监听
 * 5、客户端连接数据源数据转发
 * 6、节点之间实例服务切换(动态 或 故障)
 * 监管实例建立(数据源 数据转发)、监管实例迁移(主动或故障)、实例由集群启动初始化时下发、实例动态添加、分配、移出
 * 
 * ★解决思路，可考虑：
 * 考虑从slave对Master也有心跳(以便检测slave是否离线)
 * Leader 与 HCM 中Raft客户端进行后续通讯
 * HCS启动后与HCM通讯 获取配置的连接 考虑下是否为长联(拿完配置作为心跳) 还是 短连 拿完配置后即断开？
 * 先定义流程————>再定义JSON
 * 
 * 
 * ★各集群节点完整配置包含(最终保存即一封完整的，leader连不上hcm时 即可读该份配置,进行操作)：
 * --公共配置(集群共享配置；包含:整个集群中节点信息、整个集群需监控的实例相关等等；由HCM 根据所处集群 下发，各集群节点一致) 
 * --私有配置(各集群节点私有启动相关配置；由HCM 根据集群节点分别下发，各集群节点不一致)
 * --分配配置(各节点被分配到 需要管理的实例，由Leader下发，各集群节点一致)
 * 以上构成完整配置，存在本地配置文件localconfig中
 * 
 * ★整个流程：创建集群、添加实例、分配实例、添加集群节点、实例动态切换、移除集群节点、集群节点故障发生(follower、Leader)、
 * 某个集群节点重启、所有集群节点都重启
 * 
 * 创建集群： (状态此时各种配置都为空)
 * 、HCS启动后与HCM通讯(心跳通道)，HCM根据hcsId得出所属集群hcsGroupId 并 下发 公共配置、私有配置
 * 、集群根据 公共配置 中 集群信息 选出Leader 与 HCM中Raft客户端(以下简称HCM) 进行后续通讯
 * 、等待后续 HCM 的 公共配置至Leader (可以包含私有配置,但需指定hcsId)
 * 
 * 添加实例：
 * 添加实例时 就 Leader发送 分配配置 分配好实例归属哪个节点 (后续实例操作均对应某节点服务器)
 * 
 * 分配实例：
 * 
 * 添加集群节点：
 * 
 * 实例动态切换：
 * 
 * 移除集群节点：
 * 
 * 集群节点故障发生：
 * 
 * 集群节点重启：
 * 
 * </pre>
 */
public class Middleware implements StateMachine {

    /**
     * 可以作为中间件相关启动初始化方法()
     */
    @Override
    public void start(RaftMessageSender raftMessageSender) {
	// TODO Auto-generated method stub

    }

    /**
     * 针对提交的 日志 进行应用(考虑)
     * 
     * @param logIndex
     * @param data
     * @see net.data.technology.jraft.StateMachine#commit(long, byte[])
     */
    @Override
    public void commit(long logIndex, byte[] data) {
	// TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.data.technology.jraft.StateMachine#rollback(long, byte[])
     */
    @Override
    public void rollback(long logIndex, byte[] data) {
	// TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.data.technology.jraft.StateMachine#preCommit(long, byte[])
     */
    @Override
    public void preCommit(long logIndex, byte[] data) {
	// TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.data.technology.jraft.StateMachine#saveSnapshotData(net.data.technology.jraft.Snapshot, long, byte[])
     */
    @Override
    public void saveSnapshotData(Snapshot snapshot, long offset, byte[] data) {
	// TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see net.data.technology.jraft.StateMachine#applySnapshot(net.data.technology.jraft.Snapshot)
     */
    @Override
    public boolean applySnapshot(Snapshot snapshot) {
	// TODO Auto-generated method stub
	return false;
    }

    /* (non-Javadoc)
     * @see net.data.technology.jraft.StateMachine#readSnapshotData(net.data.technology.jraft.Snapshot, long, byte[])
     */
    @Override
    public int readSnapshotData(Snapshot snapshot, long offset, byte[] buffer) {
	// TODO Auto-generated method stub
	return 0;
    }

    /* (non-Javadoc)
     * @see net.data.technology.jraft.StateMachine#getLastSnapshot()
     */
    @Override
    public Snapshot getLastSnapshot() {
	// TODO Auto-generated method stub
	return null;
    }

    /**
     * 定期将收到的配置写入 文件<br>
     * <b>注：可能每次收到的配置应用成功后 才写入快照中，未应用成功的将不写入，但是记录最新commit了的值</b>
     */
    @Override
    public CompletableFuture<Boolean> createSnapshot(Snapshot snapshot) {
	// TODO Auto-generated method stub
	return null;
    }

    /* (non-Javadoc)
     * @see net.data.technology.jraft.StateMachine#exit(int)
     */
    @Override
    public void exit(int code) {
	// TODO Auto-generated method stub

    }

}
