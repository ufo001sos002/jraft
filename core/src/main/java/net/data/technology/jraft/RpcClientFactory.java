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
 * RPC客户端工厂
 */
public interface RpcClientFactory {

    /**
     * Creates a RpcClient for the given endpoint <br>
     * 根据终端信息创建RPC客户端对象
     * 
     * @param endpoint endpoint for the server
     * @return an instance of RpcClient
     */
    public RpcClient createRpcClient(String endpoint);
}
