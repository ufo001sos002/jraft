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
 * 自定义日志类 接口类
 */
public interface Logger {
    /**
     * 自定义实现根据参数输出debug日志(必须保证不在该级别时不作输出,以免影响性能)
     * 
     * @param format
     *            格式化参数
     * @param args
     *            填入格式化内容 not null
     */
    public void debug(String format, Object... args);

    /**
     * 自定义实现根据参数输出info日志(必须保证不在该级别时不作输出,以免影响性能)
     * 
     * @param format
     *            格式化参数
     * @param args
     *            填入格式化内容 not null
     */
    public void info(String format, Object... args);

    /**
     * 自定义实现根据参数输出warning日志
     * 
     * @param format
     *            格式化参数
     * @param args
     *            填入格式化内容 not null
     */
    public void warning(String format, Object... args);

    /**
     * 自定义实现根据参数输出error日志
     * 
     * @param format
     *            格式化参数
     * @param args
     *            填入格式化内容 not null
     */
    public void error(String format, Object... args);

    /**
     * 自定义实现根据参数输出error日志
     * 
     * @param format
     *            格式化参数
     * @param error
     *            错误堆栈对象
     * @param args
     *            填入格式化内容 not null
     */
    public void error(String format, Throwable error, Object... args);
}
