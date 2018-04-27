package net.data.technology.jraft.jsonobj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.JSON;

import net.data.technology.jraft.CollectionUtil;
import net.data.technology.jraft.CollectionUtil.CloneAndSortObject;


/**
 * RDS 实例下 MySQL 操作用户 <br>
 * <b>注：如字段为null，则表示未设置该值</b>
 */
public class UserInfo extends CloneAndSortObject<UserInfo> {
    /**
     * 增加 操作 [={@value}]
     */
    public static final int OPTYPE_ADD = 1;
    /**
     * 更新 操作 [={@value}]
     */
    public static final int OPTYPE_UPDATE = 2;
    /**
     * 删除 操作 [={@value}]
     */
    public static final int OPTYPE_DELETE = 3;
    /**
     * 用户id
     */
    public Integer id;
    /**
     * 用户名 不能为null
     */
    private String userName;
    /**
     * 密码
     */
    private String password;
    /**
     * 主机字符串
     */
    private String host;
    /**
     * 可访问的数据库名
     */
    private List<String> databases;
    /**
     * 1:新增{@link #OPTYPE_ADD} 2:修改{@link #OPTYPE_UPDATE} 3:删除{@link #OPTYPE_DELETE}
     */
    private Integer opType;

    /**
     * @return {@link #userName} 的值 不为null
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName 根据 userName 设置 {@link #userName}的值 不能为null
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return {@link #password} 的值
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password 根据 password 设置 {@link #password}的值
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    /**
     * @return {@link #opType} 的值
     */
    public Integer getOpType() {
        return opType;
    }

    /**
     * @param opType 根据 opType 设置 {@link #opType}的值
     */
    public void setOpType(Integer opType) {
        this.opType = opType;
    }

    /**
     * @return {@link #host} 的值
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host 根据 host 设置 {@link #host}的值
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return {@link #databases} 的值
     */
    public List<String> getDatabases() {
        if (databases == null) {
            databases = new ArrayList<>();
        }
        return databases;
    }

    /**
     * @param databases 根据 databases 设置 {@link #databases}的值
     */
    public void setDatabases(List<String> databases) {
        this.databases = databases;
    }


    /**
     * @return {@link #id} 的值
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id 根据 id 设置 {@link #id}的值
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 根据JSON字节数组 返回对象
     * 
     * @param jsonStr
     * @return
     */
    public static UserInfo loadUserInfoFromJSONBytes(byte[] jsonBytes) {
        return loadUserInfoFromJSONStr(new String(jsonBytes));
    }

    /**
     * 根据JSON字符串 返回对象
     * 
     * @param jsonStr
     * @return
     */
    public static UserInfo loadUserInfoFromJSONStr(String jsonStr) {
        return JSON.parseObject(jsonStr, UserInfo.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see cn.hotpu.hotdb.util.CollectionUtil.SortObject#toSort()
     */
    @Override
    public void toSort() {
        if (this.databases != null) {
            Collections.sort(this.databases);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(UserInfo o) {
        return this.id != null && o.id != null ? this.id - o.id : 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cn.hotpu.hotdb.util.CollectionUtil.CloneAndSortObject#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        UserInfo clone = (UserInfo) super.clone();
        if (this.databases != null) {
            clone.databases = CollectionUtil.cloneBaseObject(this.databases);
        }
        return clone;
    }

    /**
     * 
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj != null) {
            UserInfo t = (UserInfo) obj;
            if (this.userName != null && this.userName.equals(t.getUserName()) && this.id != null
                    && this.id.equals(t.getId())) {
                return true;
            }
        }
        return false;
    }


}
