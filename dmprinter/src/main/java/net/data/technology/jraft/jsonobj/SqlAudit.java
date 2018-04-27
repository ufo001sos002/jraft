package net.data.technology.jraft.jsonobj;

import java.util.List;

/**
 * sql审计功能
 *
 * @author
 * @date 2017年11月27日 上午10:52:37
 *
 */
public class SqlAudit {

    /**
     * 审计规则
     */
    private List<String> rule;
    /**
     * //是否启用 （停用则其他参数均可不传）
     */
    private boolean isEnable;

    public List<String> getRule() {
        return rule;
    }

    public void setRule(List<String> rule) {
        this.rule = rule;
    }

    public boolean getEnable() {
        return isEnable;
    }

    public void setEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }



}

