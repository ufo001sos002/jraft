package net.data.technology.jraft.jsonobj;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;

import net.data.technology.jraft.CollectionUtil.ToSortObject;
import net.data.technology.jraft.Middleware;

/**
 * 集群所有配置JSON类
 * 
 */
public class HCSClusterAllConfig extends SocketRequest implements ToSortObject {

    /**
     * 私有配置 部分:集群各自节点运行参数
     */
    private HCSSystemConfig systemConfig;

    /**
     * 公有配置 部分：该组集群 所有节点信息
     */
    private List<HCSNode> hcsGroup;

    /**
     * 公有配置 部分：集群中所需要处理的 实例集合 RDS实例具体信息
     */
    private List<RDSInstanceInfo> rdsInstances;

    /**
     * 公有配置 部分：实例分配规则
     */
    private AllocationRule allocationRule;
    /**
     * 分配配置 部分：实例分配
     */
    private List<RdsAllocation> rdsAllocations;

    /**
     * @return {@link #systemConfig} 的值
     */
    public HCSSystemConfig getSystemConfig() {
	return systemConfig;
    }

    /**
     * @param systemConfig
     *            根据 systemConfig 设置 {@link #systemConfig}的值
     */
    public void setSystemConfig(HCSSystemConfig systemConfig) {
	this.systemConfig = systemConfig;
    }

    /**
     * @return {@link #hcsGroup} 的值
     */
    public List<HCSNode> getHcsGroup() {
	return hcsGroup;
    }

    /**
     * @param hcsGroup
     *            根据 hcsGroup 设置 {@link #hcsGroup}的值
     */
    public void setHcsGroup(List<HCSNode> hcsGroup) {
	this.hcsGroup = hcsGroup;
    }

    /**
     * @return {@link #rdsInstances} 的值
     */
    public List<RDSInstanceInfo> getRdsInstances() {
	return rdsInstances;
    }

    /**
     * @param rdsInstances
     *            根据 rdsInstances 设置 {@link #rdsInstances}的值
     */
    public void setRdsInstances(List<RDSInstanceInfo> rdsInstances) {
	this.rdsInstances = rdsInstances;
    }

    /**
     * @return {@link #allocationRule} 的值
     */
    public AllocationRule getAllocationRule() {
	return allocationRule;
    }

    /**
     * @param allocationRule
     *            根据 allocationRule 设置 {@link #allocationRule}的值
     */
    public void setAllocationRule(AllocationRule allocationRule) {
	this.allocationRule = allocationRule;
    }

    /**
     * @return {@link #rdsAllocations} 的值
     */
    public List<RdsAllocation> getRdsAllocations() {
	return rdsAllocations;
    }

    /**
     * @param rdsAllocations
     *            根据 rdsAllocations 设置 {@link #rdsAllocations}的值
     */
    public void setRdsAllocations(List<RdsAllocation> rdsAllocations) {
	this.rdsAllocations = rdsAllocations;
    }

    /**
     * 
     * @see net.data.technology.jraft.CollectionUtil.ToSortObject#toSort()
     */
    @Override
    public void toSort() {

    }

    @Override
    public String toString() {
	return JSON.toJSONString(this);
    }

    /**
     * 根据JSON字符串生成对象
     * 
     * @param jsonStr
     *            JSON字符串
     * @return {@link HCSClusterAllConfig} 对象
     */
    public static HCSClusterAllConfig loadObjectFromJSONString(String jsonStr) {
	return JSON.parseObject(jsonStr, HCSClusterAllConfig.class);
    }

    /**
     * 将对象值以json形式写入文件
     * 
     * @param fileName
     * @throws IOException
     */
    public void writeToFile(String fileName) throws IOException {
	File folder = new File(Middleware.getHomePath(), "conf" + File.separator);
	if (!folder.exists()) {
	    folder.mkdirs();
	}
	File file = new File(Middleware.getHomePath(), "conf" + File.separator + fileName);
	if (!file.exists()) {
	    file.createNewFile();
	}
	FileOutputStream fileOutputStream = new FileOutputStream(file);
	fileOutputStream.write(toString().getBytes(Middleware.SYSTEM_CHARSET));
	fileOutputStream.flush();
	fileOutputStream.close();
    }

    /**
     * 从指定文件名对应文件中提取 {@link HCSClusterAllConfig} 对象
     * 
     * @param fileName
     *            指定文件名
     * @return {@link HCSClusterAllConfig} 对象
     * @throws IOException
     */
    public static HCSClusterAllConfig loadFromFile(String fileName) throws IOException {
	File folder = new File(Middleware.getHomePath(), "conf" + File.separator);
	if (!folder.exists()) {
	    folder.mkdirs();
	    return null;
	}
	File file = new File(Middleware.getHomePath(), "conf" + File.separator + fileName);
	if (!file.exists()) {
	    file.createNewFile();
	    return null;
	}
	InputStream in = new FileInputStream(file);
	HCSClusterAllConfig obj = JSON.parseObject(in, HCSClusterAllConfig.class, new Feature[0]);
	in.close();
	return obj;
    }

    /**
     * 
     * @param args 
     */
    public static void main(String[] args) {

    }

}

