/**
 * 
 */
package net.data.technology.jraft;

import java.util.concurrent.CompletableFuture;

/**
 * <pre>
 * 1、配置同步 
 * 2、集群状态(leader发送rpc失败即表示 节点故障)
 * 3、初始化数据源
 * 4、VIP 绑定 监听
 * 5、客户端连接数据转发
 * 6、节点之间实例服务切换
 * 监管实例建立(数据源 数据转发)、监管实例迁移(主动或故障)
 * 解决思路，可考虑：
 * 1、增加字段configType 配置类型分为：0 全局(hcm)、1应用配置(hcs leader分配 具体实例分配等 可能也为全配置，
 * 只不过增加实例 应用节点id，以便leader切换时也能掌控切换)
 * 即：一份全局配置(0)由HCM下发，供 leader(先选出再应用配置) 识别 全配置 并进行后续(实例等)分配，
 * 包括重启时.
 * ★考虑从slave对Master也有心跳(以便检测slave是否离线)
 * 
 * 
 * 各集群节点完整配置包含(最终保存即一封完整的，leader连不上hcm时 即可读该份配置,进行操作)：
 * ----HCM下发全局各节点配置(运行参数等等，整个集群需监控的实例相关等等) ---简写 配置Q
 * ----缀加各节点实例分配配置  ---简写 配置 Z
 * 
 * 正常步骤：
 * 、HCS启动后与HCM通讯 拿全局 配置Q
 * 1、优先集群选出Leader 与 HCM进行通讯，等待HCM下发该组相关配置(运行参数、实例、集群节点等)
 * 2、leader收到后 先同步一遍 commit之后 各节点保存，再由Leader计算实例分配
 * 
 * 重启步骤：
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
