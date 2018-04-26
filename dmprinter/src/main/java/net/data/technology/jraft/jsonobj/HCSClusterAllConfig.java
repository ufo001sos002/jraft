package net.data.technology.jraft.jsonobj;

import java.util.List;

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

