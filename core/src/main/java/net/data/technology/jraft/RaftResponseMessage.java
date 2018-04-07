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
 * Raft回包消息对象
 */
public class RaftResponseMessage extends RaftMessage {
	/**
	 * 下次索引位置
	 */
    private long nextIndex;
    /**
     * 发送日志是否接受成功
     */
    private boolean accepted;
	/**
	 * @return 返回 {@link #nextIndex}值
	 */
	public long getNextIndex() {
		return nextIndex;
	}
	/**
	 * @param 用参数nextIndex设置 {@link #nextIndex}
	 */
	public void setNextIndex(long nextIndex) {
		this.nextIndex = nextIndex;
	}
	/**
	 * @return 返回 {@link #accepted}值
	 */
	public boolean isAccepted() {
		return accepted;
	}
	/**
	 * @param 用参数accepted设置 {@link #accepted}
	 */
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "RaftResponseMessage [nextIndex=" + nextIndex + ", accepted=" + accepted + "]";
    }

}
