package net.data.technology.jraft;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 网络相关工具包
 */
public class NetworkTools {

    /**
     * 增加IP
     * 
     * @param ip
     * @return
     */
    public static boolean addIp(String ip) {
	// ip addr 网卡 以及 判断是否已有IP
	// ip addr add 192.168.220.15 dev enp0s3 添加IP 和 网卡
	// ip addr 判断是否已有IP
	// ping ip 判断是否已有IP
	return false;
    }

    /**
     * 根据ip地址返回 所在 网卡名称,如IP 不存在 则返回null
     * 
     * @param ip
     *            需返回网卡名称的 IP
     * @return null 表示Ip未被绑定
     */
    public static String getDeviceByIp(String ip) {
	try {
	    NetworkInterface.getByInetAddress(InetAddress.getByAddress("".getBytes()));
	} catch (SocketException | UnknownHostException e) {
	    e.printStackTrace();
	}
	return null;
    }

    /**
     * 根据ip地址 返回当前适配需要 绑定 在的网卡名称
     * 
     * @param ip
     *            需要被绑定的ip
     * @return
     * @throws SocketException
     */
    public static void getMatchDeviceByIp(String ip, boolean returnDevice) throws Exception {
	Enumeration<NetworkInterface> netifs = NetworkInterface.getNetworkInterfaces();
	if (netifs == null) {
	    throw new SocketException("no found network device");
	}
	NetworkInterface netif = null;
	Enumeration<InetAddress> ips = null;
	while (netifs.hasMoreElements()) {
	    netif = netifs.nextElement();
	    ips = netif.getInetAddresses();
	}
	// ip addr 判断是否已有IP
	// return false;
    }

    /**
     * ping ip
     * 
     * @param ip
     * @return
     */
    public static boolean pingIp(String ip) {
	return false;
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

