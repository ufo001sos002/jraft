package net.data.technology.jraft;

import com.alibaba.fastjson.JSON;
public class TaskResponse {
    public String id; // 操作的唯一Id，这里设置任务ID
    /**
     * 令牌，这里传RDS SERVER ID
     */
    private String token;
    public int code; // 状态码，0：正常，非零：异常
    public Object message; // 消息描述
    public Integer flag; // 标识
    public Object data; // 携带的数据
    /**
     * 临时传输
     */
    public Object backend;

    public TaskResponse() {
	token = Middleware.getMiddleware().getRdsServerId();
    }

    public TaskResponse(String id) {
        this.id = id;
    }

    public TaskResponse(String id, int flag) {
        this.id = id;
        this.flag = flag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    /**
     * @return {@link #token} 的值
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token 根据 token 设置 {@link #token}的值
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return {@link #backend} 的值
     */
    public Object getBackend() {
        return backend;
    }

    /**
     * @param backend 根据 backend 设置 {@link #backend}的值
     */
    public void setBackend(Object backend) {
        this.backend = backend;
    }

}
