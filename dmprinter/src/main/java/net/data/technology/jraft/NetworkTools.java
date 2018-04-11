package net.data.technology.jraft;

/**
 * 网络相关工具包
 */
public class NetworkTools {

    /**
     * 
     * 
     * @param ip
     * @return
     */
    public static boolean addIp(String ip) {
	// ip addr 网卡 以及 判断是否已有IP
	// ip addr add 192.168.220.15 dev enp0s3 添加IP 和 网卡
	// ip add 判断是否已有IP
	// ping ip 判断是否已有IP
	return false;
    }

    /**
     * 
     * 
     * @param ip
     * @return
     */
    public static boolean delIp(String ip) {
	// ip addr 网卡 以及 判断是否已有IP 并获取 ip/24(ip/32) 网卡字符等
	// ip addr del 192.168.220.15/24 dev enp0s3 添加IP 和 网卡字符
	// ip add 判断是否已有IP
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

