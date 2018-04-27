package net.data.technology.jraft.jsonobj;

import java.util.List;

/**
 * 实例分配
 */
public class RdsAllocation {
    /**
     * HCS节点id
     */
    public String hcsId;
    /**
     * 当前已分配的实例
     */
    public List<String> rdsIds;
    /**
     * 分配增加 监管的 实例Id(实例动态切换部分 使用)
     */
    public List<String> add;
    /**
     * 移出分配 监管的 实例Id(实例动态切换部分 使用)
     */
    public List<String> delete;

    /**
     * @return {@link #hcsId} 的值
     */
    public String getHcsId() {
	return hcsId;
    }

    /**
     * @param hcsId
     *            根据 hcsId 设置 {@link #hcsId}的值
     */
    public void setHcsId(String hcsId) {
	this.hcsId = hcsId;
    }

    /**
     * @return {@link #add} 的值
     */
    public List<String> getAdd() {
	return add;
    }

    /**
     * @param add
     *            根据 add 设置 {@link #add}的值
     */
    public void setAdd(List<String> add) {
	this.add = add;
    }

    /**
     * @return {@link #delete} 的值
     */
    public List<String> getDelete() {
	return delete;
    }

    /**
     * @param delete
     *            根据 delete 设置 {@link #delete}的值
     */
    public void setDelete(List<String> delete) {
	this.delete = delete;
    }

    /**
     * @return {@link #rdsIds} 的值
     */
    public List<String> getRdsIds() {
	return rdsIds;
    }

    /**
     * @param rdsIds
     *            根据 rdsIds 设置 {@link #rdsIds}的值
     */
    public void setRdsIds(List<String> rdsIds) {
	this.rdsIds = rdsIds;
    }

}

