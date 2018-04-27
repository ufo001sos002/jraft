package net.data.technology.jraft.jsonobj;

import java.util.List;

import com.alibaba.fastjson.JSON;

import net.data.technology.jraft.CollectionUtil.ToSortObject;

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
    private List<RDSInstance> rdsInstances;

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
    public List<RDSInstance> getRdsInstances() {
	return rdsInstances;
    }

    /**
     * @param rdsInstances
     *            根据 rdsInstances 设置 {@link #rdsInstances}的值
     */
    public void setRdsInstances(List<RDSInstance> rdsInstances) {
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

    public static HCSClusterAllConfig loadObjectFromJSONString(String jsonStr) {
	return JSON.parseObject(jsonStr, HCSClusterAllConfig.class);
    }

    /**
     * 
     * @param args 
     */
    public static void main(String[] args) {

    }

}

