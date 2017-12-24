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

package net.data.technology.jraft.extensions;
/**
 * 成对对象
 *
 * @param <TFirst> 第一个对象类
 * @param <TSecond> 第二个对象类
 */
public class Pair<TFirst, TSecond> {
	/**
	 * 第一个对象类 对象
	 */
    private TFirst first;
    /**
     * 第二个对象类 对象
     */
    private TSecond second;
    /**
     * 
     * 根据参数构造 类{@link Pair} 对象
     * @param first 第一个对象类 对象
     * @param second 第二个对象类 对象
     */
    public Pair(TFirst first, TSecond second){
        this.first = first;
        this.second = second;
    }
	/**
	 * @return 返回 {@link #first}值
	 */
	public TFirst getFirst() {
		return first;
	}
	/**
	 * @param 用参数first设置 {@link #first}
	 */
	public void setFirst(TFirst first) {
		this.first = first;
	}
	/**
	 * @return 返回 {@link #second}值
	 */
	public TSecond getSecond() {
		return second;
	}
	/**
	 * @param 用参数second设置 {@link #second}
	 */
	public void setSecond(TSecond second) {
		this.second = second;
	}
    
}
