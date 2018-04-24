/*
 * Copyright (c) 2013, OpenCloudDB/HotDB and/or its affiliates. All rights reserved. DO NOT ALTER OR
 * REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This code is free software;Designed and Developed mainly by many Chinese opensource volunteers.
 * you can redistribute it and/or modify it under the terms of the GNU General Public License
 * version 2 only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version 2 along with this work;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address
 * https://code.google.com/p/opencloudb/.
 */
package net.data.technology.jraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author hotdb
 */
public class CollectionUtil {
    /**
     * @param orig if null, return intersect
     */
    public static Set<? extends Object> intersectSet(Set<? extends Object> orig,
            Set<? extends Object> intersect) {
        if (orig == null)
            return intersect;
        if (intersect == null || orig.isEmpty())
            return Collections.emptySet();
        Set<Object> set = new HashSet<Object>(orig.size());
        for (Object p : orig) {
            if (intersect.contains(p))
                set.add(p);
        }
        return set;
    }

    /**
     * 判断集合不为null 且 size > num
     * 
     * @param c
     * @param num
     * @return
     */
    public static boolean checkCollectionSizeGTNum(Collection c, int num) {
        return c != null && c.size() > num;
    }

    /**
     * 判断集合不为null 且 size > num
     * 
     * @param c
     * @param num
     * @return
     */
    public static boolean checkCollectionIsNotEmpty(Collection c) {
        return checkCollectionSizeGTNum(c, 0);
    }

    /**
     * 克隆并排序对象
     * 
     * @param list not null T 为基础类型对象
     * @return 新的已经排序过克隆的对象
     * @throws CloneNotSupportedException
     */
    private static <T extends Comparable<? super T>> List<T> cloneAndSortBaseObject(List<T> list,
            boolean toSort)
            throws CloneNotSupportedException {
        List<T> cloneList = new ArrayList<T>();
        cloneList.addAll(list);
        if (toSort) {
            Collections.sort(cloneList);
        }
        return cloneList;
    }

    /**
     * 克隆并排序对象(对象需实现{@link CloneAndSortObject})
     * 
     * @param list not null T 为非基础类型
     * @return 新的已经排序过克隆的对象
     * @throws CloneNotSupportedException
     */
    private static <T extends CloneAndSortObject<? super T>> List<T> cloneAndSortObject(
            List<T> list,
            boolean toSort) throws CloneNotSupportedException {
        List<T> cloneList = new ArrayList<T>();
        for (T t : list) {
            cloneList.add((T) t.clone());
        }
        if (toSort) {
            Collections.sort(cloneList);
        }
        return cloneList;
    }

    /**
     * 克隆并排序对象
     * 
     * @param list not null T 为基础类型对象
     * @return 新的已经排序过克隆的对象
     * @throws CloneNotSupportedException
     */
    public static <T extends Comparable<? super T>> List<T> cloneAndSortBaseObject(List<T> list)
            throws CloneNotSupportedException {
        return cloneAndSortBaseObject(list, true);
    }

    /**
     * 克隆并排序对象(对象需实现{@link CloneAndSortObject})
     * 
     * @param list not null T 为非基础类型
     * @return 新的已经排序过克隆的对象
     * @throws CloneNotSupportedException
     */
    public static <T extends CloneAndSortObject<? super T>> List<T> cloneAndSortObject(List<T> list)
            throws CloneNotSupportedException {
        return cloneAndSortObject(list, true);
    }

    /**
     * 克隆对象
     * 
     * @param list not null T 为基础类型对象
     * @return 新的克隆的对象
     * @throws CloneNotSupportedException
     */
    public static <T extends Comparable<? super T>> List<T> cloneBaseObject(List<T> list)
            throws CloneNotSupportedException {
        return cloneAndSortBaseObject(list, false);
    }

    /**
     * 克隆对象(对象需实现{@link CloneAndSortObject})
     * 
     * @param list not null T 为非基础类型
     * @return 新的克隆的对象
     * @throws CloneNotSupportedException
     */
    public static <T extends CloneAndSortObject<? super T>> List<T> cloneObject(List<T> list)
            throws CloneNotSupportedException {
        return cloneAndSortObject(list, false);
    }

    /**
     * 排序集合中对象以及对象中属性(对象需实现{@link CloneAndSortObject})
     * 
     * @param list not null T 为非基础类型
     * @return 新的克隆的对象
     */
    public static <T extends CloneAndSortObject<? super T>> void toSortObject(List<T> list) {
        Collections.sort(list);
        for (T t : list) {
            t.toSort();
        }
    }

    /**
     * 排序集合中对象以及对象中属性(对象需实现{@link CloneAndSortObject})
     * 
     * @param list not null T 为非基础类型
     * @return 新的克隆的对象
     */
    public static void toSortObject(ToSortObject toSortObject) {
        toSortObject.toSort();
    }

    /**
     * 排序对象
     * 
     * @param list not null T 为基础类型对象
     * @return 新的已经排序过克隆的对象
     * @throws CloneNotSupportedException
     */
    public static <T extends Comparable<? super T>> void toSortBaseObject(List<T> list) {
        Collections.sort(list);
    }

    /**
     * 
     */
    public static interface ToSortObject extends Cloneable {
        /**
         * 对该对象子类中的 子类对象属性、之类对象集合属性 进行排序,以便输出等
         */
        public void toSort();
    }

    public static abstract class CloneAndSortObject<T> implements ToSortObject, Comparable<T> {

        /*
         * 
         * @see java.lang.Object#clone()
         */
        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

    }
}
