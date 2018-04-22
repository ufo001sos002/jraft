package net.data.technology.jraft;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * SSL连接客户端 保持长连
 */
public interface SSLClient {
    /**
     * 开始与IP 端口 进行通信
     * 
     * @param remoteAddress
     * @param port
     * @throws IOException
     */
    public void start(String remoteAddress, int port) throws IOException;

    /**
     * 往连接中发送数据 当发送失败 / 异常时，由调用者确定后续处理(是否重新发送等)
     * 
     * @param myAppData 需发送的数据Buffer
     * @return 发送成功与否 true成功
     * @throws IOException 连接通道异常
     */
    public boolean write(ByteBuffer myAppData) throws IOException;

    /**
     * 关闭连接以及处理线程池
     * 
     * @throws IOException
     */
    public void shutdown() throws IOException;

    /**
     * 对收到的数据包进行处理
     * 
     * @param data
     */
    public void handle(byte[] data);

    /**
     * 设置心跳时间(intervalTime)参数发送心跳包(下次生效)
     * 
     * @param intervalTime 毫秒
     */
    public void setHeartbeatInterval(long intervalTime);
}
