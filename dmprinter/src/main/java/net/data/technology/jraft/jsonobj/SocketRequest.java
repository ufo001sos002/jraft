package net.data.technology.jraft.jsonobj;

public class SocketRequest {
    /**
     * 任务唯一Id, RS响应必须带该字段值
     */
    private String taskId;

    public String getTaskId() {
        // if (taskId == null)
        // taskId = UUID.randomUUID().toString();
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
