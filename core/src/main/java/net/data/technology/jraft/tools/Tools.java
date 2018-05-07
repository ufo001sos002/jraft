package net.data.technology.jraft.tools;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 通用工具类
 */
public class Tools {

    /**
     * 参照 {@link Thread#sleep(long)} 但是不抛错
     * 
     * @param millis
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    /**
     * toString
     * 
     * @param obj
     * @return obj 为null 时返回字符串"null",否则 调用 toString方法
     */
    public static String toString(Object obj) {
        return obj != null ? obj.toString() : "null";
    }

    /**
     * 返回对比集合 不在 目标集合 中元素的集合
     * 
     * @param oldList
     *            对比集合
     * @param targetList
     *            目标集合
     * @return not null
     */
    public static <E> ArrayList<E> getExclusiveList(Collection<E> oldList, Collection<E> targetList) {
	ArrayList<E> exclusiveList = new ArrayList<E>();
	for (E e : oldList) {
	    if (!targetList.contains(e)) {
		exclusiveList.add(e);
	    }
	}
	return exclusiveList;
    }


    public static void main(String[] args) {
        // String s = "1";
        // Integer i = new Integer(10);
        // ArrayList<String> ss = new ArrayList<String>();
        // ss.add("aa");
        // ss.add("bb");
        // System.out.println(toString(s));
        // System.out.println(toString(i));
        // System.out.println(toString(ss));
    }
}
