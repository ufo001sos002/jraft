package net.data.technology.jraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.SftpProgressMonitor;

public class SftpProgressMonitorImpl implements SftpProgressMonitor {
    private static final Logger log = LoggerFactory.getLogger(SftpProgressMonitorImpl.class);
    private long size;
    private long currentSize = 0;
    private boolean endFlag = false;

    @Override
    public boolean count(long count) {
        currentSize += count;
        // log.debug("传输数量:" + currentSize);
        return true;
    }

    @Override
    public void end() {
        log.debug("文件传输结束");
        endFlag = true;
    }

    @Override
    public void init(int op, String srcFile, String dstDir, long size) {
        log.debug("文件开始传输：[" + srcFile + "]-->[" + dstDir + "]" + "，文件大小:" + size + ",参数" + op);
        this.size = size;
    }

    public boolean isSuccess() {
        return endFlag && currentSize == size;
    }
}
