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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Raft客户端对象(公共类)
 */
public class RaftClient {
    /**
     * 日志对象
     */
    public static final Logger logger = LoggerFactory.getLogger(RaftClient.class);
    /**
     * K: serverId V: 对应RPC客户端对象
     */
    private Map<String, RpcClient> rpcClients = new HashMap<String, RpcClient>();
    /**
     * RPC客户端工厂
     */
    private RpcClientFactory rpcClientFactory;
    /**
     * Raft集群配置对象
     */
    private ClusterConfiguration configuration;

    private Timer timer;
    /**
     * 当前leader ID (初始化时为随机)
     */
    private String leaderId;
    /**
     * 当前处于 随机 leader时候 后续根据返回的leader 进行对leader再次发送消息<br>
     * true 表示 当前为随机leader(初始值为true)
     */
    private boolean randomLeader;
    private Random random;

    /**
     * 
     * <p>
     * Description:根据参数 创建Raft 客户端对象
     * </p>
     * 
     * @param rpcClientFactory RPC客户端工厂
     * @param configuration RAFT集群配置对象
     * @param loggerFactory 日志工厂
     */
    public RaftClient(RpcClientFactory rpcClientFactory, ClusterConfiguration configuration) {
        this.random = new Random(Calendar.getInstance().getTimeInMillis());
        this.rpcClientFactory = rpcClientFactory;
        this.configuration = configuration;
        this.leaderId = configuration.getServers().get(this.random.nextInt(configuration.getServers().size())).getId();// 最初始随机leaderId
        this.randomLeader = true;// 表示当前随机leader
        this.timer = new Timer();
    }

    /**
     * 增加日志
     * 
     * @param values 二维字节数组 [x][y] x为日志索引,同x下 y为具体某条x 对应日志内容
     * @return
     */
    public CompletableFuture<Boolean> appendEntries(byte[][] values){
        if(values == null || values.length == 0){
            throw new IllegalArgumentException("values cannot be null or empty");
        }

        LogEntry[] logEntries = new LogEntry[values.length];
        for(int i = 0; i < values.length; ++i){
            logEntries[i] = new LogEntry(0, values[i]);
        }

        RaftRequestMessage request = new RaftRequestMessage();
        request.setMessageType(RaftMessageType.ClientRequest);
        request.setLogEntries(logEntries);

        CompletableFuture<Boolean> result = new CompletableFuture<Boolean>();
        this.tryCurrentLeader(request, result, 0, 0);
        return result;
    }
    /**
     * 发送增加服务器消息
     * @param server
     * @return
     */
    public CompletableFuture<Boolean> addServer(ClusterServer server){
        if(server == null){
            throw new IllegalArgumentException("server cannot be null");
        }

        LogEntry[] logEntries = new LogEntry[1];
        logEntries[0] = new LogEntry(0, server.toBytes(), LogValueType.ClusterServer);
        RaftRequestMessage request = new RaftRequestMessage();
        request.setMessageType(RaftMessageType.AddServerRequest);
        request.setLogEntries(logEntries);

        CompletableFuture<Boolean> result = new CompletableFuture<Boolean>();
        this.tryCurrentLeader(request, result, 0, 0);
        return result;
    }
    
    /**
     * 移除服务器
     * @param serverId
     * @return
     */
    public CompletableFuture<Boolean> removeServer(String serverId) {
	if ("-1".equals(serverId) || serverId == null || serverId.length() <= 0) {
            throw new IllegalArgumentException("serverId must be equal or greater than zero");
        }
	byte[] idData = serverId.getBytes(StandardCharsets.UTF_8);
	ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + idData.length);
	buffer.putInt(idData.length);
	buffer.put(idData);
        LogEntry[] logEntries = new LogEntry[1];
        logEntries[0] = new LogEntry(0, buffer.array(), LogValueType.ClusterServer);
        RaftRequestMessage request = new RaftRequestMessage();
        request.setMessageType(RaftMessageType.RemoveServerRequest);
        request.setLogEntries(logEntries);

        CompletableFuture<Boolean> result = new CompletableFuture<Boolean>();
        this.tryCurrentLeader(request, result, 0, 0);
        return result;
    }

    /**
     * 尝试当前主进行 消息请求
     * 
     * @param request 请求消息
     * @param future 结果
     * @param rpcBackoff
     * @param retry 当前重试次数
     */
    private void tryCurrentLeader(RaftRequestMessage request, CompletableFuture<Boolean> future, int rpcBackoff, int retry){
	if (logger.isDebugEnabled())
	logger.debug(String.format("trying request to %s as current leader", this.leaderId));
        getOrCreateRpcClient().send(request).whenCompleteAsync((RaftResponseMessage response, Throwable error) -> {
            if(error == null){
		if (logger.isDebugEnabled())
		    logger.debug(String.format("response from remote server, leader: %s, accepted: %s",
			    response.getDestination(), String.valueOf(response.isAccepted())));
                if(response.isAccepted()){ // 判断发送日志是否被leader接受
                    future.complete(true); //接受
                }else{ // 未接受
                    // set the leader return from the server
                    if(this.leaderId == response.getDestination() && !this.randomLeader){ // 则判断当前是否为随机leader(即非leader)
                        future.complete(false); // 表明发往的为Leader,置发送日志任务结束  且发送的日志 不被接受
                    }else{
                        this.randomLeader = false;// 置为非随机 因为 集群已返回当前 leader id
                        this.leaderId = response.getDestination(); // 设置当前leaderId
                        tryCurrentLeader(request, future, rpcBackoff, retry); // 并重新向leader发送日志消息
                    }
                }
            }else{
		if (logger.isInfoEnabled())
		    logger.info(String.format("rpc error, failed to send request to remote server (%s)",
			    error.getMessage()));
                if(retry > configuration.getServers().size()){ // 重试次数大于 当前服务器节点数 则默认失败
                    future.complete(false);
                    return;
                }

                // try a random server as leader
                this.leaderId = this.configuration.getServers().get(this.random.nextInt(this.configuration.getServers().size())).getId();
                this.randomLeader = true;
                refreshRpcClient();

                if(rpcBackoff > 0){
                    timer.schedule(new TimerTask(){

                        @Override
                        public void run() {
                            tryCurrentLeader(request, future, rpcBackoff + 50, retry + 1);

                        }}, rpcBackoff);
                }else{
                    tryCurrentLeader(request, future, rpcBackoff + 50, retry + 1);
                }
            }
        });
    }

    /**
     * 获取 或 创建 前{@link #leaderId} 对应的RPC Client对象
     * 
     * @return 当前{@link #leaderId} 对应的 RPC Client对象
     */
    private RpcClient getOrCreateRpcClient(){
        synchronized(this.rpcClients){
            if (this.rpcClients.containsKey(this.leaderId)) { // 判断当前leaderId 对应 RPC 客户端对象是否存在
                return this.rpcClients.get(this.leaderId);
            }
            // 不存在 则创建
            RpcClient client = this.rpcClientFactory.createRpcClient(getLeaderEndpoint());
            this.rpcClients.put(this.leaderId, client);
            return client;
        }
    }

    private RpcClient refreshRpcClient(){
        synchronized(this.rpcClients){
            RpcClient client = this.rpcClientFactory.createRpcClient(getLeaderEndpoint());
            this.rpcClients.put(this.leaderId, client);
            return client;
        }
    }

    private String getLeaderEndpoint(){
        for(ClusterServer server : this.configuration.getServers()){
	    if (this.leaderId.equals(server.getId())) {
                return server.getEndpoint();
            }
        }
	if (logger.isInfoEnabled())
	    logger.info(String.format(
		    "no endpoint could be found for leader %s, that usually means no leader is elected, retry the first one",this.leaderId));
        this.randomLeader = true;
	ClusterServer server = this.configuration.getServers().get(0);
	this.leaderId = server.getId();
	return server.getEndpoint();
    }
}
