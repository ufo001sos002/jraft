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
    public List<String> adds;
    /**
     * 移出分配 监管的 实例Id(实例动态切换部分 使用)
     */
    public List<String> deletes;

    /**
     * 
     * 根据参数构造 类{@link RdsAllocation} 对象
     * 
     * @param hcsId
     *            节点id
     */
    public RdsAllocation(String hcsId) {
	this.hcsId = hcsId;
    }

    /**
     * 
     * 根据参数构造 类{@link RdsAllocation} 对象
     * 
     * @param hcsId
     *            节点id
     * @param add
     *            当前分配增加的实例id
     */
    public RdsAllocation(String hcsId, List<String> adds) {
	this.hcsId = hcsId;
	this.adds = adds;
    }

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

    /**
     * @return 返回 {@link #adds}值
     */
    public List<String> getAdds() {
	return adds;
    }

    /**
     * @param 用参数adds设置
     *            {@link #adds}
     */
    public void setAdds(List<String> adds) {
	this.adds = adds;
    }

    /**
     * @return 返回 {@link #deletes}值
     */
    public List<String> getDeletes() {
	return deletes;
    }

    /**
     * @param 用参数deletes设置
     *            {@link #deletes}
     */
    public void setDeletes(List<String> deletes) {
	this.deletes = deletes;
    }


}

