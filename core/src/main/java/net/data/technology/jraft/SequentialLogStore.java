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
 * 顺序日志存储
 */
public interface SequentialLogStore {

    /**
     * The first available index of the store, starts with 1 <br/>
     * (当前第一个可用的索引 起始值为1)
     * 
     * @return value &gt;= 1
     */
    public long getFirstAvailableIndex();

    /**
     * The start index of the log store, at the very beginning, it must be 1
     * however, after some compact actions, this could be anything greater or equals to one<br/>
     * (日志存储的开始索引)
     * @return start index of the log store
     */
    public long getStartIndex();

    /**
     * The last log entry in store<br/>
     * (最后存储的日志)
     * 
     * @return a dummy constant entry with value set to null and term set to zero if no log entry in store<br/>
     *         (非空)
     */
    public LogEntry getLastLogEntry();

    /**
     * Appends a log entry to store<br/>
     * (追加日志至存储)
     * 
     * @param logEntry log entry to append
     * @return the last appended log index<br/>
     *         (日志所在的索引)
     */
    public long append(LogEntry logEntry);

    /**
     * Over writes a log entry at index of {@code index}<br/>
     * (将日志写至对应索引处)
     * 
     * @param index a value &lt; {@code this.getFirstAvailableIndex()}, and starts from 1
     * @param logEntry log entry to write
     */
    public void writeAt(long index, LogEntry logEntry);

    /**
     * Get log entries with index between {@code start} and {@code end}<br/>
     * (根据起始位置返回日志集合,注:读不到将抛错)
     * 
     * @param start the start index of log entries
     * @param end the end index of log entries (exclusive)
     * @return the log entries between [start, end)
     */
    public LogEntry[] getLogEntries(long start, long end);

    /**
     * Gets the log entry at the specified index<br/>
     * (返回对应索引处日志)
     * 
     * @param index starts from 1
     * @return the log entry or null if index &gt;= {@link #getFirstAvailableIndex()}<br/>
     *         (可能为null)
     */
    public LogEntry getLogEntryAt(long index);

    /**
     * Pack {@code itemsToPack} log items starts from {@code index}<br/>
     * (从指定索引处打包对应数目的日志数据)
     * 
     * @param index index of the log store to start
     * @param itemsToPack items to pack
     * @return log pack
     */
    public byte[] packLog(long index, int itemsToPack);

    /**
     * Apply the log pack to current log store, starting from index<br/>
     * (从指定索引处应用日志数据)
     * 
     * @param index the log index that start applying the logPack, index starts from 1
     * @param logPack log pack to apply
     */
    public void applyLogPack(long index, byte[] logPack);

    /**
     * Compact the log store by removing all log entries including the log at the lastLogIndex<br/>
     * (通过删除所有 最后日志索引处 及之前的 日志条目 来压缩日志存储)
     * 
     * @param lastLogIndex pack until the last log index
     * @return compact successfully or not
     */
    public boolean compact(long lastLogIndex);
}
