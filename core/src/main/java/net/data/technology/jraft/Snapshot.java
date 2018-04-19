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
 * 快照类
 */
public class Snapshot {
    /**
     * 最后一个 数据记录 的 索引位置
     */
    private long lastLogIndex;
    /**
     * 最后一个 数据索引 的 任期
     */
    private long lastLogTerm;
    /**
     * 数据大小
     */
    private long size;
    /**
     * 集群最后配置
     */
    private ClusterConfiguration lastConfig;

    /**
     * 
     * 根据参数构造 类{@link Snapshot} 对象 {@link #size} 默认0
     * 
     * @param lastLogIndex
     *            最后一个 数据记录 的 索引位置
     * @param lastLogTerm
     *            最后一个 数据索引 的 任期
     * @param lastConfig
     *            集群最后配置
     */
    public Snapshot(long lastLogIndex, long lastLogTerm, ClusterConfiguration lastConfig){
        this(lastLogIndex, lastLogTerm, lastConfig, 0);
    }

    /**
     * 
     * 根据参数构造 类{@link Snapshot} 对象
     * 
     * @param lastLogIndex
     *            最后一个 数据记录 的 索引位置
     * @param lastLogTerm
     *            最后一个 数据索引 的 任期
     * @param lastConfig
     *            集群最后配置
     * @param size
     *            数据大小
     */
    public Snapshot(long lastLogIndex, long lastLogTerm, ClusterConfiguration lastConfig, long size){
        this.lastConfig = lastConfig;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
        this.size = size;
    }

    /**
     * @return 返回 {@link #lastLogIndex}值
     */
    public long getLastLogIndex() {
        return lastLogIndex;
    }

    /**
     * @return 返回 {@link #lastLogTerm}值
     */
    public long getLastLogTerm() {
        return lastLogTerm;
    }

    /**
     * @return 返回 {@link #size}值
     */
    public long getSize() {
	return size;
    }

    /**
     * @return 返回 {@link #lastConfig}值
     */
    public ClusterConfiguration getLastConfig() {
        return lastConfig;
    }

    /**
     * 
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "Snapshot [lastLogIndex=" + lastLogIndex + ", lastLogTerm=" + lastLogTerm + ", size=" + size
		+ ", lastConfig=" + lastConfig + "]";
    }

}
