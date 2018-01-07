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

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.util.function.BiConsumer;
/**
 * 异步工具类
 */
public class AsyncUtility {
	/**
	 * 生成异步消息完成处理对象
	 * @param completed 成功 处理 函数式接口对象
	 * @param failed 失败 处理 函数式接口对象
	 * @return 异步消息完成处理对象 not null
	 */
    public static <V, A> CompletionHandler<V, A> handlerFrom(BiConsumer<V, A> completed, BiConsumer<Throwable, A> failed) {
        return new CompletionHandler<V, A>() {
            @Override
            public void completed(V result, A attachment) {
                completed.accept(result, attachment);
            }

            @Override
            public void failed(Throwable exc, A attachment) {
                failed.accept(exc, attachment);
            }
        };
    }
    /**
     * 从通道中读取数据<br>
     * <b>注:本方法 连接关闭 或 Buffer 读满 才调用完成，即:通道不关闭的情况下，将 继续读满buffer 否则持续等待</b>
     * @param channel
     * @param buffer 读取的buffer
     * @param attachment 附件对象
     * @param completionHandler 读操作完成后回调 完成处理对象
     */
    public static <A> void readFromChannel(AsynchronousByteChannel channel, ByteBuffer buffer, A attachment, CompletionHandler<Integer, A> completionHandler){
        try{
            channel.read(
                    buffer,
                    new AsyncContext<A>(attachment, completionHandler), // 再次封装 作为附件对象
                    handlerFrom(
                    (Integer result, AsyncContext<A> a) -> {
                        int bytesRead = result.intValue();
                        if(bytesRead == -1 || !buffer.hasRemaining()){// 连接关闭 或 Buffer 收完 才调用完成 否则 继续读满buffer
                            a.completionHandler.completed(buffer.position(), a.attachment);
                        }else{
                            readFromChannel(channel, buffer, a.attachment, a.completionHandler);
                        }
                    },
                    (Throwable error, AsyncContext<A> a) -> {
                        a.completionHandler.failed(error, a.attachment);
                    }));
        }catch(Throwable exception){
            completionHandler.failed(exception, attachment);
        }
    }
    
    /**
     * 写数据至通道
     * <b>注:本方法 连接关闭 或 Buffer 发完 才调用完成，即:通道不关闭的情况下，将 继续把buffer发完 </b>
     * @param channel
     * @param buffer 需写的数据
     * @param attachment 附件对象
     * @param completionHandler 写操作完成后回调 完成处理对象
     */
    public static <A> void writeToChannel(AsynchronousByteChannel channel, ByteBuffer buffer, A attachment, CompletionHandler<Integer, A> completionHandler){
        try{
            channel.write(
                    buffer,
                    new AsyncContext<A>(attachment, completionHandler), // 再次封装 作为附件对象
                    handlerFrom(
                    (Integer result, AsyncContext<A> a) -> {
                        int bytesRead = result.intValue();
                        if(bytesRead == -1 || !buffer.hasRemaining()){
                            a.completionHandler.completed(buffer.position(), a.attachment);
                        }else{
                            writeToChannel(channel, buffer, a.attachment, a.completionHandler);
                        }
                    },
                    (Throwable error, AsyncContext<A> a) -> {
                        a.completionHandler.failed(error, a.attachment);
                    }));
        }catch(Throwable exception){
            completionHandler.failed(exception, attachment);
        }
    }
    /**
     * 异步操作内容类(用于扩展作为 通信相关附件对象)
     */
    static class AsyncContext<A>{
    	/**
    	 * 附件对象
    	 */
        private A attachment;
        /**
         * 异步操作完成对象
         */
        private CompletionHandler<Integer, A> completionHandler;
        
        /**
         * 
         * 根据参数构造 类{@link AsyncContext} 对象
         * @param attachment
         * @param handler
         */
        public AsyncContext(A attachment, CompletionHandler<Integer, A> handler){
            this.attachment = attachment;
            this.completionHandler = handler;
        }
    }
}
