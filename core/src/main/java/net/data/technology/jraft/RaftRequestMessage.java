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
 * Raft请求消息对象
 */
public class RaftRequestMessage extends RaftMessage {
	/**
	 * 最后日志任期
	 */
    private long lastLogTerm;
    /**
     * 最后日志索引位置
     */
    private long lastLogIndex;
    /**
     * 已提交的索引位置
     */
    private long commitIndex;
    /**
     * 日志对象数组
     */
    private LogEntry[] logEntries;
	/**
	 * @return 返回 {@link #lastLogTerm}值
	 */
	public long getLastLogTerm() {
		return lastLogTerm;
	}
	/**
	 * @param 用参数lastLogTerm设置 {@link #lastLogTerm}
	 */
	public void setLastLogTerm(long lastLogTerm) {
		this.lastLogTerm = lastLogTerm;
	}
	/**
	 * @return 返回 {@link #lastLogIndex}值
	 */
	public long getLastLogIndex() {
		return lastLogIndex;
	}
	/**
	 * @param 用参数lastLogIndex设置 {@link #lastLogIndex}
	 */
	public void setLastLogIndex(long lastLogIndex) {
		this.lastLogIndex = lastLogIndex;
	}
	/**
	 * @return 返回 {@link #commitIndex}值
	 */
	public long getCommitIndex() {
		return commitIndex;
	}
	/**
	 * @param 用参数commitIndex设置 {@link #commitIndex}
	 */
	public void setCommitIndex(long commitIndex) {
		this.commitIndex = commitIndex;
	}
	/**
	 * @return 返回 {@link #logEntries}值
	 */
	public LogEntry[] getLogEntries() {
		return logEntries;
	}
	/**
	 * @param 用参数logEntries设置 {@link #logEntries}
	 */
	public void setLogEntries(LogEntry[] logEntries) {
		this.logEntries = logEntries;
	}
}
