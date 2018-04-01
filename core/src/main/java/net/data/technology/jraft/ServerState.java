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
 * 集群服务端 状态 <br>
 * 包含 {@link #term} {@link #commitIndex} {@link #votedFor}
 */
public class ServerState {
    /**
     * 任期
     */
    private long term;
    /**
     * 已提交索引
     */
    private long commitIndex;
    /**
     * 投票
     */
    private int votedFor;

    /**
	 * @return 返回 {@link #term}值
	 */
	public long getTerm() {
		return term;
	}

	/**
	 * @param 用参数term设置 {@link #term}
	 */
	public void setTerm(long term) {
		this.term = term;
	}

	/**
	 * @return 返回 {@link #votedFor}值
	 */
	public int getVotedFor() {
		return votedFor;
	}

	/**
	 * @param 用参数votedFor设置 {@link #votedFor}
	 */
	public void setVotedFor(int votedFor) {
		this.votedFor = votedFor;
	}

	/**
	 * @return 返回 {@link #commitIndex}值
	 */
	public long getCommitIndex() {
		return commitIndex;
	}
	/**
	 * 当前{@link #term} +1
	 */
	public void increaseTerm(){
        this.term += 1;
    }
	
	/**
	 * @param 用参数commitIndex设置 {@link #commitIndex} 大于才会生效
	 */
    public void setCommitIndex(long commitIndex) {
        if(commitIndex > this.commitIndex){
            this.commitIndex = commitIndex;
        }
    }

    /**
     * 
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ServerState [term=" + term + ", commitIndex=" + commitIndex + ", votedFor="
                + votedFor + "]";
    }
}
