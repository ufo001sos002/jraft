package net.data.technology.jraft.jsonobj;

import net.data.technology.jraft.CollectionUtil.ToSortObject;

/**
 * 集群所有配置JSON类
 * 
 */
public class HCSClusterAllConfig extends SocketRequest implements ToSortObject {

    /**
     * 私有配置 部分:集群各自节点运行参数
     */
    public HCSSystemConfig systemConfig;

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

