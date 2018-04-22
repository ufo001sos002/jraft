package net.data.technology.jraft;

import java.util.UUID;

import com.alibaba.fastjson.JSON;

public class SocketRequest {
    /**
     * 任务唯一Id, RS响应必须带该字段值
     */
    private String taskId;

    private String token = "hcm_web";

    public SocketRequest() {}

    public SocketRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTaskId() {
        if (taskId == null)
            taskId = UUID.randomUUID().toString();
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
