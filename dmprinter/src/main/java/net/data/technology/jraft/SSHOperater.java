package net.data.technology.jraft;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;


public class SSHOperater {
    private static final Logger logger = LoggerFactory.getLogger(SSHOperater.class);

    private String host;
    private Integer port;
    private String userName;
    private String password; // 登录用户密码或私钥密码
    private byte[] prvkeyFileContent; // 认证的私钥文件内容
    private boolean isUsedPrvkey = false;
    private static byte[] defualtprvkeyFileContent = null; // 默认认证的私钥文件内容
    private int fisrtConnectTimeout = 40000; // 首次连接超时
    private int sencondConnectTimeout = 20000; // 二次重连超时
    private int soTimeout = 3600000; // 默认一小时
    private int sftpConnectTimeout = 120000; // sftp超时时间2分钟

    private Session session;

    public SSHOperater(String host, Integer port, String userName, String password) {
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public byte[] getPrvkeyFileContent() {
        return prvkeyFileContent;
    }

    /**
     * 
     * @param host
     * @param port
     * @param userName
     * @param password
     * @param isUsedPrvkey 为空使用默认私钥文件
     */
    public SSHOperater(String host, Integer port, String userName, byte[] prvkeyFileContent,
            String password) {
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.prvkeyFileContent = prvkeyFileContent;
        this.isUsedPrvkey = true;
        this.password = password;
    }

    /**
     * @功能 读取流
     * @param inStream
     * @return 字节数组
     * @throws Exception
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
	ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
	byte[] buffer = new byte[1024];
	int len = -1;
	while ((len = inStream.read(buffer)) != -1) {
	    outSteam.write(buffer, 0, len);
	}
	outSteam.close();
	inStream.close();
	return outSteam.toByteArray();
    }

    public static byte[] getDefualtprvkeyFileContent() {
        if (SSHOperater.defualtprvkeyFileContent == null) {
            InputStream in = SSHOperater.class.getClassLoader().getResourceAsStream("id_rsa");
            try {
		SSHOperater.defualtprvkeyFileContent = readStream(in);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return SSHOperater.defualtprvkeyFileContent;
    }

    public void connect() {
        if (session == null)
            try {
                createSession();
            } catch (JSchException e) {
                logger.error(e.getMessage());
                close();
                return;
            }
        try {
            // 通过Session建立链接
            session.connect(fisrtConnectTimeout); // 首次连接超时
        } catch (Exception e) {
            if (sencondConnectTimeout > 0) {
                try {
                    // 通过Session建立链接
                    session.connect(sencondConnectTimeout); // 二次连接超时
                } catch (Exception e2) {
                    logger.error("ssh connect error. host=" + host + ", port=" + port
                            + ", userName=" + userName + ". message=" + e.getMessage());
                    close();
                }
            } else {
                logger.error("ssh connect error. host=" + host + ", port=" + port + ", userName="
                        + userName + ". message=" + e.getMessage());
                close();
            }
        }
    }

    public boolean isConnected() {
        if (session == null)
            return false;
        boolean isConnected = session.isConnected();
        if (!isConnected) {
            close();
        }
        return isConnected;
    }

    public Session getSession() {
        if (!isConnected())
            connect(); // 中断重连
        return session;
    }

    private void createSession() throws JSchException {
        if (session == null) {
            synchronized (this) {
                if (session == null) {
                    JSch jsch = new JSch(); // 创建JSch对象
                    if (isUsedPrvkey) {
                        jsch.addIdentity("jsch",
                                prvkeyFileContent != null ? prvkeyFileContent
                                        : getDefualtprvkeyFileContent(),
                                null, password != null ? password.getBytes() : null); // 设置
                    }
                    if (port == null) {
                        // 根据用户名，主机ip获取一个Session对象
                        session = jsch.getSession(userName, host);
                    } else {
                        // 根据用户名，主机ip，端口获取一个Session对象
                        session = jsch.getSession(userName, host, port);
                    }
                    if (!isUsedPrvkey && password != null && password.length() > 0) {
                        session.setPassword(password); // 设置密码
                    }
                    Properties config = new Properties();
                    config.put("userauth.gssapi-with-mic", "no");// GSSAPI关闭，加快访问速度
                    config.put("StrictHostKeyChecking", "no");
                    session.setConfig(config); // 为Session对象设置properties
                    session.setTimeout(soTimeout); // 设置timeout时间
                }
            }
        }
    }

    public void close() {
        if (session != null) {
            session.disconnect();
            session = null;
        }
    }

    /**
     * 执行指令
     * 
     * @param commands
     */
    @Deprecated
    public String executeCommandReturnStr(String commands) {
        ResInfo resInfo = executeCommand(commands);
        return resInfo == null ? null : resInfo.getOutRes();
    }

    @Deprecated
    public ResInfo executeCommand(String commands) {
        return executeCommand(commands, 0);
    }

    /**
     * 执行shell指令并且返回结果对象ResInfo，等待标准输出结束
     * 
     * @param commands
     * @return
     */
    @Deprecated
    public ResInfo executeCommand(String commands, int waitResultInterval) {
        ResInfo resInfo = null;
        if (commands == null || commands.length() == 0)
            return resInfo;
        logger.debug(" executeCommand[" + host + "] : " + commands);
        ChannelExec channel = null;
        byte[] tmp = new byte[1024]; // 读数据缓存
        StringBuffer strBuffer = new StringBuffer(); // 执行SSH返回的结果
        StringBuffer errResult = new StringBuffer();
        try {
            Session session = getSession();
            if (session == null)
                return null;
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(commands);
            channel.setInputStream(null);
            channel.setErrStream(null);
            channel.connect(sftpConnectTimeout);
            InputStream stdStream = channel.getInputStream();
            InputStream errStream = channel.getErrStream();
            // 开始获得SSH命令的结果
            while (true) {
                // 获得错误输出
                while (errStream.available() > 0) {
                    int i = errStream.read(tmp, 0, 1024);
                    if (i < 0)
                        break;
                    errResult.append(new String(tmp, 0, i));
                }
                // 获得标准输出
                while (stdStream.available() > 0) {
                    int i = stdStream.read(tmp, 0, 1024);
                    if (i < 0)
                        break;
                    strBuffer.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    int code = channel.getExitStatus();
                    logger.debug(" executeCommand[" + host + "] exit-status: " + code);
                    resInfo = new ResInfo(code, strBuffer.toString(), errResult.toString());
                    break;
                }
                if (waitResultInterval > 0) {
                    try {
                        Thread.sleep(waitResultInterval);
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }
            }
            stdStream.close();
            errStream.close();
            return resInfo;
        } catch (JSchException e) {
            logger.error(e.getMessage(), e);
            close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            close();
        } finally {
            if (channel != null) {
                channel.disconnect();
                channel = null;
            }
        }
        return resInfo;
    }

    public ResInfo executeSudoCmd(String commands, int waitResultInterval) {
        ResInfo resInfo = null;
        if (commands == null || commands.length() == 0)
            return resInfo;
        logger.debug(" executeCommand[" + host + "] : " + commands);
        ChannelExec channel = null;
        byte[] tmp = new byte[1024]; // 读数据缓存
        StringBuffer strBuffer = new StringBuffer(); // 执行SSH返回的结果
        StringBuffer errResult = new StringBuffer();
        try {
            Session session = getSession();
            if (session == null)
                return null;
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("sudo -S -p '' " + commands);
            channel.setPty(true);
            InputStream stdStream = channel.getInputStream();
            OutputStream out = channel.getOutputStream();
            InputStream errStream = channel.getErrStream();
            channel.connect(sftpConnectTimeout);
            out.write((password + "\n").getBytes());
            out.flush();
            // 开始获得SSH命令的结果
            while (true) {
                // 获得错误输出
                while (errStream.available() > 0) {
                    int i = errStream.read(tmp, 0, 1024);
                    if (i < 0)
                        break;
                    errResult.append(new String(tmp, 0, i));
                }
                // 获得标准输出
                while (stdStream.available() > 0) {
                    int i = stdStream.read(tmp, 0, 1024);
                    if (i < 0)
                        break;
                    strBuffer.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    int code = channel.getExitStatus();
                    logger.debug(" executeCommand[" + host + "] exit-status: " + code);
                    resInfo = new ResInfo(code, strBuffer.toString(), errResult.toString());
                    break;
                }
                if (waitResultInterval > 0) {
                    try {
                        Thread.sleep(waitResultInterval);
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }
            }
            stdStream.close();
            errStream.close();
            out.close();
            return resInfo;
        } catch (JSchException e) {
            logger.error(e.getMessage(), e);
            close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            close();
        } finally {
            if (channel != null) {
                channel.disconnect();
                channel = null;
            }
        }
        return resInfo;
    }

    /**
     * 执行scp命令
     * 
     * @param cmd scp命令
     * @param password 密码
     * @param waitResultInterval 结果等待睡眠时间（单位毫秒）
     * @param executeTimeOut 执行超时（单位毫秒）
     * @return
     */
    @Deprecated
    public ResInfo executeScpCmd(String cmd, String password, long waitResultInterval,
            Long executeTimeOut) {
        ResInfo res = null;
        if (cmd == null || cmd.length() == 0)
            return res;
        ChannelShell channel = null;
        long now = System.currentTimeMillis();
        long timeOut = (executeTimeOut == null || executeTimeOut <= 0 ? 60000
                : executeTimeOut.longValue());
        try {
            channel = (ChannelShell) getSession().openChannel("shell");
            channel.connect(sftpConnectTimeout);
            byte[] tmp = new byte[1024]; // 读数据缓存
            StringBuffer strBuffer = new StringBuffer(); // 执行SSH返回的结果
            StringBuffer allBuffer = new StringBuffer(); // 执行SSH返回的结果
            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();
            out.write((cmd + "\n").getBytes());
            out.write("exit\n".getBytes());
            out.flush();
            boolean isExit = false;
            // 开始获得SSH命令的结果
            while (true) {
                // 获得标准输出
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0)
                        break;
                    strBuffer.append(new String(tmp, 0, i));
                }
                String tmpStr = strBuffer.toString().trim();
                if (tmpStr.length() > 0) {
                    allBuffer.append(strBuffer);
                    if (tmpStr.endsWith("(yes/no)?")) {
                        strBuffer = new StringBuffer();
                        out.write("yes\n".getBytes());
                        out.flush();
                        continue;
                    } else if (tmpStr.endsWith("password:")) {
                        strBuffer = new StringBuffer();
                        out.write((password + "\n").getBytes());
                        out.flush();
                        continue;
                    } else if (tmpStr.endsWith("]#") || tmpStr.endsWith("]$")) {
                        isExit = true;
                    }
                    strBuffer = new StringBuffer();
                }
                if (channel.isClosed()) {
                    int code = channel.getExitStatus();
                    logger.debug(" executeCommand[" + host + "] exit-status: " + code);
                    if (code == 0)
                        res = new ResInfo(code, allBuffer.toString(), null);
                    else
                        res = new ResInfo(code, null, allBuffer.toString());
                    break;
                }
                if (isExit || (System.currentTimeMillis() - now) >= timeOut) {
                    strBuffer = new StringBuffer();
                    out.write("exit\n".getBytes());
                    out.flush();
                    continue;
                }
                if (waitResultInterval > 0) {
                    try {
                        Thread.sleep(waitResultInterval);
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }
            }
            in.close();
            out.close();
        } catch (JSchException e) {
            logger.error(e.getMessage(), e);
            close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            close();
        } finally {
            if (channel != null) {
                channel.disconnect();
                channel = null;
            }
        }
        return res;
    }

    public ResInfo executeSudoScpCmd(String cmd, String password, long waitResultInterval,
            Long executeTimeOut) {
        ResInfo res = null;
        if (cmd == null || cmd.length() == 0)
            return res;
        ChannelShell channel = null;
        long now = System.currentTimeMillis();
        long timeOut = (executeTimeOut == null || executeTimeOut <= 0 ? 60000
                : executeTimeOut.longValue());
        try {
            channel = (ChannelShell) getSession().openChannel("shell");
            channel.setPty(true);
            channel.connect(sftpConnectTimeout);
            byte[] tmp = new byte[1024]; // 读数据缓存
            StringBuffer strBuffer = new StringBuffer(); // 执行SSH返回的结果
            StringBuffer allBuffer = new StringBuffer(); // 执行SSH返回的结果
            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();
            out.write(("sudo -s -p 'sudo:' " + cmd + "\n").getBytes());
            out.write("exit\n".getBytes());
            out.flush();
            boolean isExit = false;
            // 开始获得SSH命令的结果
            while (true) {
                // 获得标准输出
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0)
                        break;
                    strBuffer.append(new String(tmp, 0, i));
                }
                String tmpStr = strBuffer.toString().trim();
                if (tmpStr.length() > 0) {
                    allBuffer.append(strBuffer);
                    if (tmpStr.endsWith("sudo:")) {
                        strBuffer = new StringBuffer();
                        out.write((this.password + "\n").getBytes());
                        out.flush();
                        continue;
                    } else if (tmpStr.endsWith("(yes/no)?")) {
                        strBuffer = new StringBuffer();
                        out.write("yes\n".getBytes());
                        out.flush();
                        continue;
                    } else if (tmpStr.endsWith("password:")) {
                        strBuffer = new StringBuffer();
                        out.write((password + "\n").getBytes());
                        out.flush();
                        continue;
                    } else if (tmpStr.endsWith("]#") || tmpStr.endsWith("]$")) {
                        isExit = true;
                    }
                    strBuffer = new StringBuffer();
                }
                if (channel.isClosed()) {
                    int code = channel.getExitStatus();
                    logger.debug(" executeCommand[" + host + "] exit-status: " + code);
                    if (code == 0)
                        res = new ResInfo(code, allBuffer.toString(), null);
                    else
                        res = new ResInfo(code, null, allBuffer.toString());
                    break;
                }
                if (isExit || (System.currentTimeMillis() - now) >= timeOut) {
                    strBuffer = new StringBuffer();
                    out.write("exit\n".getBytes());
                    out.flush();
                    continue;
                }
                if (waitResultInterval > 0) {
                    try {
                        Thread.sleep(waitResultInterval);
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }
            }
            in.close();
            out.close();
        } catch (JSchException e) {
            logger.error(e.getMessage(), e);
            close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            close();
        } finally {
            if (channel != null) {
                channel.disconnect();
                channel = null;
            }
        }
        return res;
    }

    @Deprecated
    public ResInfo executeCommandOnShell(String commands) {
        return executeCommandOnShell(new String[] {commands}, null);
    }

    /**
     * 完全模式在Linux的SHELL上执行命令，返回的信息里带命令提示符，执行完exit退出通道，加了&在后台执行不用等待结果
     * 
     * @param commands
     * @param executeTimeOut 执行超时(单位：秒)
     * @return
     */
    @Deprecated
    public ResInfo executeCommandOnShell(String[] commands, Integer executeTimeOut) {
        ResInfo res = null;
        if (commands == null || commands.length == 0)
            return null;

        long now = System.currentTimeMillis();
        ChannelShell channel = null;
        try {
            channel = (ChannelShell) getSession().openChannel("shell");
            channel.connect(sftpConnectTimeout);
            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();
            PrintWriter printWriter = new PrintWriter(out);
            for (String cmd : commands) {
                logger.debug(" executeCommandOnShell[" + host + "] : " + cmd);
                printWriter.println(cmd);
            }
            printWriter.println("exit");// 加上个就是为了，结束本次交互
            printWriter.flush();
            res = new ResInfo();
            // 获取命令执行的结果
            BufferedReader read = new BufferedReader(new InputStreamReader(in));
            StringBuffer strBuffer = new StringBuffer();
            String msg = null;
            long time = System.currentTimeMillis();
            int timeOut =
                    (executeTimeOut == null || executeTimeOut <= 0 ? 60 : executeTimeOut.intValue())
                            * 1000;
            while ((time - now) < timeOut) {
                while ((msg = read.readLine()) != null) { // 等待读取输出流时无法确知时间
                    strBuffer.append(msg + "\n");
                }
                if (channel.isClosed()) {
                    res.setExitStatus(channel.getExitStatus());
                    if (res.getExitStatus() == 0) {
                        res.setOutRes(strBuffer.toString());
                    } else {
                        res.setErrRes(strBuffer.toString());
                    }
                    logger.debug("executeCommandOnShell[" + host + "] exit-status: "
                            + res.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                }
                time = System.currentTimeMillis();
            }
            read.close();
            in.close();
            out.close();
            return res;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            close();
        } finally {
            if (channel != null) {
                channel.disconnect();
                channel = null;
            }
        }
        return null;
    }

    /**
     * 上传本地文件到远程linux上 使用sftp上传
     * 
     * @param localFile
     * @param remoteDir
     * @return
     * @throws JSchException
     * @throws SftpException
     */
    public boolean uploadLocalFileToRemote(String localFile, String remoteDir) throws Exception {
	if (StringUtil.isEmpty(localFile) || StringUtil.isEmpty(remoteDir))
            return false;
        return uploadLocalFileToRemote(new File(localFile), remoteDir);
    }

    /**
     * 路径规范化
     * 
     * @param path
     * @return
     */
    public static String normalize(String path) {
	path = path.replaceAll("\\\\+", "/").replaceAll("\\/+", "/");
	if (path.endsWith("/")) {
	    path = path.substring(0, path.length() - 1);
	}
	return path;
    }

    /**
     * 上传本地文件到远程linux上 使用sftp上传
     * 
     * @throws JSchException
     * @throws SftpException
     */
    @Deprecated
    public boolean uploadLocalFileToRemote(File localFile, String remoteDir) throws Exception {
        if (localFile == null || !localFile.exists()) {
            FileNotFoundException e = new FileNotFoundException();
            throw new SftpException(4, e.toString(), e);
        }
	remoteDir = normalize(remoteDir);
        if (localFile.isDirectory()) { // 目录
            boolean flag = false;
            remoteDir = remoteDir + "/" + localFile.getName();
            if (!remoteFileExist(remoteDir))
                createRemoteDirectory(remoteDir);
            for (File f : localFile.listFiles()) {
                flag = uploadLocalFileToRemote(f, remoteDir);
                if (!flag)
                    return flag;
            }
            return flag;
        } else {
            return uploadLocalFileToRemote2(localFile, remoteDir);
        }
    }

    public boolean uploadLocalFileToRemote2(File localFile, String remoteDir) throws Exception {
	remoteDir = normalize(remoteDir);
        SftpProgressMonitorImpl sftpProgressMonitorImpl = new SftpProgressMonitorImpl();
        ChannelSftp sftp = null;
        try {
            sftp = (ChannelSftp) getSession().openChannel("sftp");
            if (sftp == null)
                return false;
            sftp.connect(sftpConnectTimeout);
            sftp.put(localFile.getAbsolutePath(), remoteDir, sftpProgressMonitorImpl);
        } catch (Exception e) {
            e.printStackTrace();
            close();
	    if (StringUtil.isEmpty(e.getMessage()))
                throw new Exception("ssh[" + host + "]连接异常");
            throw e;
        } finally {
            if (sftp != null) {
                sftp.disconnect();
                sftp = null;
            }
        }
        return sftpProgressMonitorImpl.isSuccess();
    }

    /**
     * sftp下载文件（夹）
     * 
     * @param directory 下载文件上级目录
     * @param srcFile 下载文件完全路径
     * @param saveFile 保存文件路径
     * @param sftp ChannelSftp
     * @throws UnsupportedEncodingException
     * @throws SftpException
     * @throws JSchException
     */
    public boolean download(String remoteFile, String saveDir) throws Exception {
        File saveFile = new File(saveDir);
        if (!saveFile.exists())
            saveFile.mkdirs();
	remoteFile = normalize(remoteFile);
        ChannelSftp sftp = null;
        try {
            sftp = (ChannelSftp) getSession().openChannel("sftp");
            if (sftp == null)
                return false;
            sftp.connect(sftpConnectTimeout);
            SftpATTRS sftpATTRS = sftp.lstat(remoteFile);
            if (sftpATTRS.isDir()) {
                boolean flag = true;
                // 目录
                Vector conts = sftp.ls(remoteFile);
                saveDir = new File(saveFile, new File(remoteFile).getName()).getAbsolutePath();
                // 文件夹(路径)
                for (Iterator iterator = conts.iterator(); iterator.hasNext();) {
                    LsEntry obj = (LsEntry) iterator.next();
                    String filename = new String(obj.getFilename().getBytes(), "UTF-8");
                    // 扫描到文件名为".."这样的直接跳过
                    if ("..".equals(filename) || ".".equals(filename)) {
                        continue;
                    }
                    flag = download(remoteFile + "/" + filename, saveDir);
                    if (!flag)
                        break;
                }
                return flag;
            } else if (sftpATTRS.isReg()) {
                SftpProgressMonitorImpl sftpProgressMonitorImpl = new SftpProgressMonitorImpl();
                sftp.get(remoteFile, saveDir, sftpProgressMonitorImpl);
                return sftpProgressMonitorImpl.isSuccess();
            } else {
                logger.debug("remote File:unkown");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            close();
	    if (StringUtil.isEmpty(e.getMessage()))
                throw new Exception("ssh[" + host + "]连接异常");
            throw e;
        } finally {
            if (sftp != null) {
                sftp.disconnect();
                sftp = null;
            }
        }

    }

    public ChannelSftp openSftpChannel() throws IOException {
        ChannelSftp sftp = null;
        try {
            sftp = (ChannelSftp) getSession().openChannel("sftp");
        } catch (Exception e) {
        }
        if (sftp == null)
            throw new IOException("ssh[" + host + "]sftp通道打开失败");
        try {
            sftp.connect(sftpConnectTimeout);
        } catch (Exception e) {
            throw new IOException("ssh[" + host + "]连接异常", e);
        }
        return sftp;
    }

    public ByteData download(String remoteFile) throws IOException {
        try {
            ChannelSftp sftp = openSftpChannel();
	    return ByteData.valueOf(sftp.get(normalize(remoteFile)))
                    .withCloseable(new Closeable() {
                        @Override
                        public void close() throws IOException {
                            sftp.disconnect();
                        }
                    });
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("ssh[" + host + "]下载文件失败", e);
        }
    }

    public void upload(String remoteFile, ByteData data) throws IOException {
        ChannelSftp sftp = null;
        try {
            sftp = openSftpChannel();
	    sftp.put(data.toStream(), normalize(remoteFile));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("ssh[" + host + "]上传文件失败", e);
        } finally {
            if (sftp != null)
                sftp.disconnect();
        }
    }

    /**
     * 删除远程linux下的文件
     * 
     * @throws JSchException
     * @throws SftpException
     */
    @Deprecated
    public boolean deleteRemoteFileorDir(String remoteFile) throws Exception {
	if (StringUtil.isEmpty(remoteFile))
            return false;
        ChannelSftp sftp = null;
        try {
            sftp = (ChannelSftp) getSession().openChannel("sftp");
            if (sftp == null)
                return false;
            sftp.connect(sftpConnectTimeout);
            SftpATTRS sftpATTRS = sftp.lstat(remoteFile);
            if (sftpATTRS.isDir()) {
                // 目录
                logger.debug("remote File:dir");
                sftp.rmdir(remoteFile);
                return true;
            } else if (sftpATTRS.isReg() || sftpATTRS.isLink()) {
                // 文件
                logger.debug("remote File:file");
                sftp.rm(remoteFile);
                return true;
            } else {
                logger.debug("remote File:unkown");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            close();
	    if (StringUtil.isEmpty(e.getMessage()))
                throw new Exception("ssh[" + host + "]连接异常");
            throw e;
        } finally {
            if (sftp != null) {
                sftp.disconnect();
                sftp = null;
            }
        }
    }

    /**
     * 判断linux下 某文件是否存在
     * 
     * @throws JSchException
     * @throws SftpException
     */
    public boolean remoteFileExist(String remoteFile) throws Exception {
        return remoteFileExist(getRemoteFileStat(remoteFile));
    }

    public boolean remoteFileExist(SftpATTRS sftpATTRS) {
        if (sftpATTRS == null)
            return false;
        if (sftpATTRS.isDir() || sftpATTRS.isReg() || sftpATTRS.isLink()) {
            // 目录 和文件
            return true;
        } else {
            return false;
        }
    }

    public SftpATTRS getRemoteFileStat(String remoteFile) throws Exception {
        SftpATTRS sftpATTRS = null;
	if (StringUtil.isEmpty(remoteFile))
            return sftpATTRS;
        ChannelSftp sftp = null;
        try {
            sftp = (ChannelSftp) getSession().openChannel("sftp");
            if (sftp == null)
                return sftpATTRS;
            sftp.connect(sftpConnectTimeout);
            sftpATTRS = sftp.lstat(remoteFile);
            return sftpATTRS;
        } catch (SftpException e) {
            if (e.id == 2) {
                logger.warn(e.getMessage());
                return sftpATTRS;
            } else {
                logger.error(e.getMessage(), e);
                close();
		if (StringUtil.isEmpty(e.getMessage()))
                    throw new SftpException(e.id, "ssh[" + host + "]连接异常");
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            close();
	    if (StringUtil.isEmpty(e.getMessage()))
                throw new Exception("ssh[" + host + "]连接异常");
            throw e;
        } finally {
            if (sftp != null) {
                sftp.disconnect();
                sftp = null;
            }
        }
    }

    /**
     * 创建远程目录
     * 
     * @throws JSchException
     * @throws SftpException
     */
    @Deprecated
    public boolean createRemoteDirectory(String remoteFile) throws SftpException {
	if (StringUtil.isEmpty(remoteFile))
            return false;
        ResInfo res = executeCommand("mkdir -p " + remoteFile, 10);
        if (res.isSuccess())
            return true;
        else
            return false;
    }

    @Override
    public String toString() {
        return "SSHOperater [host=" + host + ", port=" + port + ", userName=" + userName
                + ", password=" + password + "]";
    }
}

