package net.data.technology.jraft;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Session;


public class SSHPoolManager {
    private final static Logger logger = LoggerFactory.getLogger(SSHPoolManager.class);

    private static Map<String, SSHOperater> sshMap = new ConcurrentHashMap<>();

    private static String getKey(String host, Integer port, String userName) {
        return host + (port != null ? ("_" + port) : "") + "_" + userName;
    }

    /** 删除SSHOperater */
    public static void removeSSHOperater(String host, Integer port, String userName) {
        String key = getKey(host, port, userName);
        SSHOperater ssho = sshMap.get(key);
        if (ssho == null)
            return;
        sshMap.remove(key);
        ssho.close();
        ssho = null;
    }

    public static SSHOperater getSSHOperater(String host, Integer port, String userName,
            String password) {
        return getSSHOperater(host, port, userName, false, null, password);
    }

    /** 获取机器的SSHOperater */
    public static SSHOperater getSSHOperater(String host, Integer port, String userName,
            boolean isUsedPrvkey, byte[] prvkeyContent, String password) {
        SSHOperater ssho = null;
        String key = getKey(host, port, userName);
        ssho = sshMap.get(key);
        if (ssho != null) {
            Session session = ssho.getSession();
            if (session != null && session.isConnected()) {
                return ssho;
            }
            sshMap.remove(key);
            ssho.close();
        }
        synchronized (SSHPoolManager.class) {
            if (sshMap.get(key) == null) {
                if (isUsedPrvkey) {
                    ssho = new SSHOperater(host, port, userName, prvkeyContent, password);
                } else {
                    ssho = new SSHOperater(host, port, userName, password);
                }
                Session session = ssho.getSession();
                if (session != null && session.isConnected()) {
                    sshMap.put(key, ssho);
                } else {
                    ssho.close();
                }
            }
            ssho = sshMap.get(key);
        }
        return ssho;
    }
}
