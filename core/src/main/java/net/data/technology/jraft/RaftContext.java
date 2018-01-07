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

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Raft 上下文
 */
public class RaftContext {
	/**
	 * 服务器状态管理 对象
	 */
    private ServerStateManager serverStateManager;
    /**
     * RPC监听对象
     */
    private RpcListener rpcListener;
    /**
     * 日志工厂对象
     */
    private LoggerFactory loggerFactory;
    /**
     * RPC客户端工厂对象
     */
    private RpcClientFactory rpcClientFactory;
    /**
     * 状态机对象
     */
    private StateMachine stateMachine;
    /**
     * Raft参数对象
     */
    private RaftParameters raftParameters;
    /**
     * 计划线程池
     */
    private ScheduledThreadPoolExecutor scheduledExecutor;
    /**
     * 
     * 根据参数构造 类{@link RaftContext}  对象
     * @param stateManager
     * @param stateMachine
     * @param raftParameters
     * @param rpcListener
     * @param logFactory
     * @param rpcClientFactory
     */
    public RaftContext(ServerStateManager stateManager, StateMachine stateMachine, RaftParameters raftParameters, RpcListener rpcListener, LoggerFactory logFactory, RpcClientFactory rpcClientFactory){
        this(stateManager, stateMachine, raftParameters, rpcListener, logFactory, rpcClientFactory, null);
    }
    
    /**
     * 
     * 根据参数构造 类{@link RaftContext} 对象
     * @param stateManager
     * @param stateMachine
     * @param raftParameters
     * @param rpcListener
     * @param logFactory
     * @param rpcClientFactory
     * @param scheduledExecutor
     */
    public RaftContext(ServerStateManager stateManager, StateMachine stateMachine, RaftParameters raftParameters, RpcListener rpcListener, LoggerFactory logFactory, RpcClientFactory rpcClientFactory, ScheduledThreadPoolExecutor scheduledExecutor){
        this.serverStateManager = stateManager;
        this.stateMachine = stateMachine;
        this.raftParameters = raftParameters;
        this.rpcClientFactory = rpcClientFactory;
        this.rpcListener = rpcListener;
        this.loggerFactory = logFactory;
        this.scheduledExecutor = scheduledExecutor;
        if(this.scheduledExecutor == null){
            this.scheduledExecutor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
        }

        if(this.raftParameters == null){
            this.raftParameters = new RaftParameters()
                    .withElectionTimeoutUpper(300)
                    .withElectionTimeoutLower(150)
                    .withHeartbeatInterval(75)
                    .withRpcFailureBackoff(25)
                    .withMaximumAppendingSize(100)
                    .withLogSyncBatchSize(1000)
                    .withLogSyncStoppingGap(100)
                    .withSnapshotEnabled(0)
                    .withSyncSnapshotBlockSize(0);
        }
    }

	/**
	 * @return 返回 {@link #serverStateManager}值
	 */
	public ServerStateManager getServerStateManager() {
		return serverStateManager;
	}

	/**
	 * @return 返回 {@link #rpcListener}值
	 */
	public RpcListener getRpcListener() {
		return rpcListener;
	}

	/**
	 * @return 返回 {@link #loggerFactory}值
	 */
	public LoggerFactory getLoggerFactory() {
		return loggerFactory;
	}

	/**
	 * @return 返回 {@link #rpcClientFactory}值
	 */
	public RpcClientFactory getRpcClientFactory() {
		return rpcClientFactory;
	}

	/**
	 * @return 返回 {@link #stateMachine}值
	 */
	public StateMachine getStateMachine() {
		return stateMachine;
	}

	/**
	 * @return 返回 {@link #raftParameters}值
	 */
	public RaftParameters getRaftParameters() {
		return raftParameters;
	}

	/**
	 * @return 返回 {@link #scheduledExecutor}值
	 */
	public ScheduledThreadPoolExecutor getScheduledExecutor() {
		return scheduledExecutor;
	}
}
