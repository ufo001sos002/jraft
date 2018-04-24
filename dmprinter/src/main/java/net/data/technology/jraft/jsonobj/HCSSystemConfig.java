package net.data.technology.jraft.jsonobj;

import com.alibaba.fastjson.JSON;

import net.data.technology.jraft.CollectionUtil.ToSortObject;

/**
 * 集群各自节点 私有配置 部分(后期细化)
 */
public class HCSSystemConfig implements ToSortObject {
    /**
     * 发送心跳至Management(ms)
     */
    private Integer heartbeatToM;
    /**
     * 是否使用AIO，是：1，否：0
     */
    private Integer usingAIO;

    /**
     * @return {@link #heartbeatToM} 的值
     */
    public Integer getHeartbeatToM() {
	return heartbeatToM;
    }

    /**
     * @param heartbeatToM
     *            根据 heartbeatToM 设置 {@link #heartbeatToM}的值
     */
    public void setHeartbeatToM(Integer heartbeatToM) {
	this.heartbeatToM = heartbeatToM;
    }

    /**
     * @return {@link #usingAIO} 的值
     */
    public Integer getUsingAIO() {
	return usingAIO;
    }

    /**
     * @param usingAIO
     *            根据 usingAIO 设置 {@link #usingAIO}的值
     */
    public void setUsingAIO(Integer usingAIO) {
	this.usingAIO = usingAIO;
    }

    @Override
    public String toString() {
	return JSON.toJSONString(this);
    }

    /**
     * 
     * @see net.data.technology.jraft.CollectionUtil.ToSortObject#toSort()
     */
    @Override
    public void toSort() {
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
    }
}

