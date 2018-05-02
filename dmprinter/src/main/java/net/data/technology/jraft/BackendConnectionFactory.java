package net.data.technology.jraft;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.NetworkChannel;

/**
 * @author hotdb
 */
public class BackendConnectionFactory {

    private AsynchronousSocketChannel openSocketChannel() throws IOException {

	AsynchronousChannelGroup asynchronousChannelGroup = AIOAcceptors.getInstance().getAsynchronousChannelGroup();
        AsynchronousSocketChannel channel =
                AsynchronousSocketChannel.open(asynchronousChannelGroup);
        channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, false);
        channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);

        return channel;
    }

    /**
     * 根据参数创建后端连接对象
     * 
     * @param mysqlConfig
     * @param connectionVars 当前被创建连接的 连接变量对象(独享){@link ConnectionVars#getUserConfig()} 不能为null
     * @param handler
     * @param verion 连接版本
     * @return
     * @throws IOException
     */
    public BackendConnection createNewConnection(MySQLDataSource mysqlDataSource,
	    FrontendConnection frontendConnection) throws IOException {
	NetworkChannel channel = openSocketChannel();
	BackendConnection backendConnection = new BackendConnection(channel);
	((AsynchronousSocketChannel) channel).connect(
		new InetSocketAddress(mysqlDataSource.getMysqlInfo().getIp(), mysqlDataSource.getMysqlInfo().getPort()),
		new Tuple2<FrontendConnection, BackendConnection>(frontendConnection, backendConnection),
		AIOAcceptors.getInstance().getAIOConnector());
	return backendConnection;
    }

}
