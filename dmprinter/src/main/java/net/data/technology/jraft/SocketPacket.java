package net.data.technology.jraft;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * hotdb cloud Server 与 hotdb management 交互数据包
 * 
 */
public class SocketPacket {
    private static final Logger logger = LoggerFactory.getLogger(SocketPacket.class);
    /**
     * 基本字节数(协议版本1字节、保留字段2个字节、类型1个字节、内容长度4个字节)
     */
    public static final int PACKET_HEADER_SIZE = 8;

	/**
	 * 协议版本
	 */
    public byte version = MsgSign.getCurrentVersion();
	/**
	 * 2bytes 保留字段
	 */
    public int flags;
	/**
	 * 1bytes
	 * 整数型，0保留不用，1-127是从HotDB-Cloud发往HotDB-Backup，128-255是从HotDB-Backup发往HotDB-
	 * Cloud
	 */
    public byte type;
	/**
	 * 4bytes 数据长度，json格式
	 */
	public int length;

	/**
	 * 数据
	 */
	public byte[] data;

	/**
     * 获取buffer中，从offset起完整数据包的字节数，-1表示内容不够无法计算数据包长度
     * 
     * @param buffer
     * @param offset 数据包获取的起始位置
     * @return 完整数据包字节数，-1表示内容不够无法计算数据包长度
     */
	public static int getPacketLength(ByteBuffer buffer, int offset) {
        if (buffer.position() < offset + PACKET_HEADER_SIZE) {
			return -1;
		} else {
			offset += 4;
			int length = (buffer.get(offset) & 0xff) << 24;
			length |= (buffer.get(++offset) & 0xff) << 16;
			length |= (buffer.get(++offset) & 0xff) << 8;
			length |= buffer.get(++offset) & 0xff;
            return length + PACKET_HEADER_SIZE;
		}
	}

    /**
     * 根据flag值和包体数据构建数据包对象
     * 
     * @param flags 标志值
     * @param data 包体数据
     */
    public SocketPacket(int flags, byte[] data) {
        this.flags = flags;
		this.length = data == null ? 0 : data.length;
		this.data = data;
	}

    /**
     * 构建包对象
     */
	public SocketPacket() {
	}

    /**
     * 
     * @return 包对象完整长度
     */
	private int calcPacketSize() {
		return PACKET_HEADER_SIZE + (data == null ? 0 : data.length);
	}

    public static int calcPacketSize(byte[] data) {
        return PACKET_HEADER_SIZE + (data == null ? 0 : data.length);
    }

    /**
     * 根据字节数组读取完整包对象
     * 
     * @param data 完整包数据
     */
	public void read(byte[] data) {
		try {
			int position = 0;
			this.version = data[position++];
			this.flags = readUB2(data, position);
			position += 2;
			this.type = data[position++];
			this.length = readInt(data, position);
			position += 4;
			if (this.length > 0) {
				if (this.data == null) {
					this.data = new byte[this.length];
				}
				System.arraycopy(data, position, this.data, 0, this.length);
			}
		} catch (RuntimeException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Exception:").append(e.getMessage()).append(" in Reading Packet:");
			for (byte b : data) {
				sb.append(" ").append(b);
			}
			sb.append(".");
            logger.warn(Markers.SSLCLIENT, sb.toString());
			throw e;
		}
	}

    /**
     * 将对象值写入buffer
     * 
     * @param buffer
     */
    private void write(ByteBuffer buffer) {
        write(buffer, version, flags, type, data);
	}

    /**
     * 将值写入buffer 对象中
     * 
     * @param buffer
     * @param version
     * @param flags
     * @param type
     * @param data
     */
    public static void write(ByteBuffer buffer, byte version, int flags, byte type, byte[] data) {
        buffer.put(version);
        writeUB2(buffer, flags);
        buffer.put(type);
        if (data != null) {
            writeInt(buffer, data.length);
            buffer.put(data);
        } else {
            writeInt(buffer, 0);
        }
    }

    /**
     * @return 将对象值写入新buffer并返回
     */
	public ByteBuffer writeBuffer() {
		int size = calcPacketSize();
        ByteBuffer buffer = getByteBuffer(size);
		write(buffer);
        if (logger.isDebugEnabled()) {
            logger.debug(Markers.SSLCLIENT,
                    "writeBuffer size:" + buffer.position() + " data:" + (data != null
			    ? new String(data, Middleware.UTF8_FOR_JAVA)
			    : "is empty"));
        }
		return buffer;
	}

    /**
     * 通过{@link SSLClient} 将本对象值发送<br>
     * <b>往连接中发送数据 当发送失败 / 异常时，由调用者确定后续处理(是否重新发送等)</b>
     * 
     * @param sslClient SSL连接客户端 对象
     * @return 发送成功与否 true成功
     * @throws IOException 连接通道异常
     * 
     */
    public boolean writeBufferToSSLClient(SSLClient sslClient) throws IOException {
        return sslClient.write(writeBuffer());
    }

    /**
     * @param data 根据 data 设置 {@link #data}{@link #length}的值
     */
    public void setData(byte[] data) {
        this.data = data;
        if (data != null) {
            this.length = data.length;
        }
    }

    /**
     * 
     * @return 将对象值写入新字节数组并返回
     */
	public byte[] writeBytes() {
		int size = calcPacketSize();
		ByteBuffer buffer = writeBuffer();
		byte[] b = new byte[size];
		buffer.flip();
		buffer.get(b);
		return b;
	}

    /**
     * 读取目标字节数值中 起始位置 起2个字节长度int 值
     * 
     * @param b 目标字节数组
     * @param offset 起始位置
     * @return 2个字节的存储的int值
     */
    public static final int readUB2(byte[] b, int offset) {
		int i = (b[offset++] & 0xff) << 8;
		i |= b[offset++] & 0xff;
		return i;
	}

    /**
     * 读取目标字节数值中 起始位置 起4个字节长度int 值
     * 
     * @param b 目标字节数组
     * @param offset 起始位置
     * @return 4个字节的存储的int值
     */
    public static final int readInt(byte[] b, int offset) {
		int i = (b[offset++] & 0xff) << 24;
		i |= (b[offset++] & 0xff) << 16;
		i |= (b[offset++] & 0xff) << 8;
		i |= b[offset++] & 0xff;
		return i;
	}

    /**
     * 将int值转成2个字节存入buffer中
     * 
     * @param buffer 被存入的buffer
     * @param i 需存储的int值
     */
    public static final void writeUB2(ByteBuffer buffer, int i) {
		byte h = (byte) (i & 0xff);
		buffer.put((byte) (i >>> 8));
		buffer.put(h);
	}
	
    /**
     * 将int值转成4个字节存入buffer中
     * 
     * @param buffer 被存入的buffer
     * @param i 需存储的int值
     */
	public static final void writeInt(ByteBuffer buffer, int i) {
		byte b4 = (byte) (i & 0xff);
		byte b3 = (byte) (i >>> 8);
		byte b2 = (byte) (i >>> 16);
		byte b1 = (byte) (i >>> 24);
		buffer.put(b1);
		buffer.put(b2);
		buffer.put(b3);
		buffer.put(b4);
	}

    /**
     * 创建新的buffer 并根据参数 将值写入buffer中
     * 
     * @param version
     * @param flags
     * @param type
     * @param data
     * @return
     */
    public static final ByteBuffer getWriteByteBuffer(byte version, int flags, byte type,
            byte[] data) {
        ByteBuffer buffer = getByteBuffer(calcPacketSize(data));
        write(buffer, version, flags, type, data);
        if (logger.isDebugEnabled() && flags > 0) {
            logger.debug(Markers.SSLCLIENT,
                    "writeBuffer size:" + buffer.position() + " data:" + (data != null
			    ? new String(data, Middleware.UTF8_FOR_JAVA)
			    : "is empty"));
        }
        return buffer;
    }

    /**
     * 根据长度返回buffer
     * 
     * @param length
     * @return
     */
    private static ByteBuffer getByteBuffer(int length) {
        return ByteBuffer.allocate(length);
    }

    /**
     * 根据 <a href='http://wiki.hotpu.cn:8090/pages/viewpage.action?pageId=13533402'>1.1.1 RS to
     * M_启动成功消息</a> 协议<br>
     * 发送启动成功消息
     * 
     * @param sslClient 与管理端通讯对象
     * @throws IOException
     */
    public static final void sendStartupInfo(SSLClient sslClient) throws IOException {
        sendStartupInfo(sslClient, MsgSign.FLAG_RDS_SERVER_START);
    }

    /**
     * 根据 <a href='http://wiki.hotpu.cn:8090/pages/viewpage.action?pageId=14123180'>1.3.1 RS to
     * M_自动异常切换</a> 协议<br>
     * 发送启动成功消息
     * 
     * @param sslClient 与管理端通讯对象
     * @throws IOException
     */
    public static final void sendAutoSwitch(SSLClient sslClient, String errorMsg)
            throws IOException {
        if (errorMsg != null) {
            sendStartupInfo(sslClient, MsgSign.FLAG_RDS_SERVER_AUTO_SWITCH);
        } else {
            TaskResponse taskResponse = new TaskResponse();
            taskResponse.setCode(1000);
            taskResponse.setMessage(errorMsg);
	    ByteBuffer sendBuffer = getWriteByteBuffer(MsgSign.getCurrentVersion(),
                    MsgSign.FLAG_RDS_SERVER_AUTO_SWITCH, MsgSign.TYPE_RDS_SERVER,
		    taskResponse.toString().getBytes(Middleware.UTF8_FOR_JAVA));
            sslClient.write(sendBuffer);
        }
    }

    /**
     * 发送启动成功消息
     * 
     * @param sslClient
     * @param flag
     * @throws IOException
     */
    private static final void sendStartupInfo(SSLClient sslClient, int flag) throws IOException {
	ByteBuffer sendBuffer = getWriteByteBuffer(MsgSign.getCurrentVersion(), flag,
                MsgSign.TYPE_RDS_SERVER, MsgSign.getSendDataFromSererIdBytes());
        sslClient.write(sendBuffer);
    }

    /**
     * 根据 <a href='http://wiki.hotpu.cn:8090/pages/viewpage.action?pageId=13533421'>1.2.127 RS to
     * M_心跳</a> 协议 生成心跳buffer<br>
     * 
     * @return 心跳协议buffer
     * 
     */
    public static final ByteBuffer getHeartbeatToMBuffer() {
        ByteBuffer sendBuffer =
		getWriteByteBuffer(MsgSign.getCurrentVersion(), 0, MsgSign.TYPE_RDS_SERVER,
                        MsgSign.getSendDataFromSererIdBytes());
        return sendBuffer;
    }

    /**
     * @return 该对象的调试信息
     */
    public String getDebugValue() {
        StringBuilder debugValue = new StringBuilder();
        debugValue.append("version:").append(version).append(' ').append("flags:").append(flags)
                .append(' ').append("type:").append(type).append(' ').append("length:")
                .append(length).append(' ').append("data:[");
        for (int i = 0; i < data.length; i++) {
            debugValue.append(' ').append(data[i]);
        }
        debugValue.append(' ').append("]--->json data: ");
        if (data.length > 0) {
	    debugValue.append(new String(data, Middleware.UTF8_FOR_JAVA));
        }
        return debugValue.toString();
    }

}
