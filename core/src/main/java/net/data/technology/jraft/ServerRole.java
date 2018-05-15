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
 * 服务端校色
 */
public enum ServerRole {

    /**
     * 跟随者 0
     */
    Follower(0),
    /**
     * 候选者 1
     */
    Candidate(1),
    /**
     * 领导者 2
     */
    Leader(2);
    /**
     * 枚举值
     */
    private int value;

    ServerRole(int value) {
	this.value = value;
    }

    /**
     * @return {@link #value} 的值
     */
    public int getValue() {
	return value;
    }

    /**
     * 根据值返回对应枚举值
     * 
     * @param value
     * @return
     */
    public static ServerRole getServerRole(int value) {
	switch (value) {
	case 2:
	    return Leader;
	case 1:
	    return Candidate;
	default:
	    return Follower;
	}
    }

}
