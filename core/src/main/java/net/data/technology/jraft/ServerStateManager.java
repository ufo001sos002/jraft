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
 * 服务器状态管理
 */
public interface ServerStateManager {

    /**
     * Load cluster configuration for this server <br>
     * 加载集群服务端 配置
     * 
     * @return the cluster configuration, never be null (not null)
     */
    public ClusterConfiguration loadClusterConfiguration();

    /**
     * 判断当前是否存在服务端集群配置
     * 
     * @return true 存在 false不存在
     */
    public boolean existsClusterConfiguration();

    /**
     * Save the cluster configuration <br>
     * 保存集群服务端 配置
     * 
     * @param configuration cluster configuration to save
     */
    public void saveClusterConfiguration(ClusterConfiguration configuration);

    /**
     * Save the server state<br>
     * 持久化本集群服务端 状态
     * 
     * @param serverState
     *            server state to persist 将被持久化的服务器状态
     */
    public void persistState(ServerState serverState);

    /**
     * Load server state<br>
     * 加载集群服务端 状态
     * 
     * @return the server state, never be null
     */
    public ServerState readState();

    /**
     * Load the log store for current server<br>
     * 返回集群服务端 数据记录存储 对象
     * 
     * @return the log store, never be null(not null)
     */
    public SequentialLogStore loadLogStore();

    /**
     * Get current server id<br>
     * 获取当前集群服务端id
     * 
     * @return server id for this server
     */
    public String getServerId();
}
