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
 * Raft消息类型
 */
public enum RaftMessageType {
    /**
     * 投票请求
     */
    RequestVoteRequest {
        @Override
        public String toString() {
            return "RequestVoteRequest";
        }

        @Override
        public byte toByte() {
            return (byte) 1;
        }
    },
    /**
     * 投票请求响应
     */
    RequestVoteResponse {
        @Override
        public String toString() {
            return "RequestVoteResponse";
        }

        @Override
        public byte toByte() {
            return (byte) 2;
        }
    },
    /**
     * 追加记录请求
     */
    AppendEntriesRequest {
        @Override
        public String toString() {
            return "AppendEntriesRequest";
        }

        @Override
        public byte toByte() {
            return (byte) 3;
        }
    },
    /**
     * 追加记录响应
     */
    AppendEntriesResponse {
        @Override
        public String toString() {
            return "AppendEntriesResponse";
        }

        @Override
        public byte toByte() {
            return (byte) 4;
        }
    },
    /**
     * 客户端请求
     */
    ClientRequest {
        @Override
        public String toString() {
            return "ClientRequest";
        }

        @Override
        public byte toByte() {
            return (byte) 5;
        }
    },
    /**
     * 增加服务端请求
     */
    AddServerRequest {
        @Override
        public String toString() {
            return "AddServerRequest";
        }

        @Override
        public byte toByte() {
            return (byte) 6;
        }
    },
    /**
     * 增加服务端响应
     */
    AddServerResponse {
        @Override
        public String toString() {
            return "AddServerResponse";
        }

        @Override
        public byte toByte() {
            return (byte) 7;
        }
    },
    /**
     * 移除服务端请求
     */
    RemoveServerRequest {
        @Override
        public String toString(){
            return "RemoveServerRequest";
        }

        @Override
        public byte toByte(){
            return (byte)8;
        }
    },
    /**
     * 移除服务端响应
     */
    RemoveServerResponse {
        @Override
        public String toString(){
            return "RemoveServerResponse";
        }

        @Override
        public byte toByte(){
            return (byte)9;
        }
    },
    /**
     * 同步日志请求
     */
    SyncLogRequest {
        @Override
        public String toString(){
            return "SyncLogRequest";
        }

        @Override
        public byte toByte(){
            return (byte)10;
        }
    },
    /**
     * 同步日志响应
     */
    SyncLogResponse {
        @Override
        public String toString(){
            return "SyncLogResponse";
        }

        @Override
        public byte toByte(){
            return (byte)11;
        }
    },
    /**
     * 加入集群请求
     */
    JoinClusterRequest {
        @Override
        public String toString(){
            return "JoinClusterRequest";
        }

        @Override
        public byte toByte(){
            return (byte)12;
        }
    },
    /**
     * 加入集群响应
     */
    JoinClusterResponse {
        @Override
        public String toString(){
            return "JoinClusterResponse";
        }

        @Override
        public byte toByte(){
            return (byte)13;
        }
    },
    /**
     * 离开集群请求
     */
    LeaveClusterRequest {
        @Override
        public String toString(){
            return "LeaveClusterRequest";
        }

        @Override
        public byte toByte(){
            return (byte)14;
        }
    },
    /**
     * 离开集群响应
     */
    LeaveClusterResponse {
        @Override
        public String toString(){
            return "LeaveClusterResponse";
        }

        @Override
        public byte toByte(){
            return (byte)15;
        }
    },
    /**
     * 初始化快照请求
     */
    InstallSnapshotRequest {
        @Override
        public String toString(){
            return "InstallSnapshotRequest";
        }

        @Override
        public byte toByte(){
            return (byte)16;
        }
    },
    /**
     * 初始化快照响应
     */
    InstallSnapshotResponse {
        @Override
        public String toString(){
            return "InstallSnapshotResponse";
        }

        @Override
        public byte toByte(){
            return (byte)17;
        }
    };
    /**
     * 
     * 
     * @return 类型转换成对应byte值
     */
    public abstract byte toByte();

    /**
     * 
     * 
     * @param value
     * @return 根据对应byte值转换成 raft 消息枚举对象
     * @throws IllegalArgumentException 对应值 不存在
     */
    public static RaftMessageType fromByte(byte value) {
        switch (value) {
        case 1:
            return RequestVoteRequest;
        case 2:
            return RequestVoteResponse;
        case 3:
            return AppendEntriesRequest;
        case 4:
            return AppendEntriesResponse;
        case 5:
            return ClientRequest;
        case 6:
            return AddServerRequest;
        case 7:
            return AddServerResponse;
        case 8:
            return RemoveServerRequest;
        case 9:
            return RemoveServerResponse;
        case 10:
            return SyncLogRequest;
        case 11:
            return SyncLogResponse;
        case 12:
            return JoinClusterRequest;
        case 13:
            return JoinClusterResponse;
        case 14:
            return LeaveClusterRequest;
        case 15:
            return LeaveClusterResponse;
        case 16:
            return InstallSnapshotRequest;
        case 17:
            return InstallSnapshotResponse;
        }

        throw new IllegalArgumentException("the value for the message type is not defined");
    }
}
