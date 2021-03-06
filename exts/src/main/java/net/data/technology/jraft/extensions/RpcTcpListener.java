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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.data.technology.jraft.RaftMessageHandler;
import net.data.technology.jraft.RaftRequestMessage;
import net.data.technology.jraft.RaftResponseMessage;
import net.data.technology.jraft.RpcListener;

/**
 * RPC Server端 TCP 监听服务类 实现 {@link RpcListener} 接口类
 *
 */
public class RpcTcpListener implements RpcListener {
    /**
     * 日志对象
     */
    private static final Logger logger = LoggerFactory.getLogger(RpcTcpListener.class);
    /**
     * RPC TCP 监听端口
     */
    private int port;
    /**
     * RPC TCP Server 通道对象
     */
    private AsynchronousServerSocketChannel listener;
    /**
     * TCP 异步通信 所用 连接池
     */
    private ExecutorService executorService;
    /**
     * 访问本RPC Server的 所有 RPC TCP socket通道 对象集合 , 可用于主动关闭连接
     */
    private List<AsynchronousSocketChannel> connections;
    
    /**
     * 
     * 根据参数构造 类{@link RpcTcpListener} 对象
     * 
     * @param port
     *            本机RPC TCP 监听端口
     * @param executorService
     *            连接池对象 为本对象独占, {@link #stop()} 将关闭该连接池,因此，其他地方不能使用
     */
    public RpcTcpListener(int port, ExecutorService executorService){
        this.port = port;
        this.executorService = executorService;
        this.connections = Collections.synchronizedList(new LinkedList<AsynchronousSocketChannel>());
    }

    @Override
    public void startListening(RaftMessageHandler messageHandler) {
        try{
            AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withThreadPool(this.executorService);
            this.listener = AsynchronousServerSocketChannel.open(channelGroup);
            this.listener.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            this.listener.bind(new InetSocketAddress(this.port));
            this.acceptRequests(messageHandler);
        }catch(IOException exception){
            logger.error("failed to start the listener due to io error", exception);
        }
    }

    @Override
    public void stop(){
        for(AsynchronousSocketChannel connection : this.connections){ // 关闭所有连接
            try{
                connection.close();
            }catch(IOException error){
                logger.info("failed to close connection, but it's fine", error);
            }
        }

        if(this.listener != null){ // 停止RPC Server TCP监听服务
            try {
                this.listener.close();
            } catch (IOException e) {
                logger.info("failed to close the listener socket", e);
            }

            this.listener = null;
        }

        if(this.executorService != null){ // 关闭连接池
            this.executorService.shutdown();
            this.executorService = null;
        }
    }
    /**
     * 根据Raft消息处理对象 进行RPC TCP 消息处理<br>  
     * (先接受连接 后开始读操作 {@link #readRequest(AsynchronousSocketChannel, RaftMessageHandler)} )
     * @param messageHandler
     */
    private void acceptRequests(RaftMessageHandler messageHandler){
        try{
            this.listener.accept(messageHandler, AsyncUtility.handlerFrom(
                    (AsynchronousSocketChannel connection, RaftMessageHandler handler) -> {
                        connections.add(connection);// 记录连接
                        acceptRequests(handler);
                        readRequest(connection, handler);
                    },
                    (Throwable error, RaftMessageHandler handler) -> {
                        logger.error("accepting a new connection failed, will still keep accepting more requests", error);
                        acceptRequests(handler);
                    }));
        }catch(Exception exception){
            logger.error("failed to accept new requests, will retry", exception);
            this.acceptRequests(messageHandler);
        }
    }
    /**
     * 读操作
     * @param connection socket通道对象 
     * @param messageHandler 消息处理对象
     */
    private void readRequest(final AsynchronousSocketChannel connection, RaftMessageHandler messageHandler){
        ByteBuffer buffer = ByteBuffer.allocate(BinaryUtils.RAFT_REQUEST_HEADER_SIZE);
        try{
            AsyncUtility.readFromChannel(connection, buffer, messageHandler, handlerFrom((Integer bytesRead, final RaftMessageHandler handler) -> {
                if(bytesRead.intValue() < BinaryUtils.RAFT_REQUEST_HEADER_SIZE){// 未满足此条件情况 只有 连接 断开 或收包异常  进入此方法前将一直等待收报完成
                	if(logger.isInfoEnabled()) {	
                		logger.info("failed to read the request header from client socket");
                	}
                    closeSocket(connection);
                }else{
                    try{
                    	if(logger.isDebugEnabled()) {
                    		logger.debug("request header read, try to see if there is a request body");
                    	}
                    	// 读取Raft请求头数据
                        final Tuple4<RaftRequestMessage, Integer, Integer, Integer> requestInfo = BinaryUtils.bytesToRequestMessage(buffer.array());
                        ByteBuffer logBuffer = ByteBuffer.allocate(requestInfo._2() + requestInfo._3() + requestInfo._4());
                        AsyncUtility.readFromChannel(connection, logBuffer, null, handlerFrom((Integer size, Object attachment) -> {
                         // 未满足此条件情况 只有 连接 断开 或收包异常  进入此方法前将一直等待收报完成
			    if (size.intValue() < requestInfo._2() + requestInfo._3() + requestInfo._4()) {
                                if(logger.isInfoEnabled()) {
                                     logger.info("failed to read the log entries data from client socket");
                                }
                                closeSocket(connection);
                            }else{
                                try{
                                    logBuffer.flip();
                                    processRequest(connection, BinaryUtils.bufferToRequestMessage(requestInfo, logBuffer), handler);
                                }catch(Throwable error){
                                	if(logger.isInfoEnabled()) {
                                		logger.info("log entries parsing error", error);
                                	}
                                    closeSocket(connection);
                                }
                            }
                        }, connection));
                    }catch(Throwable runtimeError){
                        // if there are any conversion errors, we need to close the client socket to prevent more errors
                        closeSocket(connection);
                        if(logger.isInfoEnabled()) {
                        	logger.info("message reading/parsing error", runtimeError);
                        }
                    }
                }
            }, connection));
        }catch(Exception readError){
        	if(logger.isInfoEnabled()) {
        		logger.info("failed to read more request from client socket", readError);
        	}
            closeSocket(connection);
        }
    }
    /**
     * Raft消息处理对象 处理 请求消息 并发送回包
     * @param connection
     * @param request
     * @param messageHandler
     */
    private void processRequest(AsynchronousSocketChannel connection, RaftRequestMessage request, RaftMessageHandler messageHandler){
        try{
            RaftResponseMessage response = messageHandler.processRequest(request);
            final ByteBuffer buffer = ByteBuffer.wrap(BinaryUtils.messageToBytes(response));
            AsyncUtility.writeToChannel(connection, buffer, null, handlerFrom((Integer bytesSent, Object attachment) -> {
                if(bytesSent.intValue() < buffer.limit()){ // 进入此方法时 除非异常 或 连接关闭 否则 包已全部发完 符合该条件
                	if(logger.isInfoEnabled()) {
                		logger.info("failed to completely send the response.");
                	}
                    closeSocket(connection);
                }else{
                	if(logger.isDebugEnabled()) {
                		logger.debug("response message sent.");
                	}
                    if(connection.isOpen()){
				if (logger.isDebugEnabled())
				    logger.debug("try to read next request");
                        readRequest(connection, messageHandler); // 循环读包
                    }
                }
            }, connection));
        }catch(Throwable error){
            // for any errors, we will close the socket to prevent more errors
            closeSocket(connection);
            logger.error("failed to process the request or send the response", error);
        }
    }
    /**
     * 构造 连接操作失败则关闭连接 的 通用 异步操作完成对象
     * @param completed
     * @param connection
     * @return
     */
    private <V, A> CompletionHandler<V, A> handlerFrom(BiConsumer<V, A> completed, AsynchronousSocketChannel connection) {
        return AsyncUtility.handlerFrom(completed, (Throwable error, A attachment) -> {
                        try {
		if (logger.isInfoEnabled())
			    logger.info("socket("+(connection!= null ? connection.getLocalAddress()+","+connection.getRemoteAddress() : "")+") server failure", error);
			} catch (Exception e) {
			}
                        if(connection != null){
                            closeSocket(connection);
                        }
                    });
    }
    /**
     * 关闭连接 并从连接集合中移除该连接
     * @param connection
     */
    private void closeSocket(AsynchronousSocketChannel connection){
        try{
            this.connections.remove(connection);
            connection.close();
        }catch(IOException ex){
            logger.info("failed to close client socket", ex);
        }
    }
}
