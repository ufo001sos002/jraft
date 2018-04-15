package net.data.technology.jraft;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * 网络相关工具包 TODO 需要添加 远程 SSH 移除 VIP的方法 以免本机程序挂了
 */
public class NetworkTools {
    // /**
    // * logger 日志对象
    // */
    // private static final Logger logger =
    // LoggerFactory.getLogger(NetworkTools.class);
    /**
     * 默认超时时间{@value}(毫秒)
     */
    public static final int DEFAULT_TIMEOUT = 5000;

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
	    // logger.warn("getDeviceByIp(" + ip + ") is error:" +
	    // e.getMessage(), e);
	    System.err.println("getDeviceByIp(" + ip + ") is error:" + e.getMessage());
	    e.printStackTrace();
	}
	return null;
    }

    /**
     * 通过命令ip addr add ip dev deviceName 命令对网卡增加IP绑定<br>
     * TODO 还需再window系统下添加 方便调试
     * 
     * @param ip
     *            目前仅支持ipv4 字符串
     * @param deviceName
     *            IP绑定的网卡名
     * @return true 绑定成功 false 绑定失败
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
			// if (logger.isInfoEnabled()) {
			// logger.info("run 'ip addr add " + ip + "/32 dev " +
			// deviceName + "' is error:" + errorInfo);
			// }
			System.out.println(
				"run 'ip addr add " + ip + "/32 dev " + deviceName + "' is error:" + errorInfo);
			return true;
		    } else {
			// logger.warn("run 'ip addr add " + ip + "/32 dev" +
			// deviceName + "' is error:" + errorInfo);
			System.err
				.println("run 'ip addr add " + ip + "/32 dev" + deviceName + "' is error:" + errorInfo);
		    }
		}
	    } else {
		return true;
	    }
	} catch (Exception e) {
	    // logger.warn("run 'ip addr add " + ip + "/32 dev" + deviceName +
	    // "' is error:" + e.getMessage(), e);
	    System.err.println("run 'ip addr add " + ip + "/32 dev" + deviceName + "' is error:" + e.getMessage());
	    e.printStackTrace();
	} finally {
	    if (process != null) {
		process.destroy();
	    }
	}
	return false;
    }

    /**
     * 通过命令ip addr del ip dev deviceName 命令解除网卡绑定IP<br>
     * TODO 还需再window系统下添加 方便调试
     * 
     * @param ip
     *            目前仅支持ipv4 字符串
     * @param deviceName
     *            IP绑定的网卡名
     * @return true 解除绑定成功 false 解除绑定失败
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
			// if (logger.isInfoEnabled()) {
			// logger.info("run 'ip addr del " + ip + "/32 dev" +
			// deviceName + "' is error:" + errorInfo);
			// }
			System.out
				.println("run 'ip addr del " + ip + "/32 dev" + deviceName + "' is error:" + errorInfo);
			return true;
		    } else {
			// logger.warn("run 'ip addr del " + ip + "/32 dev" +
			// deviceName + "' is error:" + errorInfo);
			System.err
				.println("run 'ip addr del " + ip + "/32 dev" + deviceName + "' is error:" + errorInfo);
		    }
		}
	    } else {
		return true;
	    }
	} catch (Exception e) {
	    // logger.warn("run 'ip addr del " + ip + "/32 dev" + deviceName +
	    // "' is error:" + e.getMessage(), e);
	    System.err.println("run 'ip addr del " + ip + "/32 dev" + deviceName + "' is error:" + e.getMessage());
	    e.printStackTrace();
	} finally {
	    if (process != null) {
		process.destroy();
	    }
	}
	return false;
    }

    /**
     * 按(默认超时时间 {@link #DEFAULT_TIMEOUT} )ping ip
     * 
     * @param ip
     *            ip地址，目前仅支持IPv4
     * @return
     */
    public static boolean pingIp(String ip) {
	return pingIp(ip, DEFAULT_TIMEOUT);
    }

    /**
     * ping ip
     * 
     * @param ip
     *            ip地址，目前仅支持IPv4
     * @param timeout
     *            ping 等待响应最大超时时间
     * @return
     */
    public static boolean pingIp(String ip, int timeout) {
	try {
	    return InetAddress.getByName(ip).isReachable(timeout);
	} catch (Exception e) {
	    // if (logger.isInfoEnabled()) {
	    // logger.info("ping " + ip + "is error:" + e.getMessage(), e);
	    // }
	    System.err.println("ping " + ip + "is error:" + e.getMessage());
	    e.printStackTrace();
	}
	return false;
    }

    /**
     * 根据本地IP 增加IP绑定；>=0 表示已绑定VIP
     * 
     * @param ip
     * @return 1 表示成功<br>
     *         0 表示已监听<br>
     *         -1 表示失败<br>
     *         -2 表示VIP已在其他地方被绑定<br>
     *         -3 表示 根据 本地IP 获取 所在网卡 失败 <br>
     */
    public static int addIp(String localIp, String vip) {
	String deviceName = getDeviceByIp(vip);
	if (deviceName != null) { // 当前VIP 已监听
	    if (pingIp(vip)) { // ping vip 是否成功
		return 0;
	    } else {
		delIp(vip); // 尝试解除IP 无论结果与否，由上端进行再次尝试(一般不会出现)
		return -1;
	    }
	}
	if (pingIp(vip)) {
	    return -2; // 当前未绑定VIP，但可以ping的通 表示其他地方已绑定
	}
	deviceName = getDeviceByIp(localIp);
	if (deviceName == null) { // 本地IP 获取 所在网卡 失败
	    return -3;
	}
	if (addIpByCommand(vip, deviceName)) { // 绑定VIP
	    if (pingIp(vip)) { // ping vip 是否成功
		return 1;
	    } else {
		delIp(vip); // 尝试解除IP 无论结果与否，由上端进行再次尝试 (一般不会出现)
	    }
	}
	return -1;
    }

    /**
     * 解除本机IP绑定 ；>=0表示 VIP已解绑
     * 
     * @param ip
     * @return 1 表示成功<br>
     *         0 表示当前VIP未被绑定<br>
     *         -1 表示解除失败<br>
     *         -2 表示VIP已在其他地方被绑定<br>
     */
    public static int delIp(String vip) {
	String deviceName = getDeviceByIp(vip);
	if (deviceName == null) { // 当前VIP 未绑定
	    if (pingIp(vip,1000)) { // ping vip 是否还通
		return -2; // 当前网卡未绑定VIP 但其他地方有绑定VIP
	    } else {
		return 0;// 当前网卡未绑定VIP
	    }
	}
	if (delIpByCommand(vip, deviceName)) { // 解除VIP绑定
	    if (pingIp(vip, 1000)) { // ping vip 是否成功
		return -1; // 解除绑定VIP失败
	    } else {
		return 1;// 解除绑定成功
	    }
	}
	return -1;
    }

    /**
     * 
     * @param args 
     */
    public static void main(String[] args) {
	System.out.println("localIp:" + args[0] + ",vip:" + args[1] + " add result:" + addIp(args[0], args[1]));
	try {
	    Thread.sleep(20000);
	} catch (InterruptedException e) {
	}
	System.out.println("localIp:" + args[0] + ",vip:" + args[1] + " del result:" + delIp(args[1]));
    }

}

