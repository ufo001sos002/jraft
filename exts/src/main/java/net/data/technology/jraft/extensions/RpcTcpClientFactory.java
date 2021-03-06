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

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.data.technology.jraft.RpcClient;
import net.data.technology.jraft.RpcClientFactory;

/**
 * RPC TCP 客户端工厂 实现类 实现 {@link RpcClientFactory}接口类
 */
public class RpcTcpClientFactory implements RpcClientFactory {
    /**
     * 日志对象
     */
    private static final Logger logger = LoggerFactory.getLogger(RpcTcpClientFactory.class);
    /**
     * 线程池对象 所有RPC TCP客户端共用 用来建立socket连接
     */
    private ExecutorService executorService;

    /**
     * <p>
     * Description: 根据线程池构造RPC客户端工厂
     * </p>
     * 
     * @param executorService
     *            线程池对象 所有RPC TCP客户端共用 用来建立socket连接
     */
    public RpcTcpClientFactory(ExecutorService executorService){
        this.executorService = executorService;
    }

    @Override
    public RpcClient createRpcClient(String endpoint) {
        try {
            URI uri = new URI(endpoint);
            return new RpcTcpClient(new InetSocketAddress(uri.getHost(), uri.getPort()), this.executorService);
        } catch (URISyntaxException e) {
	    logger.error(String.format("%s is not a valid uri", endpoint));
            throw new IllegalArgumentException("invalid uri for endpoint");
        }
    }

}
