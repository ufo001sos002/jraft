package net.data.technology.jraft;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * 网络相关工具包 TODO 需要添加 远程 SSH 移除 VIP的方法 以免本机程序挂了
 * /HotDB-Cloud-Management/src/main/java/cn/hotdb/cloud/pool/SSHPoolManager.java
 * /HotDB-Cloud-Management/src/main/java/cn/hotdb/cloud/util/SSHOperater.java
 * 秘钥文件 id_rsa id_rsa.pub 要么各账号不一致 要么秘钥文件一个
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
     * 返回两个字符串从头至尾 相同字符的 长度
     * 
     * @param strA
     * @param strB
     * @return
     */
    private static int getSameCharLength(String strA, String strB) {
	char[] cA = strA.toCharArray();
	char[] cB = strB.toCharArray();
	int num = cA.length > cB.length ? cB.length : cA.length;
	for (int i = 0; i < num; i++) {
	    if (cA[i] != cB[i]) {
		return i;
	    }
	}
	return 0;
    }

    /**
     * 根据ip地址返回 同网段网卡名称,如异常获取失败 则返回null
     * 
     * @param ip
     *            需返回网卡名称的 IP
     * @return null 表示异常，获取失败
     */
    public static String getSameNetworkSegmentDeviceByIp(String ip) {
	ProcessBuilder builder = new ProcessBuilder("ip", "addr");
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
		    // logger.warn("run 'ip addr' is error:" + new String(errArray));
		    System.err.println("run 'ip addr' is error:" + new String(errArray));
		}
	    } else {
		BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = null;
		Pattern devPattern = Pattern.compile("(\\d{1})\\:(\\s+)(\\S+)\\:(.*)");// 3 dev
		Pattern ipPattern = Pattern.compile("(\\s+)inet(\\s{1})(\\S+)/(.*)"); // 3 ip
		Matcher matcher = null;
		HashMap<String, ArrayList<String>> devIpMap = new HashMap<String, ArrayList<String>>();
		String dev = null;
		String devIp = null;
		ArrayList<String> ips = null;
		while ((line = in.readLine()) != null) {
		    matcher = devPattern.matcher(line);
		    if(matcher.find()) {
			dev = matcher.group(3);
			ips = devIpMap.get(dev);
			if (ips == null) {
			    ips = new ArrayList<String>();
			    devIpMap.put(dev, ips);
			}
			continue;
		    }
		    matcher = ipPattern.matcher(line);
		    if(matcher.find()) {
			devIp = matcher.group(3);
			if (ips != null) {
			    ips.add(devIp.trim());
			}
		    }
		}
		dev = null;
		int maxNum = 0;
		int num = 0;
		for (Entry<String, ArrayList<String>> entry : devIpMap.entrySet()) {
		    ips = entry.getValue();
		    for (String t_ip : ips) {
			num = getSameCharLength(ip, t_ip);
			if (num >= maxNum) {
			    dev = entry.getKey();
			    maxNum = num;
			}
		    }
		}
		return dev;
	    }
	} catch (Exception e) {
	    // logger.warn("run 'ip addr' is error:", e);
	    System.err.println("run 'ip addr' is error:" + e.getMessage());
	    e.printStackTrace();
	} finally {
	    if (process != null) {
		process.destroy();
	    }
	}
	return null;
    }

    /**
     * 增加IP绑定(绑定在同一网段网卡上)；>=0 表示已绑定VIP
     * 
     * @param ip
     * @return 1 表示成功<br>
     *         0 表示已监听<br>
     *         -1 表示失败<br>
     *         -2 表示VIP已在其他地方被绑定<br>
     *         -3 表示 根据 本地IP 获取 所在网卡 失败 <br>
     */
    public static int addIp(String vip) {
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
	deviceName = getSameNetworkSegmentDeviceByIp(vip);
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
     * 远程根据ip地址返回 所在 网卡名称,如IP 不存在 则返回null
     * 
     * @param sshOperater
     *            ssh连接对象
     * @param ip
     *            需返回网卡名称的 IP
     * @return null 表示Ip未被绑定
     */
    public static String getIpDeviceOfRemote(SSHOperater sshOperater, String ip) {
	try {
	    ResInfo resInfo = sshOperater.executeSudoCmd("ip addr", 0);
	    if (resInfo != null) {
		if (resInfo.isSuccess()) {
		    BufferedReader in = new BufferedReader(new StringReader(resInfo.getOutRes()));
		    String line = null;
		    Pattern devPattern = Pattern.compile("(\\d{1})\\:(\\s+)(\\S+)\\:(.*)");// 3  dev
		    Pattern ipPattern = Pattern.compile("(\\s+)inet(\\s{1})(\\S+)/(.*)"); // 3 ip
		    Matcher matcher = null;
		    HashMap<String, ArrayList<String>> devIpMap = new HashMap<String, ArrayList<String>>();
		    String dev = null;
		    String devIp = null;
		    ArrayList<String> ips = null;
		    while ((line = in.readLine()) != null) {
			matcher = devPattern.matcher(line);
			if (matcher.find()) {
			    dev = matcher.group(3);
			    ips = devIpMap.get(dev);
			    if (ips == null) {
				ips = new ArrayList<String>();
				devIpMap.put(dev, ips);
			    }
			    continue;
			}
			matcher = ipPattern.matcher(line);
			if (matcher.find()) {
			    devIp = matcher.group(3);
			    if (ips != null) {
				ips.add(devIp.trim());
			    }
			}
			}
		    for (Entry<String, ArrayList<String>> entry : devIpMap.entrySet()) {
			ips = entry.getValue();
			for (String t_ip : ips) {
			    if (ip.equals(t_ip)) {
				return entry.getKey();
			    }
			}
			}
		} else {
		    // logger.warn("remote run 'ip addr' is error:" +  resInfo.getErrRes());
		    System.err.println("remote run 'ip addr' is error:" + resInfo.getErrRes());
		}
	    }
	} catch (Exception e) {
	    // logger.warn("remote run 'ip addr' is error:", e);
	    System.err.println("remote run 'ip addr' is error:" + e.getMessage());
	    e.printStackTrace();
	}
	return null;
    }

    /**
     * 远程 通过命令ip addr del ip dev deviceName 命令解除网卡绑定IP<br>
     * TODO 还需再window系统下添加 方便调试
     * 
     * @param sshOperater
     *            ssh连接对象
     * @param ip
     *            目前仅支持ipv4 字符串
     * @param deviceName
     *            IP绑定的网卡名
     * @return true 解除绑定成功 false 解除绑定失败
     */
    public static boolean delIpByRemoteCommand(SSHOperater sshOperater, String ip, String deviceName) {
	try {
	    ResInfo resInfo = sshOperater.executeSudoCmd("ip addr del " + ip + "/32, dev " + deviceName, 0);
	    if (resInfo != null) {
		if (resInfo.isSuccess()) {
		    return true;
		} else {
		    String errorInfo = resInfo.getErrRes();
		    if (errorInfo.toLowerCase().indexOf("file exists") > 0) {
			// if (logger.isInfoEnabled()) {
			// 	logger.info("run 'ip addr del " + ip + "/32 dev" + deviceName + "' is error:" + errorInfo);
			// }
			System.out.println("run 'ip addr del " + ip + "/32 dev" + deviceName + "' is error:" + errorInfo);
			return true;
		    } else {
			// logger.warn("run 'ip addr del " + ip + "/32 dev" +  deviceName + "' is error:" + errorInfo);
			System.err.println("run 'ip addr del " + ip + "/32 dev" + deviceName + "' is error:" + errorInfo);
		    }
		}
	    }
	} catch (Exception e) {
	    // logger.warn("run 'ip addr del " + ip + "/32 dev" + deviceName + "' is error:" + e.getMessage(), e);
	    System.err.println("run 'ip addr del " + ip + "/32 dev" + deviceName + "' is error:" + e.getMessage());
	    e.printStackTrace();
	}
	return false;
    }

    /**
     * 远程解除本机IP绑定 ；>=0表示 VIP已解绑
     * 
     * @param sshOperater
     *            ssh连接对象
     * @param vip
     *            需解绑的IP
     * @return 1 表示成功<br>
     *         0 表示当前VIP未被绑定<br>
     *         -1 表示解除失败<br>
     *         -2 表示VIP已在其他地方被绑定<br>
     */
    public static int delIpOfRemote(SSHOperater sshOperater, String vip) {
	String deviceName = getIpDeviceOfRemote(sshOperater, vip);
	if (deviceName == null) { // 当前VIP 未绑定
	    if (pingIp(vip,1000)) { // ping vip 是否还通
		return -2; // 当前网卡未绑定VIP 但其他地方有绑定VIP
	    } else {
		return 0;// 当前网卡未绑定VIP
	    }
	}
	if (delIpByRemoteCommand(sshOperater, vip, deviceName)) { // 解除VIP绑定
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

