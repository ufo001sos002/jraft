package net.data.technology.jraft;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NetworkChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象连接对象
 *
 *
 */
public abstract class AbstractConnection {
    static final Logger logger = LoggerFactory.getLogger(AbstractConnection.class);
    /**
     * 默认Buffer块大小
     */
    public static final int DEFAULT_BUFFER_CHUNK_SIZE = 1024 * 16;
    /**
     * Socket连接通道对象
     */
    private NetworkChannel channel;
    /**
     * 写队列
     */
    public final ConcurrentLinkedQueue<ByteBuffer> writeQueue = new ConcurrentLinkedQueue<ByteBuffer>();
    /**
     * 写通道
     */
    private final AIOSocketWR socketWR;
    /**
     * 连接Id
     */
    private long id;
    /**
     * 通道读buffer
     */
    public volatile ByteBuffer readBuffer;
    /**
     * 通道写buffer
     */
    public volatile ByteBuffer writeBuffer;
    /**
     * true 为已关闭
     */
    public AtomicBoolean isClosed = new AtomicBoolean();

    public AbstractConnection(NetworkChannel channel) {
	this.channel = channel;
	this.socketWR = new AIOSocketWR(this);
    }

    /**
     * @return {@link #channel} 的值
     */
    public NetworkChannel getChannel() {
	return channel;
    }

    /**
     * @return {@link #id} 的值
     */
    public long getId() {
	return id;
    }

    /**
     * @param id
     *            根据 id 设置 {@link #id}的值
     */
    public void setId(long id) {
	this.id = id;
    }

    /**
     * 关闭socket，清理buffer
     */
    public void closeSocket() {
	if (channel != null) {
	    boolean isSocketClosed = true;
	    try {
		channel.close();
	    } catch (Throwable e) {
		if (logger.isInfoEnabled()) {
		    logger.info(Markers.CONNECTION, "close socket exception " + this);
		}
	    }
	    boolean closed = isSocketClosed && (!channel.isOpen());
	    if (closed == false) {
		logger.warn(Markers.CONNECTION, "close socket of connnection failed " + this);
	    }
	}
    }

    /**
     * 关闭方法
     */
    public abstract void close(String reason);

    /**
     * 构建Buffer
     * 
     * @return
     */
    public ByteBuffer allocate() {
	return ByteBuffer.allocate(DEFAULT_BUFFER_CHUNK_SIZE);
    }

    public final void write(ByteBuffer buffer) {
	synchronized (writeQueue) {
	    writeQueue.offer(buffer);
	}
	// if ansyn write finishe event got lock before me ,then writing
	// flag is set false but not start a write request
	// so we check again
	try {
	    this.socketWR.doNextWriteCheck();
	} catch (Exception e) {
	    logger.warn(Markers.CONNECTION, "write err:", e);
	    this.close("write err:" + e);

	}
    }

    /**
     * 当前该读方法 仅针对前端连接对象
     * 
     * @param got
     * @throws IOException
     */
    public abstract void onReadData(int got);

    public void asynRead() throws IOException {
	this.socketWR.asynRead();
    }

    public void doNextWriteCheck() throws IOException {
	this.socketWR.doNextWriteCheck();
    }

}
