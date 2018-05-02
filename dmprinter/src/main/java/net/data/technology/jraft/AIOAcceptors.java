package net.data.technology.jraft;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AIO IP:PORT 监听 启停类
 */
public final class AIOAcceptors {
    private static final Logger logger = LoggerFactory.getLogger(AIOAcceptors.class);
    private static final int DEFAULT_SOCKET_BACKLOG = 1000;
    /**
     * 监听(ip:port)集合
     */
    private final ConcurrentHashMap<Tuple2<String, Integer>, AsynchronousServerSocketChannel> serverSocketChannelMap;
    /**
     * 异步通道组数组
     */
    private final AsynchronousChannelGroup[] asyncChannelGroups;
    /**
     * 当前异步通道组 索引位置
     */
    private int asyncChannelGroupIndex = 0;
    /**
     * 单例对象
     */
    private static AIOAcceptors instance = new AIOAcceptors();
    /**
     * 后端连接相关
     */
    private AIOConnector aioConnector = null;

    /**
     * 构造 AIO监听启停类对象
     * 
     * @param newConfig 配置对象
     * @throws IOException 启动失败
     */
    private AIOAcceptors() {
        this.serverSocketChannelMap =
                new ConcurrentHashMap<Tuple2<String, Integer>, AsynchronousServerSocketChannel>();
        asyncChannelGroups =
		new AsynchronousChannelGroup[Middleware.getExecutorSize()];
	aioConnector = new AIOConnector();
    }

    /**
     * 初始化环境
     * 
     * @throws IOException
     */
    public void init() throws IOException {
	for (int i = 0; i < asyncChannelGroups.length; i++) {
	    asyncChannelGroups[i] = AsynchronousChannelGroup.withThreadPool(Executors.newCachedThreadPool());
        }
    }

    /**
     * 获取类单例对象
     * 
     * @return
     */
    public static AIOAcceptors getInstance() {
	return instance;
    }

    /**
     * @return {@link #aioConnector} 的值
     */
    public AIOConnector getAIOConnector() {
	return aioConnector;
    }

    /**
     * 接受前端Socket连接，并进行处理后续处理
     * 
     * @param channel 前端连接Socket对象
     * @param factory 前端侦听服务对应的工厂类对象
     */
    private void accept(AsynchronousSocketChannel channel, FrontendConnectionFactory factory) {
        try {
	    factory.make(channel);
        } catch (Throwable e) {
            logger.error(Markers.SERVERLISTEN, "when accept a channel " + channel, e);
            closeChannel(channel);
        }
    }

    /**
     * 异步等待 前端连接IP:PORT
     * 
     * @param serverChannel 监听的服务器通道对象
     * @param handler 处理连接监听响应的的处理对象
     */
    private boolean pendingAccept(AsynchronousServerSocketChannel serverChannel,
            FrontendConnectionFactory factory, AIOAcceptHandler handler) {
        try {
            if (serverChannel.isOpen()) {
                serverChannel.accept(factory, handler);
                return true;
            }
        }catch(Throwable e) {
            logger.error(Markers.SERVERLISTEN, "when accept a serverChannel" + serverChannel
                    + " is error" + e.getMessage() + ", Server Listen will close!", e);
            closeServerChannel(serverChannel);
            // TODO 通知HCM 监听异常关闭
        }
        return false;
    }

    /**
     * 异常情况下用于关闭前端连接
     * 
     * @param channel 前端连接对象
     */
    private void closeChannel(AsynchronousSocketChannel channel) {
        if (channel == null) {
            return;
        }
        try {
            channel.close();
        } catch (IOException e) {
        }
    }


    /**
     * 
     * @return AsynchronousChannelGroup 对象
     */
    public AsynchronousChannelGroup getAsynchronousChannelGroup() {
        if (asyncChannelGroups.length == 1) {
            return asyncChannelGroups[0];
        } else {
            AsynchronousChannelGroup asyncChannelGroup = null;
            synchronized (asyncChannelGroups) {
                asyncChannelGroup = asyncChannelGroups[asyncChannelGroupIndex++];
                if (asyncChannelGroupIndex == asyncChannelGroups.length) {
                    asyncChannelGroupIndex = 0;
                }
            }
            return asyncChannelGroup;
        }
    }
    
    /**
     * 
     * @return 前端连接工厂对象
     */
    private FrontendConnectionFactory createFrontendConnectionFactory(RDSInstance rdsInstance) {
	return new FrontendConnectionFactory(rdsInstance);
    }
    
    /**
     * 监控指定IP 和 端口 并由指定连接工厂对象进行封装
     * 
     * @param ip 监听IP
     * @param port 监听端口
     * @param factory 连接工厂对象
     * @return true 增加成功 false增加失败
     */
    private boolean addServerListen(String ip, int port, FrontendConnectionFactory factory) {
        AsynchronousServerSocketChannel serverChannel = null;
        try {
            serverChannel = AsynchronousServerSocketChannel.open(getAsynchronousChannelGroup());
            /** 设置TCP属性 */
            serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            // serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 16 * 1024);
            InetSocketAddress inetSocketAddress = null;
            if (ip == null) {
                inetSocketAddress = new InetSocketAddress(port);
            } else {
                inetSocketAddress = new InetSocketAddress(ip, port);
            }
	    serverChannel.bind(inetSocketAddress);
            if (pendingAccept(serverChannel, factory, new AIOAcceptHandler(serverChannel))) {
                serverSocketChannelMap.put(new Tuple2<String, Integer>(ip, port),
                        serverChannel);
                return true;
            }
            // try {
            // // 进行再次绑定验证如果可以绑定说明之前未绑定成功监听失败
            // serverChannel.bind(inetSocketAddress);
            // return false;
            // } catch (Exception e) {
            // }
        } catch (Exception e) {
            logger.error(Markers.SERVERLISTEN, "addServerListen(" + ip + "," + port + ") is Error",
                    e);
            closeServerChannel(serverChannel);
        }
        return false;
    }


    public boolean addServerListen(RDSInstance rdsInstance) {
	return addServerListen(rdsInstance.getRdsInstanceInfo().getVip(), rdsInstance.getRdsInstanceInfo().getPort(),
		createFrontendConnectionFactory(rdsInstance));
    }
    
    /**
     * 关闭服务serverChannel对象
     * 
     * @param serverChannel
     */
    private void closeServerChannel(AsynchronousServerSocketChannel serverChannel) {
        if (serverChannel != null) {
            try {
                serverChannel.close();
            } catch (Exception e) {
            }
        }
    }


    public void closeServerListen(String ip, int port, String reason) {
        try {
            AsynchronousServerSocketChannel serverChannel =
                    serverSocketChannelMap.remove(new Tuple2<String, Integer>(ip, port));
            if (logger.isInfoEnabled()) {
                logger.info(Markers.SERVERLISTEN, "HotDB Cloud Server Socket Listen(" + ip + ","
                        + port + ") close due to:" + reason);
            }
            closeServerChannel(serverChannel);
        } catch (Exception e) {
            logger.error(Markers.SERVERLISTEN,
                    "closeServerListen(" + ip + "," + port + ") is Error", e);
        }
    }



    /**
     * AIO 异步 连接请求处理
     */
    private class AIOAcceptHandler
            implements CompletionHandler<AsynchronousSocketChannel, FrontendConnectionFactory> {

        /**
         * 异步服务器对象
         */
        private final AsynchronousServerSocketChannel serverChannel;

        protected AIOAcceptHandler(AsynchronousServerSocketChannel serverChannel) {
            this.serverChannel = serverChannel;
        }

        @Override
        public void completed(AsynchronousSocketChannel result, FrontendConnectionFactory factory) {
            accept(result, factory);
            // next pending waiting
            pendingAccept(serverChannel, factory, this);

        }

        @Override
        public void failed(Throwable exc, FrontendConnectionFactory factory) {
            if (serverChannel.isOpen()) {
                if (logger.isInfoEnabled()) {
                    logger.info(Markers.SERVERLISTEN, "acception connect failed:" + exc);
                }
            }
            // next pending waiting
            pendingAccept(serverChannel, factory, this);
        }
    }
}
