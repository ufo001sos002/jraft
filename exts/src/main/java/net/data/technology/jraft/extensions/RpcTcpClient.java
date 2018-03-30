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
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.data.technology.jraft.RaftRequestMessage;
import net.data.technology.jraft.RaftResponseMessage;
import net.data.technology.jraft.RpcClient;

/**
 * RPC TCP 客户端 实现类 实现 {@link RpcClient} 接口类
 */
public class RpcTcpClient implements RpcClient {
    /**
     * 当前Socket通道对象
     */
    private AsynchronousSocketChannel connection;
    /**
     * 通道组 用于创建通道
     */
    private AsynchronousChannelGroup channelGroup;
    /**
     * 读任务队列
     */
    private ConcurrentLinkedQueue<AsyncTask<ByteBuffer>> readTasks;
    /**
     * 写任务队列
     */
    private ConcurrentLinkedQueue<AsyncTask<RaftRequestMessage>> writeTasks;
    /**
     * 当前待读任务计数
     */
    private AtomicInteger readers;
    /**
     * 当前待发送任务数
     */
    private AtomicInteger writers;
    /**
     * 需要进行RPC TCP 通讯的地址
     */
    private InetSocketAddress remote;
    /**
     * 原生日志对象
     */
    private Logger logger;

    /**
     * 
     * <p>
     * Description: 根据Raft Server地址对象、线程池 创建RPC TCP 客户端对象
     * </p>
     * 
     * @param remote
     *            需要进行RPC TCP 通讯的地址
     * @param executorService
     *            构建AIO连接的连接池
     */
    public RpcTcpClient(InetSocketAddress remote, ExecutorService executorService){
        this.remote = remote;
        this.logger = LogManager.getLogger(getClass());
        this.readTasks = new ConcurrentLinkedQueue<AsyncTask<ByteBuffer>>();
        this.writeTasks = new ConcurrentLinkedQueue<AsyncTask<RaftRequestMessage>>();
        this.readers = new AtomicInteger(0);
        this.writers = new AtomicInteger(0);
        try{
            this.channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
        }catch(IOException err){
            this.logger.error("failed to create channel group", err);
            throw new RuntimeException("failed to create the channel group due to errors.");
        }
    }

    @Override
    public synchronized CompletableFuture<RaftResponseMessage> send(final RaftRequestMessage request) {
        this.logger.debug(String.format("trying to send message %s to server %d at endpoint %s", request.getMessageType().toString(), request.getDestination(), this.remote.toString()));
        CompletableFuture<RaftResponseMessage> result = new CompletableFuture<RaftResponseMessage>();
        if (this.connection == null || !this.connection.isOpen()) { // 没有连接 则 先创建 后 进行 发送读操作
            try{
                this.connection = AsynchronousSocketChannel.open(this.channelGroup);
                this.connection.connect(this.remote, new AsyncTask<RaftRequestMessage>(request, result), handlerFrom((Void v, AsyncTask<RaftRequestMessage> task) -> {
                    sendAndRead(task, false);
                }));
            }catch(Throwable error){
                closeSocket();
                result.completeExceptionally(error);
            }
        }else{
            this.sendAndRead(new AsyncTask<RaftRequestMessage>(request, result), false);
        }

        return result;
    }
    
    /**
     * 发送和读数据
     * @param task 发送任务
     * @param skipQueueing 是否跳过队列 直接发送 true 跳过，否者加入发送队列
     */
    private void sendAndRead(AsyncTask<RaftRequestMessage> task, boolean skipQueueing){
        if(!skipQueueing){
            int writerCount = this.writers.getAndIncrement();
            if(writerCount > 0){ // 加入队列
                this.logger.debug("there is a pending write, queue this write task");
                this.writeTasks.add(task);
                return;
            }
        }

        ByteBuffer buffer = ByteBuffer.wrap(BinaryUtils.messageToBytes(task.input));
        try{
            AsyncUtility.writeToChannel(this.connection, buffer, task, handlerFrom((Integer bytesSent, AsyncTask<RaftRequestMessage> context) -> {
                if(bytesSent.intValue() < buffer.limit()){
                    logger.info("failed to sent the request to remote server.");
                    context.future.completeExceptionally(new IOException("Only partial of the data could be sent"));
                    closeSocket();
                }else{
                    // read the response
                    ByteBuffer responseBuffer = ByteBuffer.allocate(BinaryUtils.RAFT_RESPONSE_HEADER_SIZE); // 创建回包大小的buffer加入队列进行读操作
                    this.readResponse(new AsyncTask<ByteBuffer>(responseBuffer, context.future), false);
                }

                int waitingWriters = this.writers.decrementAndGet();
                if(waitingWriters > 0){ // 队列发送
                    this.logger.debug("there are pending writers in queue, will try to process them");
                    AsyncTask<RaftRequestMessage> pendingTask = null;
                    while((pendingTask = this.writeTasks.poll()) == null);
                    this.sendAndRead(pendingTask, true);
                }
            }));
        }catch(Exception writeError){
            logger.info("failed to write the socket", writeError);
            task.future.completeExceptionally(writeError);
            closeSocket();
        }
    }
    
    /**
     * 读取回包数据
     * 
     * @param task
     *            异步任务对象
     * @param skipQueueing
     *            是否跳过队列直接进行操作 true 跳过
     */
    private void readResponse(AsyncTask<ByteBuffer> task, boolean skipQueueing){
        if(!skipQueueing){
            int readerCount = this.readers.getAndIncrement();
            if(readerCount > 0){
                this.logger.debug("there is a pending read, queue this read task");
                this.readTasks.add(task);
                return;
            }
        }

        CompletionHandler<Integer, AsyncTask<ByteBuffer>> handler = handlerFrom((Integer bytesRead, AsyncTask<ByteBuffer> context) -> {
            if(bytesRead.intValue() < BinaryUtils.RAFT_RESPONSE_HEADER_SIZE){ // 数据未收取全
                logger.info("failed to read response from remote server.");
                context.future.completeExceptionally(new IOException("Only part of the response data could be read"));
                closeSocket();
            }else{
                RaftResponseMessage response = BinaryUtils.bytesToResponseMessage(context.input.array());
                context.future.complete(response);
            }

            int waitingReaders = this.readers.decrementAndGet();
            if(waitingReaders > 0){
                this.logger.debug("there are pending readers in queue, will try to process them");
                AsyncTask<ByteBuffer> pendingTask = null;
                while((pendingTask = this.readTasks.poll()) == null);
                this.readResponse(pendingTask, true);
            }
        });

        try{
            this.logger.debug("reading response from socket...");
            AsyncUtility.readFromChannel(this.connection, task.input, task, handler);
        }catch(Exception readError){
            logger.info("failed to read from socket", readError);
            task.future.completeExceptionally(readError);
            closeSocket();
        }
    }
    
    /**
     * 生成异步操作结果处理对象(失败处理函数式接口对象已固定)
     * @param completed 成功处理对象
     * @return 异步操作结果处理对象(失败处理函数式接口对象已固定
     */
    private <V, I> CompletionHandler<V, AsyncTask<I>> handlerFrom(BiConsumer<V, AsyncTask<I>> completed) {
        return AsyncUtility.handlerFrom(completed, (Throwable error, AsyncTask<I> context) -> {
                        this.logger.info("socket error", error);
                        context.future.completeExceptionally(error);
                        closeSocket();
                    });
    }

    private synchronized void closeSocket(){
        this.logger.debug("close the socket due to errors");
        try{
            if(this.connection != null){
                this.connection.close();
                this.connection = null;
            }
        }catch(IOException ex){
            this.logger.info("failed to close socket", ex);
        }

        while(true){
            AsyncTask<ByteBuffer> task = this.readTasks.poll();
            if(task == null){
                break;
            }

            task.future.completeExceptionally(new IOException("socket is closed, no more reads can be completed"));
        }

        while(true){
            AsyncTask<RaftRequestMessage> task = this.writeTasks.poll();
            if(task == null){
                break;
            }

            task.future.completeExceptionally(new IOException("socket is closed, no more writes can be completed"));
        }

        this.readers.set(0);
        this.writers.set(0);
    }

    static class AsyncTask<TInput>{
        private TInput input;
        private CompletableFuture<RaftResponseMessage> future;

        public AsyncTask(TInput input, CompletableFuture<RaftResponseMessage> future){
            this.input = input;
            this.future = future;
        }
    }
}
