package net.data.technology.jraft;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AIOSocketWR {
    private static final Logger logger = LoggerFactory.getLogger(AIOSocketWR.class);
    private static final AIOReadHandler aioReadHandler = new AIOReadHandler();
    private static final AIOWriteHandler aioWriteHandler = new AIOWriteHandler();
    private final AsynchronousSocketChannel channel;
    protected final AbstractConnection con;
    /**
     * 控制外部调用写操作
     */
    private final AtomicBoolean writing = new AtomicBoolean(false);
    protected final AtomicBoolean reading = new AtomicBoolean(false);
    /**
     * AIO 写时信号锁 至写完释放(目前可不需要)
     */
    public final Semaphore aioWriteSemaphore = new Semaphore(1);

    public AIOSocketWR(AbstractConnection conn) {
        channel = (AsynchronousSocketChannel) conn.getChannel();
        this.con = conn;
    }

    public void asynRead() {
        ByteBuffer theBuffer = con.readBuffer;
        if (theBuffer == null) {
            theBuffer = con.allocate();
            con.readBuffer = theBuffer;
            reading.set(true);
            channel.read(theBuffer, this, aioReadHandler);
        } else if (theBuffer.hasRemaining()) {
            reading.set(true);
            channel.read(theBuffer, this, aioReadHandler);
        } else {
            throw new java.lang.IllegalArgumentException("full buffer to read ");
        }
    }

    private void asynWrite(ByteBuffer buffer) {
        buffer.flip();
        this.channel.write(buffer, this, aioWriteHandler);
    }

    private void write0() {
        ByteBuffer theBuffer = con.writeBuffer;
        if (theBuffer != null) { // 如果上次有写数据
            if (theBuffer.hasRemaining()) { // 如未写完, 继续写
                theBuffer.compact();
                asynWrite(theBuffer);
                return;
            } else { // 写完回收上次buffer
                    con.writeBuffer = null;
            }
        }
        theBuffer = con.writeQueue.poll(); // 获取队列中是否还有buffer需要写
        if (theBuffer == null) {
            synchronized (con.writeQueue) {
                theBuffer = con.writeQueue.poll(); // 获取队列中是否还有buffer需要写
                if (theBuffer == null) {
                    writing.compareAndSet(true, false);// 置写操作结束
                    return;
                }
            }
        }
        if (theBuffer.limit() == 0) {// buffer 有问题 断开连接
                con.writeBuffer = null;
            con.close("quit cmd(buffer.limit() == 0)");
        } else {
	    con.writeBuffer = theBuffer;
	    asynWrite(theBuffer);
        }
    }

    protected void onWriteFinished(int result) {
        write0();
    }

    public void doNextWriteCheck() {
        if (!writing.compareAndSet(false, true)) {
            return;
        }
        write0();
    }


    /**
     * 
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (con != null) {
            return con.toString();
        } else if (channel != null) {
            return channel.toString();
        }
        return super.toString();
    }

}


class AIOWriteHandler implements CompletionHandler<Integer, AIOSocketWR> {

    @Override
    public void completed(final Integer result, final AIOSocketWR wr) {
        try {
            if (AbstractConnection.logger.isDebugEnabled()) {
                AbstractConnection.logger.debug(Markers.CONNECTION,
                        "completed(result:" + result + "):" + wr.toString());
            }
            if (result >= 0) {
                wr.onWriteFinished(result);
            } else {
                wr.con.close("write erro " + result);
            }
        } catch (Exception e) {
            AbstractConnection.logger.warn(Markers.CONNECTION, "caught aio process err:", e);
            wr.con.close("caught aio process err");
        }

    }

    @Override
    public void failed(Throwable exc, AIOSocketWR wr) {
        wr.aioWriteSemaphore.release();
        wr.con.close("write failed " + exc);
    }

}


class AIOReadHandler implements CompletionHandler<Integer, AIOSocketWR> {
    @Override
    public void completed(final Integer i, final AIOSocketWR wr) {
        wr.reading.set(false);
        if (i > 0) {
            try {
                wr.con.onReadData(i);
                wr.con.asynRead();
            } catch (Exception e) {
                wr.con.close("handle err:" + e);
            }
        } else if (i == -1) {
            wr.con.close("Read closed");
        }
    }

    @Override
    public void failed(Throwable exc, final AIOSocketWR wr) {
        wr.reading.set(false);
        wr.con.close(exc.toString());

    }
}
