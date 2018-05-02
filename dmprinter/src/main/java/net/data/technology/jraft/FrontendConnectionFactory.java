package net.data.technology.jraft;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.NetworkChannel;

/**
 * @author hotdb
 */
public class FrontendConnectionFactory {
    private IdGenerator idGenerator = new IdGenerator();
    /**
     * 实例
     */
    private RDSInstance rdsInstance;
    /**
     * 后端数据源工厂
     */
    private BackendConnectionFactory backendConnectionFactory;

    /**
     * <p>
     * Description:
     * </p>
     * 
     * @param rdsInstance
     */
    public FrontendConnectionFactory(RDSInstance rdsInstance) {
	super();
	this.rdsInstance = rdsInstance;
	this.backendConnectionFactory = new BackendConnectionFactory();
    }
    public FrontendConnection make(NetworkChannel channel) throws IOException {
        channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
	FrontendConnection c = new FrontendConnection(channel, rdsInstance);
	backendConnectionFactory.createNewConnection(rdsInstance.getCurrentDataSource(), c);
	c.setId(getIdGenerator().getId());
	rdsInstance.getFrontendConnMap().put(c.getId(), c);
	c.asynRead();
	return c;
    }
    /**
     * @return {@link #idGenerator} 的值
     */
    public IdGenerator getIdGenerator() {
        return idGenerator;
    }
}
