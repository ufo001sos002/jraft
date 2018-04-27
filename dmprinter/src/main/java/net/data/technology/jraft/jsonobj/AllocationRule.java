package net.data.technology.jraft.jsonobj;

/**
 * 实例分配规则
 */
public class AllocationRule {
    /**
     * 规则：0为均分(默认) 所有节点轮询分配
     */
    public Integer rule;

    /**
     * @return {@link #rule} 的值
     */
    public Integer getRule() {
	return rule;
    }

    /**
     * @param rule
     *            根据 rule 设置 {@link #rule}的值
     */
    public void setRule(Integer rule) {
	this.rule = rule;
    }

}

