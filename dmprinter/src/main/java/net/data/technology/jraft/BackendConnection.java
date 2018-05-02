package net.data.technology.jraft;

import java.nio.ByteBuffer;
import java.nio.channels.NetworkChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author X250
 * @date 2018年5月2日 下午6:20:37
 *
 */
public class BackendConnection extends AbstractConnection {
    private static final Logger logger = LoggerFactory.getLogger(FrontendConnection.class);
    /**
     * 后端连接对象
     */
    private AbstractConnection frontendConnection;

    /**
     * <p>
     * Description:
     * </p>
     * 
     * @param channel
     */
    public BackendConnection(NetworkChannel channel) {
	super(channel);
    }

    /**
     * 
     * @param reason
     * @see net.data.technology.jraft.AbstractConnection#close(java.lang.String)
     */
    @Override
    public void close(String reason) {
	if (isClosed.compareAndSet(false, true)) {
	    closeSocket();
	    if (frontendConnection != null) {
		frontendConnection.close(reason);
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
	if (frontendConnection != null) {
	    ByteBuffer buffer = readBuffer;
	    readBuffer = allocate();
	    frontendConnection.write(buffer);
	}
    }

    /**
     * @return {@link #frontendConnection} 的值
     */
    public AbstractConnection getFrontendConnection() {
	return frontendConnection;
    }

    /**
     * @param frontendConnection
     *            根据 frontendConnection 设置 {@link #frontendConnection}的值
     */
    public void setFrontendConnection(AbstractConnection frontendConnection) {
	this.frontendConnection = frontendConnection;
    }

}

