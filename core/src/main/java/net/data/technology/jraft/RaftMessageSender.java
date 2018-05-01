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

import java.util.concurrent.CompletableFuture;

/**
 * 当前 Raft 服务端 消息发送者 接口
 */
public interface RaftMessageSender {

    /**
     * Add a new server to the cluster
     * <br>增加一个服务器(对象) 至 集群
     * @param server new member of cluster 集群Server 配置对象
     * @return true if request is accepted, or false if no leader, rpc fails or leader declines
     *  <br>如果请求成功 返回 true,如果 没有当前没有leader 或 RPC 失败 或者 leader节点拒绝  返回false
     */
    CompletableFuture<Boolean> addServer(ClusterServer server);

    /**
     * Remove a server from cluster, the server will step down when the removal is confirmed
     * <br>从RAFT集群移除  一个服务器(对象) 删除确认后 服务器将退出Raft集群
     * @param serverId the id for the server to be removed 服务器对象ID
     * @return true if request is accepted or false if no leader, rpc fails or leader declines
     * <br>如果请求成功 返回 true,如果 没有当前没有leader 或 RPC 失败 或者 leader节点拒绝  返回false
     */
    CompletableFuture<Boolean> removeServer(String serverId);

    /**
     * Append multiple application logs to log store
     * <br>追加多个 应用程序类型({@link RaftMessageType.ClientRequest})数据记录 进行存储  
     * @param values the application log entries
     * @return true if request is accepted or false if no leader, rpc fails or leader declines
     * <br>如果请求成功 返回 true,如果 没有当前没有leader 或 RPC 失败 或者 leader节点拒绝  返回false
     */
    CompletableFuture<Boolean> appendEntries(byte[][] values);
}
