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
 * Raft消息对象
 */
public class RaftMessage {
    /**
     * Raft消息类型
     */
    private RaftMessageType messageType;
    /**
     * 来源
     */
    private String source;
    /**
     * 目的地
     */
    private String destination;
    /**
     * 任期
     */
    private long term;

    /**
     * @return 返回 {@link #messageType}值
     */
    public RaftMessageType getMessageType() {
	return messageType;
    }

    /**
     * @param 用参数messageType设置
     *            {@link #messageType}
     */
    public void setMessageType(RaftMessageType messageType) {
	this.messageType = messageType;
    }

    /**
     * @return 返回 {@link #source}值
     */
    public String getSource() {
	return source;
    }

    /**
     * @param 用参数source设置
     *            {@link #source}
     */
    public void setSource(String source) {
	this.source = source;
    }

    /**
     * @return 返回 {@link #destination}值
     */
    public String getDestination() {
	return destination;
    }

    /**
     * @param 用参数destination设置
     *            {@link #destination}
     */
    public void setDestination(String destination) {
	this.destination = destination;
    }

    /**
     * @return 返回 {@link #term}值
     */
    public long getTerm() {
	return term;
    }

    /**
     * @param 用参数term设置
     *            {@link #term}
     */
    public void setTerm(long term) {
	this.term = term;
    }

}
