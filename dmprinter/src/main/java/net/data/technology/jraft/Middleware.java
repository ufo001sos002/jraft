/**
 * 
 */
package net.data.technology.jraft;

import java.util.concurrent.CompletableFuture;

/**
 * <pre>
 * 1、配置同步 
 * 2、集群状态 
 * 3、初始化数据源
 * 4、VIP 绑定 监听
 * 5、客户端连接数据转发
 * 6、节点之间实例服务切换
 * 监管实例建立(数据源 数据转发)、监管实例迁移(主动或故障)
 * </pre>
 */
public class Middleware implements StateMachine {

    @Override
    public void start(RaftMessageSender raftMessageSender) {
	// TODO Auto-generated method stub

    }

    /* (non-Javadoc)
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

    /* (non-Javadoc)
     * @see net.data.technology.jraft.StateMachine#createSnapshot(net.data.technology.jraft.Snapshot)
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
