package net.data.technology.jraft;


import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.data.technology.jraft.jsonobj.HCSClusterAllConfig;

/**
 * SSL连接客户端 保持长连
 */
public class NIOSSLClient implements SSLClient {
    private static final Logger logger = LoggerFactory.getLogger(NIOSSLClient.class);
    /**
     * 连接到指定IP
     */
    private String remoteAddress;
    /**
     * 连接到指定端口
     */
    private int port;

    /**
     * SSLEngine 对象
     */
    private SSLEngine engine;

    /**
     * SocketChannel 对象
     */
    private SocketChannel socketChannel;

    /**
     * 业务数据 已加密
     */
    private volatile ByteBuffer myNetData;

    /**
     * 缓冲区已返回业务数据 已解密
     */
    private volatile ByteBuffer peerAppData;
    /**
     * 缓冲区待发送业务数据 已加密
     */
    private volatile ByteBuffer peerNetData;
    /**
     * 处理线程池
     */
    private ExecutorService executor;// = Executors.newSingleThreadExecutor();
    /**
     * 是否从通道读数据
     */
    private volatile boolean isReading = true;
    /**
     * 是否继续连接，断链后继续连接
     */
    private volatile boolean iskeepAlive = true;
    /**
     * SSL上下文对象
     */
    private volatile SSLContext context;
    /**
     * 正在关闭
     */
    private volatile AtomicBoolean isCloseing = new AtomicBoolean(false);
    /**
     * 已启动
     */
    private volatile boolean started = false;
    /**
     * 正在尝试连接
     */
    private volatile AtomicBoolean goOnConnect = new AtomicBoolean(false);
    /**
     * 是否已经连接完成 true 为连接完成，此时{@link #read()}才能开始
     */
    private volatile boolean isConnectionFinished = false;
    /**
     * 心跳时间
     */
    private volatile long intervalHeartBeatTime = 5000;
    /**
     * {@link #doHandshake()}方法中 socket 正在read时 = true
     */
    private boolean isConnectionReading = false;

    /**
     * Creates the key managers required to initiate the {@link SSLContext}, using a JKS keystore as
     * an input.
     *
     * @param filepath - the path to the JKS keystore.
     * @param keystorePassword - the keystore's password.
     * @param keyPassword - the key's passsword.
     * @return {@link KeyManager} array that will be used to initiate the {@link SSLContext}.
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    public KeyManager[] createKeyManagers(String filepath, String keystorePassword,
            String keyPassword) throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        InputStream keyStoreIS = this.getClass().getClassLoader().getResourceAsStream(filepath);
        try {
            keyStore.load(keyStoreIS, keystorePassword.toCharArray());
        } finally {
            if (keyStoreIS != null) {
                keyStoreIS.close();
            }
        }
        KeyManagerFactory kmf =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPassword.toCharArray());
        return kmf.getKeyManagers();
    }

    /**
     * Creates the trust managers required to initiate the {@link SSLContext}, using a JKS keystore
     * as an input.
     *
     * @param filepath - the path to the JKS keystore.
     * @param keystorePassword - the keystore's password.
     * @return {@link TrustManager} array, that will be used to initiate the {@link SSLContext}.
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     */
    public TrustManager[] createTrustManagers(String filepath, String keystorePassword)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        InputStream trustStoreIS = this.getClass().getClassLoader().getResourceAsStream(filepath);
        try {
            trustStore.load(trustStoreIS, keystorePassword.toCharArray());
        } finally {
            if (trustStoreIS != null) {
                trustStoreIS.close();
            }
        }
        TrustManagerFactory trustFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);
        return trustFactory.getTrustManagers();
    }

    private String keyPath;
    private String keyStorepass;
    private String keypass;
    private String trustPath;
    private String trustStorePass;

    /**
     * 初始化本地证书
     * 
     * @param keyPath 私钥文件路径
     * @param keyStorepass 私钥文件密码
     * @param keypass 私钥密码
     * @param trustPath 公钥数字证书文件路径
     * @param trustStorePass 公钥数字证书文件密码
     * @param processExecutorSize 线程池线程数
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws UnrecoverableKeyException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws IOException
     */
    public NIOSSLClient(String keyPath, String keyStorepass, String keypass, String trustPath,
            String trustStorePass, int processExecutorSize)
            throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException,
            KeyStoreException, CertificateException, IOException {
        this.keyPath = keyPath;
        this.keyStorepass = keyStorepass;
        this.keypass = keypass;
        this.trustPath = trustPath;
        this.trustStorePass = trustStorePass;
        context = SSLContext.getInstance("SSL");
        context.init(createKeyManagers(keyPath, keyStorepass, keypass),
                createTrustManagers(trustPath, trustStorePass), new SecureRandom());
	executor = new ScheduledThreadPoolExecutor(processExecutorSize);
        executor.execute(new NIOSSLClientReadWork());
        executor.execute(new NIOSSLClientHeartbeatWork());
    }

    /**
     * 根据{@link SSLSession#getPacketBufferSize()} 扩大数据buffer,大于则以该参数，小于则2倍
     * {@link ByteBuffer#capacity()}
     * 
     * @param engine
     * @param buffer
     * @return
     */
    private ByteBuffer enlargePacketBuffer(SSLEngine engine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getPacketBufferSize());
    }

    /**
     * 根据{@link SSLSession#getApplicationBufferSize()} 扩大数据buffer,大于则以该参数，小于则2倍
     * {@link ByteBuffer#capacity()}
     * 
     * @param engine
     * @param buffer
     * @return
     */
    private ByteBuffer enlargeApplicationBuffer(SSLEngine engine, ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getApplicationBufferSize());
    }

    /**
     * 
     * 将<code>sessionProposedCapacity<code>与缓冲区的容量进行比较。 如果缓冲区的容量较小，则返回具有建议容量的缓冲区。
     * 如果相等或更大，则返回一个容量为初始大小的两倍的缓冲区。
     *
     * @param buffer - 要放大的缓冲区
     * @param sessionProposedCapacity - 由{@link SSLSession}提出的新缓冲区的最小大小
     * @return 容量较大的新缓冲区
     */
    private ByteBuffer enlargeBuffer(ByteBuffer buffer, int sessionProposedCapacity) {
        if (sessionProposedCapacity > buffer.capacity()) {
            buffer = getByteBuffer(sessionProposedCapacity);
        } else if (sessionProposedCapacity > 0) {
            buffer = getByteBuffer(buffer.capacity() + sessionProposedCapacity);
        } else {
            buffer = getByteBuffer(buffer.capacity() * 2);
        }
        return buffer;
    }

    /**
     * 处理{@link SSLEngineResult.Status#BUFFER_UNDERFLOW}会检查缓冲区是否已经被填充，如果没有填充满则返回相同的缓冲区，所以客户端再次尝试读取。
     * 如果缓冲区已经被填充，将尝试将缓冲区放大到会话建议的大小或更大的容量。 缓冲区下溢只能在展开后才能发生，所以缓冲区将始终是一个peerNetData缓冲区。
     *
     * @param buffer - 将始终是peerNetData缓冲区
     * @param engine - 用于加密/解密两个对等体之间交换的数据的SSLEngine 对象
     * @return 如果没有空间问题则返回相同的缓冲区，否则返回具有相同的数据但空间更大的新的缓冲区
     */
    private ByteBuffer handleBufferUnderflow(SSLEngine engine, ByteBuffer buffer) {
        if (logger.isDebugEnabled()) {
            logger.debug(Markers.SSLCLIENT, "engine.getSession().getPacketBufferSize()-->"
                    + engine.getSession().getPacketBufferSize());
        }
        if (engine.getSession().getPacketBufferSize() < buffer.limit()) {
            return buffer;
        } else {
            ByteBuffer replaceBuffer = enlargePacketBuffer(engine, buffer);
            buffer.flip();
            replaceBuffer.put(buffer);
            return replaceBuffer;
        }
    }

    /**
     * 打通连接
     * 
     * @return
     * @throws IOException
     */
    private boolean doHandshake() throws IOException {
        SSLEngineResult result;
        HandshakeStatus handshakeStatus;
        int appBufferSize = engine.getSession().getApplicationBufferSize();
        ByteBuffer myAppData = getByteBuffer(appBufferSize);
        ByteBuffer peerAppData = getByteBuffer(appBufferSize);
        myNetData.clear();
        peerNetData.clear();
        handshakeStatus = engine.getHandshakeStatus();
        boolean isNotOK = true;
        while (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED
                && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (handshakeStatus) {
                case NEED_UNWRAP:
                    if (isNotOK) {
                        isConnectionReading = true;
                        try {
                            if (socketChannel.read(peerNetData) < 0) {
                                if (engine.isInboundDone() && engine.isOutboundDone()) {
                                    return false;
                                }
                                try {
                                    engine.closeInbound();
                                } catch (SSLException e) {
                                    logger.error(Markers.SSLCLIENT,
                                            "This engine was forced to close inbound, without having received the proper SSL/TLS close notification message from the peer, due to end of stream.");
                                }
                                engine.closeOutbound();
                                handshakeStatus = engine.getHandshakeStatus();
                                return false;
                                // break;
                            }
                        } finally {
                            isConnectionReading = false;
                        }
                    }
                    peerNetData.flip();
                    try {
                        result = engine.unwrap(peerNetData, peerAppData);
                        peerNetData.compact();
                        handshakeStatus = result.getHandshakeStatus();
                    } catch (SSLException sslException) {
                        logger.error(Markers.SSLCLIENT,
                                "A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...");
                        engine.closeOutbound();
                        handshakeStatus = engine.getHandshakeStatus();
                        return false;
                        // break;
                    }
                    switch (result.getStatus()) {
                        case OK:
                            if (SSLEngineResult.HandshakeStatus.NEED_UNWRAP == handshakeStatus) {
                                isNotOK = false;
                            }
                            break;
                        case BUFFER_OVERFLOW:
                            peerAppData = enlargeApplicationBuffer(engine, peerAppData);
                            break;
                        case BUFFER_UNDERFLOW:
                            if (result.bytesProduced() <= 0 && result.bytesConsumed() <= 0) {
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                }
                            }
                            peerNetData = handleBufferUnderflow(engine, peerNetData);
                            break;
                        case CLOSED:
                            if (engine.isOutboundDone()) {
                                return false;
                            } else {
                                engine.closeOutbound();
                                handshakeStatus = engine.getHandshakeStatus();
                                return false;
                                // break;
                            }
                        default:
                            throw new IllegalStateException(
                                    "Invalid SSL status: " + result.getStatus());
                    }
                    break;
                case NEED_WRAP:
                    myNetData.clear();
                    try {
                        result = engine.wrap(myAppData, myNetData);
                        handshakeStatus = result.getHandshakeStatus();
                    } catch (SSLException sslException) {
                        logger.error(Markers.SSLCLIENT,
                                "A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...");
                        engine.closeOutbound();
                        handshakeStatus = engine.getHandshakeStatus();
                        return false;
                        // break;
                    }
                    switch (result.getStatus()) {
                        case OK:
                            myNetData.flip();
                            while (myNetData.hasRemaining()) {
                                socketChannel.write(myNetData);
                            }
                            break;
                        case BUFFER_OVERFLOW:
                            myNetData = enlargePacketBuffer(engine, myNetData);
                            break;
                        case BUFFER_UNDERFLOW:
                            throw new SSLException(
                                    "Buffer underflow occured after a wrap. I don't think we should ever get here.");
                        case CLOSED:
                            try {
                                myNetData.flip();
                                while (myNetData.hasRemaining()) {
                                    socketChannel.write(myNetData);
                                }
                                peerNetData.clear();
                            } catch (Exception e) {
                                throw new SSLException(
                                        "Failed to send server's CLOSE message due to socket channel's failure.");
                                // handshakeStatus = engine.getHandshakeStatus();
                            }
                            break;
                        default:
                            throw new IllegalStateException(
                                    "Invalid SSL status: " + result.getStatus());
                    }
                    break;
                case NEED_TASK:
                    Runnable task;
                    while ((task = engine.getDelegatedTask()) != null) {
                        executor.execute(task);
                    }
                    handshakeStatus = engine.getHandshakeStatus();
                    break;
                case FINISHED:
                    break;
                case NOT_HANDSHAKING:
                    break;
                default:
                    throw new IllegalStateException("Invalid SSL status: " + handshakeStatus);
            }
        }
        return true;
    }


    /**
     * 连接连接 TODO 连接成功后就发送启动成功消息 并 等待主发送配置(计算配置串MD5值是否一样，不一样则变更)
     * 
     * @return true连接成功 false连接失败
     * @throws IOException
     */
    private boolean connect() throws IOException {
        isCloseing.compareAndSet(true, false);
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(true);
        socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        socketChannel.setOption(StandardSocketOptions.SO_LINGER, 10);
        socketChannel.socket().connect(new InetSocketAddress(remoteAddress, port), 10000);
        // socketChannel.connect(new InetSocketAddress(remoteAddress, port));
        while (!socketChannel.finishConnect()) {
            threadSleep(5);
        }
        initSSLEngine(remoteAddress, port);
        engine.beginHandshake();
        isConnectionFinished = doHandshake();
        peerNetData.clear();
        return isConnectionFinished;
    }

    /**
     * 关闭{@link #socketChannel}
     */
    private void closeSocketChannel() {
        if (socketChannel != null) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                logger.error(Markers.SSLCLIENT,
                        "closeConnection()-->socketChannel.close()-->ERROR:" + e.getMessage(), e);
            }
        }
    }

    /**
     * 关闭连接
     * 
     * @throws IOException
     */
    private void closeConnection() {
        if (isCloseing.compareAndSet(false, true)) {
            isConnectionFinished = false;
            if (engine != null) {
                engine.closeOutbound();
                // try {
                // doHandshake();
                // } catch (IOException e) {
                // logger.error(Markers.SSLCLIENT,
                // "closeConnection()-->doHandshake()-->ERROR:" + e.getMessage(), e);
                // }
            }
            closeSocketChannel();
            if (iskeepAlive && remoteAddress != null && port > 0) {
                goOnConnect();
            }
        }
    }

    /**
     * 流末尾关闭连接
     * 
     * @throws IOException
     */
    private void handleEndOfStream() throws IOException {
        try {
            engine.closeInbound();
        } catch (Exception e) {
            logger.error(Markers.SSLCLIENT,
                    "This engine was forced to close inbound, without having received the proper SSL/TLS close notification message from the peer, due to end of stream.");
        }
        threadSleep(100);
        closeConnection();
    }

    /**
     * 实际发送buffer至HCM 方法
     * 
     * @param myAppData 必须为{@link ByteBuffer#flip()} 之后的buffer
     * @return true 成功 false错误
     * @throws IOException
     */
    private boolean writeByteBuffer(ByteBuffer myAppData) throws IOException {
        while (myAppData.hasRemaining()) {
            myNetData.clear();
            SSLEngineResult result = engine.wrap(myAppData, myNetData);
            switch (result.getStatus()) {
                case OK:
                    if (logger.isDebugEnabled()) {
                        logger.debug(Markers.SSLCLIENT, "write(" + myAppData
                                + ")-->myNetData.position:" + myNetData.position());
                    }
                    myNetData.flip();
                    int writeNum = 0;
                    while (myNetData.hasRemaining()) {
                        writeNum = socketChannel.write(myNetData);
                        if (logger.isDebugEnabled()) {
                            logger.debug(Markers.SSLCLIENT,
                                    "write(" + myAppData + ")-->myNetData.limit:"
                                            + myNetData.limit() + ",writeNum:" + writeNum);
                        }
                    }
                    return true;
                case BUFFER_OVERFLOW:
                    myNetData = enlargePacketBuffer(engine, myNetData);
                    break;
                case BUFFER_UNDERFLOW:
                    throw new SSLException(
                            "Buffer underflow occured after a wrap. I don't think we should ever get here.");
                case CLOSED:
                    threadSleep(100);
                    closeConnection();
                    return false;
                default:
                    throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
            }
        }
        return false;
    }

    /**
     * 分别发送最大大小 单位：字节
     */
    private int separatelyMaxSizeOfSend = 10 * 1024;

    /**
     * 当包大于是否分别发送
     */
    private boolean isSeparatelyOfSend = true;

    public boolean write(ByteBuffer myAppData) throws IOException {
        synchronized (socketChannel) {
            myAppData.flip();
            if (logger.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder();
                for (int i = myAppData.position(); i < myAppData.limit(); i++) {
                    sb.append(' ').append(myAppData.get(i));
                }
                logger.debug(Markers.SSLCLIENT,
                        "write(" + myAppData + ")-->myNetData.limit:" + myNetData.limit()
                                + ",buffer:[" + sb.toString() + "]");
            }
            if (!socketChannel.isConnected()) {
                throw new IOException("socket is not Connected");
            }
            if (isSeparatelyOfSend && myAppData.limit() > separatelyMaxSizeOfSend) {
                ByteBuffer sendBuffer = getByteBuffer(separatelyMaxSizeOfSend);
                while (myAppData.hasRemaining()) {
                    if (sendBuffer.hasRemaining()) {
                        sendBuffer.put(myAppData.get());
                    } else {
                        sendBuffer.flip();
                        if (writeByteBuffer(sendBuffer)) {
                            sendBuffer.clear();
                        } else {
                            sendBuffer.clear();
                            myAppData.clear();
                            return false;
                        }
                    }
                }
                if (sendBuffer.position() > 0) {
                    sendBuffer.flip();
                    if (!writeByteBuffer(sendBuffer)) {
                        sendBuffer.clear();
                        myAppData.clear();
                        return false;
                    }
                }
                sendBuffer.clear();
                myAppData.clear();
                return true;
            } else {
                boolean isSendOk = writeByteBuffer(myAppData);
                myAppData.clear();
                return isSendOk;
            }
        }
    }

    /**
     * buffer的总长度，不够消息的长度，则进行根据消息长度创建新的buffer，把buffer剩余内容拷贝到新buffer中，否则返回
     * offset偏移大于零，要对buffer进行压缩(把后续需处理的数据移至从0开始处)，以保证下一次处理，每个完整消息都是从offset=0开始
     * 
     * @param buffer
     * @param offset buffer中读取的位置
     * @param length 包长
     * @return
     */
    private ByteBuffer cheakBuffer(ByteBuffer buffer, int offset, int length) {
        if (length > buffer.capacity()) {
            ByteBuffer newBuffer = getByteBuffer(length);
            buffer.limit(buffer.position());
            buffer.position(offset);
            newBuffer.put(buffer);
            return newBuffer;
        }
        if (offset > 0) {
            buffer.limit(buffer.position());
            buffer.position(offset);
            buffer.compact();
        }
        return buffer;
    }

    /**
     * 读取buffer中的业务数据
     */
    private void readDataToHandle() {
        ByteBuffer buffer = peerAppData;
        int offset = 0, position = buffer.position(), length;
        for (;;) {
            length = SocketPacket.getPacketLength(buffer, offset);
            if (length == -1) {// buffer中 包不完整 连长度都无法计算 继续接收
                break;
            }
            if (position >= offset + length) { // buffer 中包完整 可直接获取
                byte[] data = new byte[length];
                buffer.position(offset);
                buffer.get(data, 0, length);
                buffer.position(position);
                handle(data);
                offset += length;
                continue;
            } else {// buffer中包不完整 继续接收
                break;
            }
        }
        if (offset < position) {// 有包未处理完
            buffer.position(position);
            peerAppData = cheakBuffer(buffer, offset, length);
        } else { // >= 表示包完整处理完 一般只有=
            buffer.clear();
            if (engine != null) {
                SSLSession session = engine.getSession();
                if (session != null) {
                    if (peerNetData.capacity() > session.getApplicationBufferSize()) {
                        peerAppData = releaseAndGetNewBuffer(peerAppData,
                                session.getApplicationBufferSize());
                    }
                    if (peerNetData.capacity() > session.getPacketBufferSize()) {
                        peerNetData =
                                releaseAndGetNewBuffer(peerNetData, session.getPacketBufferSize());
                    }
                }
            }
        }
    }

    /**
     * 从连接中读数据
     * 
     * @throws IOException
     */
    private void read() throws IOException {
        if (!isConnectionFinished) {
            threadSleep(100);
            return;
        }
        // peerNetData.clear();
        int bytesRead = 0;
        if (logger.isDebugEnabled()) {
            logger.debug(Markers.SSLCLIENT,
                    "read data is start!peerNetData:" + peerNetData.toString());
        }
        // TODO 考虑是否需要设置超时,拔网线等时间太久情况(考虑之前没收到参数时也发长周期心跳,收到后按参数周期发)
        bytesRead = socketChannel.read(peerNetData);
        if (logger.isDebugEnabled()) {
            logger.debug(Markers.SSLCLIENT,
                    "read data num:" + bytesRead + " ,peerNetData:" + peerNetData);
        }
        if (bytesRead > 0) {
            peerNetData.flip();
            while (peerNetData.hasRemaining()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(Markers.SSLCLIENT, "engine.unwrap(peerNetData:" + peerNetData
                            + ", peerAppData:" + peerAppData + ")-->start-->");
                }
                // TODO SSLProtocolException("Input SSL/TLS record too big: max = 33305 len = " + i)
                // 外部捕獲 返回錯誤碼 然後 返回前端 前端報網路異常請重試
                SSLEngineResult result = engine.unwrap(peerNetData, peerAppData);
                if (logger.isDebugEnabled()) {
                    logger.debug(Markers.SSLCLIENT, "engine.unwrap(peerNetData:" + peerNetData
                            + ", peerAppData:" + peerAppData + ")-->" + result);
                }
                // peerNetData.compact();
                // if (logger.isDebugEnabled()) {
                // logger.debug(Markers.SSLCLIENT,
                // "peerNetData.compact()-->peerNetData:" + peerNetData);
                // }
                switch (result.getStatus()) {
                    case OK:
                        readDataToHandle();
                        break;
                    case BUFFER_OVERFLOW: {
                        ByteBuffer new_peerAppData = enlargeApplicationBuffer(engine, peerAppData);
                        if (peerAppData.position() > 0) {
                            peerAppData.flip();
                            new_peerAppData.put(peerAppData);
                            peerAppData = new_peerAppData;
                        } else {
                            peerAppData = enlargeApplicationBuffer(engine, peerAppData);
                        }
                        break;
                    }
                    case BUFFER_UNDERFLOW: {
                        // 数据接收未完成 判断创建新buffer继续接收
                        int newBufferSize =
                                engine.getSession().getPacketBufferSize() > peerNetData.capacity()
                                        ? engine.getSession().getPacketBufferSize()
                                        : peerNetData.capacity();
                        ByteBuffer tempBuffer = getByteBuffer(newBufferSize);
                        tempBuffer.put(peerNetData);
                        peerNetData.clear();
                        peerNetData = tempBuffer;
                        return;
                    }
                    case CLOSED: {
                        threadSleep(100);
                        closeConnection();
                        return;
                    }
                    default:
                        throw new IllegalStateException(
                                "Invalid SSL status: " + result.getStatus());
                }
            }
            peerNetData.clear();
        } else if (bytesRead < 0) {
            handleEndOfStream();
        }
    }

    /**
     * 根据长度返回buffer
     * 
     * @param length
     * @return
     */
    private ByteBuffer getByteBuffer(int length) {
        return ByteBuffer.allocate(length);
    }

    /**
     * 释放老buffer 并根据长度 创建新buffer
     * 
     * @param buffer
     */
    private ByteBuffer releaseAndGetNewBuffer(ByteBuffer buffer, int length) {
        if (buffer != null) {
            buffer.clear();
        }
        return getByteBuffer(length);
    }

    /**
     * 关闭连接以及处理线程池
     * 
     * @throws IOException
     */
    @Override
    public void shutdown() throws IOException {
        iskeepAlive = false;
        isReading = false;
        threadSleep(100);
        closeConnection();
        executor.shutdown();
    }
    
    /**
     * 线程休眠 多少毫秒
     * 
     * @param millis
     */
    public static void threadSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    /**
     * 连接读操作
     */
    private class NIOSSLClientReadWork implements Runnable {
        @Override
        public void run() {
            while (isReading) {
                if (socketChannel != null && socketChannel.isConnected()) {
                    try {
                        read();
                    } catch (IOException e) {
                        logger.error(Markers.SSLCLIENT,
                                "(" + FormatUtil.formatMillisTimeOther(System.currentTimeMillis())
                                        + ")read data is error:" + e.getMessage(), e);
                        threadSleep(100);
                        closeConnection();
                    } catch (Exception ex) {
                        // TODO 考虑怎么处理
                        logger.error(Markers.SSLCLIENT,
                                "(" + FormatUtil.formatMillisTimeOther(System.currentTimeMillis())
                                        + ")read data is error:" + ex.getMessage(), ex);
                        threadSleep(100);
                    }
                } else {
                    threadSleep(1000);
                    if (started && !isConnectionFinished) {
                        closeConnection();
                    }
                }
            }
        }
    }

    /**
     * 心跳操作
     */
    private class NIOSSLClientHeartbeatWork implements Runnable {
        @Override
        public void run() {
            long times = System.currentTimeMillis();
            while (isReading) {
                if (intervalHeartBeatTime > 0 && socketChannel != null
                        && socketChannel.isConnected() && started && isConnectionFinished) {
                    try {
                        write(SocketPacket.getHeartbeatToMBuffer());
                    } catch (Exception e) {
                        logger.warn(Markers.SSLCLIENT, "Exception in heartbeat to Management.", e);
                    }
                    threadSleep(intervalHeartBeatTime);
                } else {
                    threadSleep(1000);
                }
                if (isConnectionReading) {
                    if (System.currentTimeMillis() - times > 30 * 1000) {
                        times = System.currentTimeMillis();
                        closeSocketChannel(); // SSL 连接时 通道堵塞 超时30s 则关闭连接 重连接
                    }
                } else {
                    times = System.currentTimeMillis();
                }
            }
        }
    }


    private void initSSLEngine(String remoteAddress, int port) {
        try {
            context = SSLContext.getInstance("SSL");
            context.init(createKeyManagers(keyPath, keyStorepass, keypass),
                    createTrustManagers(trustPath, trustStorePass), new SecureRandom());
        } catch (Exception e) {
            logger.error(Markers.SSLCLIENT,
                    "initSSLEngine-->init SSLContext is error:" + e.getMessage(), e);

        }
        engine = context.createSSLEngine(remoteAddress, port);
        engine.setUseClientMode(true);
        SSLSession session = engine.getSession();
        myNetData = releaseAndGetNewBuffer(myNetData, session.getPacketBufferSize());
        peerAppData = releaseAndGetNewBuffer(peerAppData, session.getApplicationBufferSize());
        peerNetData = releaseAndGetNewBuffer(peerNetData, session.getPacketBufferSize());
        if (logger.isDebugEnabled()) {
            logger.debug(Markers.SSLCLIENT, "session.getPacketBufferSize():"
                    + session.getPacketBufferSize() + " ,peerNetData-->" + peerNetData.toString());
        }
    }

    /**
     * 开始与IP 端口 进行通信
     * 
     * @param remoteAddress
     * @param port
     * @throws IOException
     */
    @Override
    public void start(String remoteAddress, int port) throws IOException {
        if (socketChannel != null && socketChannel.isConnected()) {
            iskeepAlive = false;
            closeConnection();
        }
        iskeepAlive = true;
        this.remoteAddress = remoteAddress;
        this.port = port;
        try {
            connect();
            started = true;
        } catch (IOException e) {
            started = true;
            throw e;
        }
    }

    /**
     * 如果连接断开，则继续连接
     */
    private void goOnConnect() {
        if (!goOnConnect.compareAndSet(false, true)) { // 只允许一个线程尝试连接 直至成功
            return;
        }
        int i = 0;
        int t = 1000;
        while (!isConnectionFinished) {
            try {
                connect();
            } catch (Exception e) {
                logger.error(Markers.SSLCLIENT,
                        "goOnStart(" + remoteAddress + "," + port + ") is error:" + e.getMessage(),
                        e);
            }
            i++;
            if (i > 10) {
                t += 100;
                if (t > 5000) {
                    t = 5000;
                }
            }
            if (i > 100) {
                i = 11;
            }
            if (!isConnectionFinished) {
                closeSocketChannel();
            }
            threadSleep(t);
        }
        try {
            SocketPacket.sendStartupInfo(this); 
        } catch (IOException e) {
            logger.error(Markers.SSLCLIENT, "send startup info is error:" + e.getMessage(), e);
        }
        goOnConnect.compareAndSet(true, false);
    }

    @Override
    public void setHeartbeatInterval(long intervalTime) {
        this.intervalHeartBeatTime = intervalTime;
    }

    /**
     * TODO 先接口返回调试<br>
     * 处理 {@link MsgSign#FLAG_RDS_SERVER_CONFIG} 结果
     * 
     * @param socketPacket
     */
    private void handleServerConfig(SocketPacket socketPacket) {
	Tuple2<Integer, String> responseTuple2 = null;
	String taskId = null;
	if (socketPacket.data != null) {
	    HCSClusterAllConfig hcsClusterAllConfig = null;
	    try {
		String configJsonStr = Middleware.getStringFromBytes(socketPacket.data);
		hcsClusterAllConfig = HCSClusterAllConfig.loadObjectFromJSONString(configJsonStr);
		if (hcsClusterAllConfig != null) {
		    taskId = hcsClusterAllConfig.getTaskId();
		    if (taskId == null) {
			taskId = MsgSign.FLAG_RDS_SERVER_CONFIG + "-" + "-1";
		    } else {
			responseTuple2 = Middleware.getMiddleware().handleServerConfig(hcsClusterAllConfig);
		    }
		}
	    } catch (Exception e) {
		String errorMgs = "handle json data[" + socketPacket.getDebugValue() + "] is error:" + e.getMessage();
		responseTuple2 = new Tuple2<Integer, String>(MsgSign.ERROR_CODE_121000, errorMgs);
		logger.error(Markers.CONFIG, errorMgs, e);
	    }
	}
	if (responseTuple2 == null) {
	    String errorMgs = "handle json data[" + socketPacket.getDebugValue() + "] is error";
	    responseTuple2 = new Tuple2<Integer, String>(MsgSign.ERROR_CODE_121001, errorMgs);
	    logger.error(Markers.CONFIG, errorMgs);
	}
	TaskResponse taskResponse = new TaskResponse();
	taskResponse.setId(taskId);
	taskResponse.setCode(responseTuple2._1());
	taskResponse.setMessage(responseTuple2._2());
	try {
	    socketPacket.setData(taskResponse);
	    socketPacket.writeBufferToSSLClient(this);
	    return;
	} catch (Exception e) {
	    logger.error(Markers.CONFIG,
		    "ERROR sending the result of processing json data[" + socketPacket.getDebugValue() + "]:"
			    + e.getMessage(), e);
	}
    }

    /**
     * 对收到的数据包进行处理
     * 
     * @param data
     */
    @Override
    public void handle(final byte[] data) {
	if (logger.isDebugEnabled()) {
	    logger.debug(Markers.SSLCLIENT, "read  handle data[" + data.length + "] is start!");
	}
	// executor.execute(new Runnable() {
	// @Override
	// public void run() {
	// TODO 具体处理内容 可能需堵塞运行
	try {
	    SocketPacket socketPacket = new SocketPacket();
	    socketPacket.read(data);
	    if (logger.isDebugEnabled()) {
		logger.debug(Markers.SSLCLIENT, "executor.execute socketPacket:" + socketPacket.getDebugValue());
	    }
	    if (MsgSign.TYPE_RDS_SERVER == socketPacket.type) {
		switch (socketPacket.flags) {
		case MsgSign.FLAG_RDS_SERVER_CONFIG: {
		    handleServerConfig(socketPacket);
		    break;
		}
		default: {// TODO RDS MySQL 实例变更
		    logger.error(Markers.SSLCLIENT, "socket request flags is error:" + socketPacket.flags);
		    break;
		}
		}
	    } else {
		logger.error(Markers.SSLCLIENT, "socket request type is error:" + socketPacket.type);
	    }
	} catch (Exception e) {
	    logger.error(Markers.SSLCLIENT, "socket request is error:" + e.getMessage(), e);
	}
	// }
	// });
    }
}
