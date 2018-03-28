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
 * RPC Server端监听 接口类
 */
public interface RpcListener {

    /**
     * Starts listening and handle all incoming messages with messageHandler
     * <br>开始侦听并使用消息处理对象 处理所有传入消息
     * @param messageHandler the message handler to handle all incoming requests<br>
     * 消息处理对象 处理所有传入请求的消息处理程序
     */
    public void startListening(RaftMessageHandler messageHandler);

    /**
     * Stop listening and clean up
     * <br>关闭监听和清除
     */
    public void stop();
}
