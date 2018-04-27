package net.data.technology.jraft.jsonobj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.JSON;

import net.data.technology.jraft.CollectionUtil;
import net.data.technology.jraft.CollectionUtil.ToSortObject;


/**
 * IP 白名单 <br>
 * <b>注：如字段为null，则表示未设置该值</b>
 */
public class IpWhites implements ToSortObject {
    /**
     * 是否开启
     */
    private Boolean isEnable;
    /**
     * 白名单规则集合
     */
    private List<String> ips;

    /**
     * @return {@link #isEnable} 的值
     */
    public Boolean getIsEnable() {
        return isEnable;
    }

    /**
     * @param isEnable 根据 isEnable 设置 {@link #isEnable}的值
     */
    public void setIsEnable(Boolean isEnable) {
        this.isEnable = isEnable;
    }

    /**
     * @return {@link #ips} 的值
     */
    public List<String> getIps() {
        if (ips == null) {
            ips = new ArrayList<String>();
        }
        return ips;
    }

    /**
     * @param ips 根据 ips 设置 {@link #ips}的值
     */
    public void setIps(List<String> ips) {
        this.ips = ips;
    }

    /**
     * 
     * @param ip 根据 ip 增加至 {@link #ips}的值
     */
    public void addIp(String ip) {
        if (this.ips == null) {
            this.ips = new ArrayList<String>();
        }
        this.ips.add(ip);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    /**
     * 根据JSON字节数组 返回对象
     * 
     * @param jsonStr
     * @return
     */
    public static IpWhites loadIpWhitesFromJSONBytes(byte[] jsonBytes) {
        return loadIpWhitesFromJSONStr(new String(jsonBytes));
    }

    /**
     * 根据JSON字符串 返回对象
     * 
     * @param jsonStr
     * @return
     */
    public static IpWhites loadIpWhitesFromJSONStr(String jsonStr) {
        return JSON.parseObject(jsonStr, IpWhites.class);
    }

    /**
     * 排序
     */
    @Override
    public void toSort() {
        if (this.ips != null) {
            Collections.sort(this.ips);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        IpWhites cloneObj = (IpWhites) super.clone();
        if (this.ips != null) {
            cloneObj.setIps(CollectionUtil.cloneBaseObject(this.ips));
        }
        return cloneObj;
    }

}
