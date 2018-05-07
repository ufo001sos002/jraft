package net.data.technology.jraft;

import java.nio.ByteBuffer;
import java.nio.channels.NetworkChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.data.technology.jraft.tools.Tools;

/**
 * 前端连接对象
 *
 */
public class FrontendConnection extends AbstractConnection {
    private static final Logger logger = LoggerFactory.getLogger(FrontendConnection.class);
    /**
     * 后端连接对象
     */
    private AbstractConnection backendConnection;
    /**
     * 实例
     */
    private RDSInstance rdsInstance;

    /**
     * <p>
     * Description: 构造{@link FrontendConnection} 对象
     * </p>
     * 
     * @param channel
     */
    public FrontendConnection(NetworkChannel channel, RDSInstance rdsInstance) {
	super(channel);
	this.rdsInstance = rdsInstance;
    }


    /**
     * 
     * @param reason
     * @see net.data.technology.jraft.AbstractConnection#close(java.lang.String)
     */
    @Override
    public void close(String reason) {
	if (isClosed.compareAndSet(false, true)) {
	    rdsInstance.getFrontendConnMap().remove(getId());
	    closeSocket();
	    if (backendConnection != null) {
		backendConnection.close(reason);
	    }
	}
    }

    /**
     * 
     * @param got
     * @see net.data.technology.jraft.AbstractConnection#onReadData(int)
     */
    @Override
    public void onReadData(int got) {
	while (backendConnection == null) {
	    Tools.sleep(10); // 等待后端连接建立
	}
	ByteBuffer buffer = readBuffer;
	readBuffer = allocate();
	backendConnection.write(buffer);
    }

    /**
     * @return {@link #backendConnection} 的值
     */
    public AbstractConnection getBackendConnection() {
	return backendConnection;
    }

    /**
     * @param backendConnection
     *            根据 backendConnection 设置 {@link #backendConnection}的值
     */
    public void setBackendConnection(AbstractConnection backendConnection) {
	this.backendConnection = backendConnection;
    }

}
