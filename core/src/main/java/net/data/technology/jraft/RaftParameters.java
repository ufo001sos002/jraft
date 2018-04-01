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
/**
 * Raft参数对象
 */
public class RaftParameters {
    /**
     * Election timeout upper bound in milliseconds<br/>
     * 选举超时上限毫秒数
     */
    private int electionTimeoutUpperBound;
    /**
     * Election timeout lower bound in milliseconds<br/>
     * 选举超时下限毫秒数
     */
    private int electionTimeoutLowerBound;
    /**
     * heartbeat interval in milliseconds<br/>
     * 心跳间隔 毫秒
     */
    private int heartbeatInterval;
    /**
     * Rpc failure backoff in milliseconds <br>
     * RPC 失败回退 时间(毫秒)
     */
    private int rpcFailureBackoff;
    private int logSyncBatchSize;
    private int logSyncStopGap;
    /**
     * The commit distances for snapshots, zero means don't take any snapshots<br/>
     * 进行快照操作的 已提交 次数 0为不创建快照
     */
    private int snapshotDistance;
    private int snapshotBlockSize;
    private int maxAppendingSize;

    /**
     * The tcp block size for syncing the snapshots
     * 设置{@link #snapshotBlockSize}值
     * @param size size of sync block
     * @return self
     */
    public RaftParameters withSyncSnapshotBlockSize(int size){
        this.snapshotBlockSize = size;
        return this;
    }

    /**
     * Enable log compact and snapshot with the commit distance
     * 设置{@link #snapshotDistance}值
     * @param distance log distance to compact between two snapshots
     * @return self
     */
    public RaftParameters withSnapshotEnabled(int distance){
        this.snapshotDistance = distance;
        return this;
    }

    /**
     * For new member that just joined the cluster, we will use log sync to ask it to catch up,
     * and this parameter is to tell when to stop using log sync but appendEntries for the new server
     * when leaderCommitIndex - indexCaughtUp &lt; logSyncStopGap, then appendEntries will be used
     * 设置{@link #logSyncStopGap}值
     * @param logSyncStopGap the log gap to stop log pack-and-sync feature
     * @return self
     */
    public RaftParameters withLogSyncStoppingGap(int logSyncStopGap){
        this.logSyncStopGap = logSyncStopGap;
        return this;
    }

    /**
     * For new member that just joined the cluster, we will use log sync to ask it to catch up,
     * and this parameter is to specify how many log entries to pack for each sync request
     * 设置{@link #logSyncBatchSize}值
     * @param logSyncBatchSize the batch size fo pack-and-sync feature
     * @return self
     */
    public RaftParameters withLogSyncBatchSize(int logSyncBatchSize){
        this.logSyncBatchSize = logSyncBatchSize;
        return this;
    }

    /**
     * The maximum log entries could be attached to an appendEntries call
     * 设置{@link #maxAppendingSize}值
     * @param maxAppendingSize size limit for appendEntries call
     * @return self
     */
    public RaftParameters withMaximumAppendingSize(int maxAppendingSize){
        this.maxAppendingSize = maxAppendingSize;
        return this;
    }

    /**
     * Election timeout upper bound in milliseconds
     * 设置{@link #electionTimeoutUpperBound}值
     * @param electionTimeoutUpper election timeout upper value
     * @return self
     */
    public RaftParameters withElectionTimeoutUpper(int electionTimeoutUpper){
        this.electionTimeoutUpperBound = electionTimeoutUpper;
        return this;
    }

    /**
     * Election timeout lower bound in milliseconds
     * 设置{@link #electionTimeoutLowerBound}值
     * @param electionTimeoutLower election timeout lower value
     * @return self
     */
    public RaftParameters withElectionTimeoutLower(int electionTimeoutLower){
        this.electionTimeoutLowerBound = electionTimeoutLower;
        return this;
    }

    /**
     * heartbeat interval in milliseconds
     * 设置{@link #heartbeatInterval}值
     * @param heartbeatInterval heart beat interval
     * @return self
     */
    public RaftParameters withHeartbeatInterval(int heartbeatInterval){
        this.heartbeatInterval = heartbeatInterval;
        return this;
    }

    /**
     * Rpc failure backoff in milliseconds
     * 设置{@link #rpcFailureBackoff}值
     * @param rpcFailureBackoff rpc failure back off
     * @return self
     */
    public RaftParameters withRpcFailureBackoff(int rpcFailureBackoff){
        this.rpcFailureBackoff = rpcFailureBackoff;
        return this;
    }

    /**
     * Upper value for election timeout
     * @return upper of election timeout in milliseconds 返回{@link #electionTimeoutUpperBound}值
     */
    public int getElectionTimeoutUpperBound() {
        return electionTimeoutUpperBound;
    }

    /**
     * Lower value for election timeout
     * @return lower of election timeout in milliseconds 返回{@link #electionTimeoutLowerBound}值
     */
    public int getElectionTimeoutLowerBound() {
        return electionTimeoutLowerBound;
    }

    /**
     * Heartbeat interval for each peer
     * @return heartbeat interval in milliseconds 返回{@link #heartbeatInterval}值
     */
    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    /**
     * Rpc backoff for peers that failed to be connected
     * @return rpc backoff in milliseconds 返回{@link #rpcFailureBackoff}值
     */
    public int getRpcFailureBackoff() {
        return rpcFailureBackoff;
    }

    /**
     * The maximum heartbeat interval, any value beyond this may lead to election
     * timeout for a peer before receiving a heartbeat
     * 最大心跳间隔，在接收心跳之前，任何超出此值的值都可能导致选举超时 <br>
     * 返回max({@link #heartbeatInterval}, ({@link #electionTimeoutLowerBound} -
     * {@link #heartbeatInterval} / 2))值
     * 
     * @return maximum heartbeat interval (including rpc backoff) in
     *         milliseconds<br>
     *         最大心跳间隔 包括(rpc 失败 回退)(毫秒)
     */
    public int getMaxHeartbeatInterval(){
        return Math.max(this.heartbeatInterval, this.electionTimeoutLowerBound - this.heartbeatInterval / 2);
    }

    /**
     * The batch size for each ReplicateLogRequest message
     * @return batch size in bytes 返回{@link #logSyncBatchSize}值
     */
    public int getLogSyncBatchSize() {
        return logSyncBatchSize;
    }

    /**
     * the max gap allowed for log sync, if the gap between the client and leader is less than this value,
     * the ReplicateLogRequest will be stopped
     * @return maximum gap allowed in bytes for log sync 返回{@link #logSyncStopGap}值
     */
    public int getLogSyncStopGap() {
        return logSyncStopGap;
    }

    /**
     * The commit distances for snapshots, zero means don't take any snapshots
     * 
     * @return commit distances for log store 返回{@link #snapshotDistance}值
     */
    public int getSnapshotDistance(){
        return this.snapshotDistance;
    }

    /**
     * The block size to sync while syncing snapshots to peers
     * @return block size in bytes 返回{@link #snapshotBlockSize}值
     */
    public int getSnapshotBlockSize() {
        return snapshotBlockSize;
    }

    /**
     * The maximum log entries in an appendEntries request
     * @return maximum log entries 返回{@link #maxAppendingSize}值
     */
    public int getMaximumAppendingSize(){
        return this.maxAppendingSize;
    }

    /**
     * 
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RaftParameters [electionTimeoutUpperBound=" + electionTimeoutUpperBound
                + ", electionTimeoutLowerBound=" + electionTimeoutLowerBound
                + ", heartbeatInterval=" + heartbeatInterval + ", rpcFailureBackoff="
                + rpcFailureBackoff + ", logSyncBatchSize=" + logSyncBatchSize + ", logSyncStopGap="
                + logSyncStopGap + ", snapshotDistance=" + snapshotDistance + ", snapshotBlockSize="
                + snapshotBlockSize + ", maxAppendingSize=" + maxAppendingSize + "]";
    }

}
