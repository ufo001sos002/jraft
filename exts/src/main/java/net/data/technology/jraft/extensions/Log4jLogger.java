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

import org.apache.log4j.Logger;

/**
 * 实现自定义接口类 {@link net.data.technology.jraft.Logger}
 *
 */
public class Log4jLogger implements net.data.technology.jraft.Logger {
    /**
     * 原生日志对象
     */
    private Logger logger;

    /**
     * 
     * <p>
     * Description: 根据原生日志对象 生成自定义日志对象
     * </p>
     * 
     * @param logger
     *            原生日志对象
     */
    public Log4jLogger(Logger logger){
        this.logger = logger;
    }

    @Override
    public void debug(String format, Object... args) {
        if (this.logger.isDebugEnabled()) {
	    this.logger.debug(String.format(format, args));
        }
    }

    @Override
    public void info(String format, Object... args) {
        if (this.logger.isInfoEnabled()) {
	    this.logger.info(String.format(format, args));
        }
    }

    @Override
    public void warning(String format, Object... args) {
	this.logger.warn(String.format(format, args));
    }

    @Override
    public void error(String format, Object... args) {
	this.logger.error(String.format(format, args));
    }

    @Override
    public void error(String format, Throwable error, Object... args) {
	this.logger.error(String.format(format, args), error);

    }

}
