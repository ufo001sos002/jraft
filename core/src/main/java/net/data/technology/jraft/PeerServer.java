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

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Peer server in the same cluster for local server this represents a peer for
 * local server, it could be a leader, however, if local server is not a leader,
 * though it has a list of peer servers, they are not used <br>
 * 其他集群Server 对象
 * 
 * @author Data Technology LLC
 *
 */
public class PeerServer {
	/**
	 * 集群Server 配置对象
	 */
    private ClusterServer clusterConfig;
    /**
     * RPC客户端对象
     */
    private RpcClient rpcClient;
    /**
     * 当前心跳间隔 (毫秒)
     */
    private int currentHeartbeatInterval;
    /**
     * 心跳间隔 (毫秒)
     */
    private int heartbeatInterval;
    /**
     * RPC失败回退间隔 (毫秒)
     */
    private int rpcBackoffInterval;
    /**
     * 最大心跳间隔 (毫秒) TODO ?不理解{@link RaftParameters#getMaxHeartbeatInterval()} 中计算方式如何得来?
     */
    private int maxHeartbeatInterval;
    /**
     * 繁忙标志 1 为繁忙 0为空闲(默认值)
     */
    private AtomicInteger busyFlag;
    /**
     * 待提交标志 1为 待提交中 0为未待提交中(默认值)
     */
    private AtomicInteger pendingCommitFlag;
    /**
     * 心跳超时处理回调
     */
    private Callable<Void> heartbeatTimeoutHandler;
    /**
     * 心跳任务(默认null)
     */
    private ScheduledFuture<?> heartbeatTask;
    /**
     * 下次数据记录 索引位置(默认1)
     */
    private long nextLogIndex;
    /**
     * 当前匹配的 数据记录索引位置(默认0)
     */
    private long matchedIndex;
    /**
     * 心跳启动(true 开启) 默认未开启
     */
    private boolean heartbeatEnabled;
    /**
     * TODO ?不理解 注释作用?
     */
    private SnapshotSyncContext snapshotSyncContext;
    /**
     * 上下文共享计划线程池对象 {@link RaftContext#getScheduledExecutor()}
     */
    private Executor executor;

    /**
     * 
     * 根据参数构造 类{@link PeerServer} 对象
     * 
     * @param server
     *            集群服务器 配置对象
     * @param context
     *            集群上下文对象
     * @param heartbeatTimeOutConsumer
     *            心跳超时消费对象
     */
    public PeerServer(ClusterServer server, RaftContext context, final Consumer<PeerServer> heartbeatTimeOutConsumer) {
        this.clusterConfig = server;
        this.rpcClient = context.getRpcClientFactory().createRpcClient(server.getEndpoint());// 通过工厂创建rpc客户端
        this.busyFlag = new AtomicInteger(0);
        this.pendingCommitFlag = new AtomicInteger(0);
        this.heartbeatInterval = this.currentHeartbeatInterval = context.getRaftParameters().getHeartbeatInterval();
        this.maxHeartbeatInterval = context.getRaftParameters().getMaxHeartbeatInterval();
        this.rpcBackoffInterval = context.getRaftParameters().getRpcFailureBackoff();
        this.heartbeatTask = null;
        this.snapshotSyncContext = null;
        this.nextLogIndex = 1;
        this.matchedIndex = 0;
        this.heartbeatEnabled = false;
        this.executor = context.getScheduledExecutor();
        PeerServer self = this;
        this.heartbeatTimeoutHandler = new Callable<Void>(){

            @Override
            public Void call() throws Exception {
		heartbeatTimeOutConsumer.accept(self);
                return null;
            }};
    }

    /**
     * 
     * @return 返回 {@link ClusterServer#getId()} 的值
     */
    public int getId(){
        return this.clusterConfig.getId();
    }

    /**
     * @return 返回 {@link #clusterConfig}
     */
    public ClusterServer getClusterConfig(){
        return this.clusterConfig;
    }

    /**
     * @return 返回 {@link #heartbeatTimeoutHandler}
     */
    public Callable<Void> getHeartbeartHandler(){
        return this.heartbeatTimeoutHandler;
    }

    /**
     * (类对象级别) 线程同步
     * 
     * @return 返回 {@link #currentHeartbeatInterval}
     */
    public synchronized int getCurrentHeartbeatInterval(){
        return this.currentHeartbeatInterval;
    }

    public void setHeartbeatTask(ScheduledFuture<?> heartbeatTask){
        this.heartbeatTask = heartbeatTask;
    }

    public ScheduledFuture<?> getHeartbeatTask(){
        return this.heartbeatTask;
    }

    /**
     * 线程同步 设置当前繁忙 设置 {@link #busyFlag} 为1
     */
    public boolean makeBusy(){
        return this.busyFlag.compareAndSet(0, 1);
    }

    /**
     * 设置当前空闲 设置 {@link #busyFlag} 为0
     */
    public void setFree(){
        this.busyFlag.set(0);
    }

    /**
     * 是否开启心跳
     * 
     * @return true 开启 ,false未开启
     */
    public boolean isHeartbeatEnabled(){
        return this.heartbeatEnabled;
    }

    /**
     * 开启 / 关闭心跳 (关闭时 置 {@link #heartbeatTask} 为null)
     * 
     * @param enable
     */
    public void enableHeartbeat(boolean enable){
        this.heartbeatEnabled = enable;

        if(!enable){
            this.heartbeatTask = null;
        }
    }

    /**
     * 
     * 
     * @return 返回 {@link #nextLogIndex}
     */
    public long getNextLogIndex() {
        return nextLogIndex;
    }

    /**
     * 设置 {@link #nextLogIndex} 值
     * 
     * @param nextLogIndex
     */
    public void setNextLogIndex(long nextLogIndex) {
        this.nextLogIndex = nextLogIndex;
    }

    /**
     * 
     * @return 返回 {@link #matchedIndex} 值
     */
    public long getMatchedIndex(){
        return this.matchedIndex;
    }

    /**
     * 设置 {@link #matchedIndex} 值
     * 
     * @param matchedIndex
     */
    public void setMatchedIndex(long matchedIndex){
        this.matchedIndex = matchedIndex;
    }

    /**
     * 设置待提交中状态 设置 {@link #pendingCommitFlag} 为1
     */
    public void setPendingCommit(){
        this.pendingCommitFlag.set(1);
    }

    /**
     * 线程同步 清除 待提交中状态 设置 {@link #pendingCommitFlag} 为0
     */
    public boolean clearPendingCommit(){
        return this.pendingCommitFlag.compareAndSet(1, 0);
    }

    public void setSnapshotInSync(Snapshot snapshot){
        if(snapshot == null){
            this.snapshotSyncContext = null;
        }else{
            this.snapshotSyncContext = new SnapshotSyncContext(snapshot);
        }
    }

    public SnapshotSyncContext getSnapshotSyncContext(){
        return this.snapshotSyncContext;
    }

    public CompletableFuture<RaftResponseMessage> SendRequest(RaftRequestMessage request){
        boolean isAppendRequest = request.getMessageType() == RaftMessageType.AppendEntriesRequest || request.getMessageType() == RaftMessageType.InstallSnapshotRequest;
        return this.rpcClient.send(request)
                .thenComposeAsync((RaftResponseMessage response) -> {
                    if(isAppendRequest){
                        this.setFree();
                    }

                    this.resumeHeartbeatingSpeed();
                    return CompletableFuture.completedFuture(response);
                }, this.executor)
                .exceptionally((Throwable error) -> {
                    if(isAppendRequest){
                        this.setFree();
                    }

                    this.slowDownHeartbeating();
                    throw new RpcException(error, request);
                });
    }

    public synchronized void slowDownHeartbeating(){
        this.currentHeartbeatInterval = Math.min(this.maxHeartbeatInterval, this.currentHeartbeatInterval + this.rpcBackoffInterval);
    }

    public synchronized void resumeHeartbeatingSpeed(){
        if(this.currentHeartbeatInterval > this.heartbeatInterval){
            this.currentHeartbeatInterval = this.heartbeatInterval;
        }
    }
}
