package net.data.technology.jraft.jsonobj;

import com.alibaba.fastjson.JSON;

import net.data.technology.jraft.CollectionUtil.CloneAndSortObject;


/**
 * RDS下 数据库详细信息 <br>
 * <b>注：如字段为null，则表示未设置该值</b>
 */
public class DBInfo extends CloneAndSortObject<DBInfo> {
    /**
     * 数据库ID <b>暂未使用</b>
     */
    private Integer dbId;
    /**
     * 数据库名称
     */
    private String databaseName;
    /**
     * 数据库字符集<b>暂未使用</b>
     */
    private String characterset;

    /**
     * @return {@link #dbId} 的值
     */
    public Integer getDbId() {
        return dbId;
    }

    public DBInfo() {

    }

    public DBInfo(String databaseName) {
        super();
        this.databaseName = databaseName;
    }



    /**
     * @param dbId 根据 dbId 设置 {@link #dbId}的值
     */
    public void setDbId(Integer dbId) {
        this.dbId = dbId;
    }

    /**
     * @return {@link #databaseName} 的值
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * @param databaseName 根据 databaseName 设置 {@link #databaseName}的值
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * @return {@link #characterset} 的值
     */
    public String getCharacterset() {
        return characterset;
    }

    /**
     * @param characterset 根据 characterset 设置 {@link #characterset}的值
     */
    public void setCharacterset(String characterset) {
        this.characterset = characterset;
    }

    /**
     * 根据JSON字节数组 返回对象
     * 
     * @param jsonStr
     * @return
     */
    public static DBInfo loadDBInfoFromJSONBytes(byte[] jsonBytes) {
        return loadDBInfoFromJSONStr(new String(jsonBytes));
    }

    /**
     * 根据JSON字符串 返回对象
     * 
     * @param jsonStr
     * @return
     */
    public static DBInfo loadDBInfoFromJSONStr(String jsonStr) {
        return JSON.parseObject(jsonStr, DBInfo.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(DBInfo o) {
        return this.dbId != null && o.dbId != null ? this.dbId - o.dbId : 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cn.hotpu.hotdb.util.CollectionUtil.CloneAndSortObject#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /*
     * (non-Javadoc)
     * 
     * @see cn.hotpu.hotdb.util.CollectionUtil.ToSortObject#toSort()
     */
    @Override
    public void toSort() {}

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
            DBInfo t = (DBInfo) obj;
            if (this.databaseName != null
                    && this.databaseName.equalsIgnoreCase((t.getDatabaseName()))) {
                return true;
            }
        }
        return false;
    }

}
