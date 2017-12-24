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
 * Log entry, which represents an entry stored in the sequential log store replication is done
 * through LogEntry objects<br/>
 * 日志记录对象
 * 
 * @author Data Technology LLC
 *
 */
public class LogEntry {
    /**
     * 日志记录实际内容
     */
    private byte[] value;
    /**
     * 任期
     */
    private long term;
    /**
     * 日志类型
     */
    private LogValueType valueType;
    
    /**
     * 0,null 为入参调用{@link #LogEntry(long, byte[])}
     */
    public LogEntry(){
        this(0, null);
    }
    /**
     * 根据参数创建 {@link LogValueType#Application }应用程序相关 类型的日志对象
     * @param term
     * @param value
     */
    public LogEntry(long term, byte[] value){
        this(term, value, LogValueType.Application);
    }
    /**
     * 根据参数创建日志对象
     * @param term
     * @param value
     * @param valueType
     */
    public LogEntry(long term, byte[] value, LogValueType valueType){
        this.term = term;
        this.value = value;
        this.valueType = valueType;
    }



    /**
     * @return {@link #value} 的值
     */
    public byte[] getValue() {
        return value;
    }

    /**
     * @return {@link #term} 的值
     */
    public long getTerm() {
        return term;
    }


    /**
     * @return {@link #valueType} 的值
     */
    public LogValueType getValueType() {
        return valueType;
    }

    /**
     * 
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LogEntry [term=" + term + ", valueType=" + valueType + ", value="
                + (value != null ? value.length : "null") + "]";
    }
}
