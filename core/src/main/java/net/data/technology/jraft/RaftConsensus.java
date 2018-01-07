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
 * Raft 一致主运行类
 *
 */
public class RaftConsensus {

    public static RaftMessageSender run(RaftContext context){
        if(context == null){
            throw new IllegalArgumentException("context cannot be null");
        }

        RaftServer server = new RaftServer(context);// 服务端Raft消息处理类
        RaftMessageSender messageSender = server.createMessageSender();
        context.getStateMachine().start(messageSender);
        context.getRpcListener().startListening(server);
        return messageSender;
    }
}
