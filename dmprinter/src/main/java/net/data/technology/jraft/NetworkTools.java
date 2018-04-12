package net.data.technology.jraft;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 网络相关工具包
 */
public class NetworkTools {
    /**
     * logger 日志对象
     */
    private static final Logger logger = LoggerFactory.getLogger(NetworkTools.class);
    /**
     * 根据ip地址返回 所在 网卡名称,如IP 不存在 则返回null
     * 
     * @param ip
     *            需返回网卡名称的 IP
     * @return null 表示Ip未被绑定
     */
    public static String getDeviceByIp(String ip) {
	try {
	    NetworkInterface netif = NetworkInterface.getByInetAddress(InetAddress.getByName(ip));
	    return netif == null ? null : netif.getName();
	} catch (Exception e) {
	    // e.printStackTrace();
	    logger.warn("getDeviceByIp(" + ip + ") is error:" + e.getMessage(), e);
	}
	return null;
    }

    /**
     * 通过命令ip addr add ip dev deviceName 命令增加IP
     * 
     * @param ip
     *            目前仅支持ipv4 字符串
     * @param deviceName
     * @return
     */
    public static boolean addIpByCommand(String ip, String deviceName) {
	ProcessBuilder builder = new ProcessBuilder("ip", "addr", "add", ip + "/32", "dev", deviceName);
	Process process = null;
	try {
	    process = builder.start();
	    int processComplete = process.waitFor();
	    if (processComplete != 0) {
		InputStream err = process.getErrorStream();
		int length = err.available();
		if (length >= 0) {
		    byte[] errArray = new byte[length];
		    err.read(errArray, 0, length);
		    String errorInfo = new String(errArray);
		    if (errorInfo.toLowerCase().indexOf("file exists") > 0) {
			if (logger.isInfoEnabled()) {
			    logger.info("run 'ip addr add " + ip + "/32 dev " + deviceName + "' is error:" + errorInfo);
			}
			return true;
		    } else {
			logger.warn("run 'ip addr add " + ip + "/32 dev" + deviceName + "' is error:" + errorInfo);
		    }
		}
	    } else {
		return true;
	    }
	} catch (Exception e) {
	    logger.warn("run 'ip addr add " + ip + "/32 dev" + deviceName + "' is error:" + e.getMessage(), e);
	} finally {
	    if (process != null) {
		process.destroy();
	    }
	}
	return false;
    }

    /**
     * 通过命令ip addr del ip dev deviceName 命令增加IP
     * 
     * @param ip
     *            目前仅支持ipv4字符串
     * @param deviceName
     * @return
     */
    public static boolean delIpByCommand(String ip, String deviceName) {
	ProcessBuilder builder = new ProcessBuilder("ip", "addr", "del", ip + "/32", "dev", deviceName);
	Process process = null;
	try {
	    process = builder.start();
	    int processComplete = process.waitFor();
	    if (processComplete != 0) {
		InputStream err = process.getErrorStream();
		int length = err.available();
		if (length >= 0) {
		    byte[] errArray = new byte[length];
		    err.read(errArray, 0, length);
		    String errorInfo = new String(errArray);
		    if (errorInfo.toLowerCase().indexOf("file exists") > 0) {
			if (logger.isInfoEnabled()) {
			    logger.info("run 'ip addr del " + ip + "/32 dev" + deviceName + "' is error:" + errorInfo);
			}
			return true;
		    } else {
			logger.warn("run 'ip addr del " + ip + "/32 dev" + deviceName + "' is error:" + errorInfo);
		    }
		}
	    } else {
		return true;
	    }
	} catch (Exception e) {
	    logger.warn("run 'ip addr del " + ip + "/32 dev" + deviceName + "' is error:" + e.getMessage(), e);
	} finally {
	    if (process != null) {
		process.destroy();
	    }
	}
	return false;
    }

    /**
     * ping ip
     * 
     * @param ip
     * @return
     */
    public static boolean pingIp(String ip) {
	try {
	    return InetAddress.getByName(ip).isReachable(5000);
	} catch (Exception e) {
	    if (logger.isInfoEnabled()) {
		logger.info("ping " + ip + "is error:" + e.getMessage(), e);
	    }
	}
	return false;
    }

    /**
     * 增加IP
     * 
     * @param ip
     * @return 1表示成功 <=0表示失败:<br>
     *         -1 表示已监听
     */
    public static int addIp(String vip, String localIp) {
	String deviceName = getDeviceByIp(vip);
	if (deviceName != null) {
	    return -1;
	}
	// ip addr 网卡 以及 判断是否已有IP
	// ip addr add 192.168.220.15 dev enp0s3 添加IP 和 网卡
	// ip addr 判断是否已有IP
	// ping ip 判断是否已有IP
	return 0;
    }

    /**
     * 删除IP
     * 
     * @param ip
     * @return
     */
    public static boolean delIp(String ip) {
	// ip addr 网卡 以及 判断是否已有IP 并获取 ip/24(ip/32) 网卡字符等
	// ip addr del 192.168.220.15/24 dev enp0s3 添加IP 和 网卡字符
	// ip addr 判断是否已有IP
	// ping ip 判断是否已有IP
	return false;
    }

    /**
     * 
     * @param args 
     */
    public static void main(String[] args) {

    }

}

