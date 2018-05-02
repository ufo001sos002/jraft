package net.data.technology.jraft.jsonobj;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;

import net.data.technology.jraft.CollectionUtil;
import net.data.technology.jraft.CollectionUtil.CloneAndSortObject;


/**
 * RDS实例具体信息 <br>
 * <b>注：如字段为null，则表示未设置该值</b>
 */
public class RDSInstanceInfo extends CloneAndSortObject<RDSInstanceInfo> {
    /**
     * 实例Id not null
     */
    private String rdsId;
    /**
     * 虚拟ip
     */
    private String vip;
    /**
     * 监听端口
     */
    private Integer port;
    /**
     * 实例类型 1：单机版 2：双机双主版 3：三机// MGR 4：三机双主带从 5：四机双主带从 6：六机双主带从
     */
    private Integer modeltype;
    /**
     * 最大连接数
     */
    private Integer maxConnections;
    /**
     * 用户前端最大连接数, 0为不限制
     */
    private Integer maxUserConnections;
    /**
     * 开始时间 yyyy-MM-dd HH:mm:ss 格式
     */
    private String startAt;
    /**
     * 到期时间 yyyy-MM-dd HH:mm:ss 格式
     */
    private String endAt;
    /**
     * 不开启读写分离：0；可分离的读请求发// 往所有可用数据源：1；可分离的读请求发往可用备数据源：2
     */
    private Integer strategyForRWSplit;
    /**
     * 从机读比例，默认50（百分比）,为0时代表该参数无效
     */
    private Integer weightForSlaveRWSplit;
    /**
     * RDS下 所有 MySQL信息
     */
    private List<MySQLInfo> mysqlInfos;
    /**
     * RDS下 所有数据库信息
     */
    private List<DBInfo> dbInfos;
    /**
     * RDS 实例下 MySQL 操作用户 集合 json数组
     */
    private List<UserInfo> userInfos;
    /**
     * IP 白名单信息
     */
    private IpWhites ipWhites;
    /**
     * SQL 拦截 数组,启用的规则项
     */
    private List<String> wallConfig;

    /**
     * 写状态：0为可写，1为不可写，只允许查询
     */
    private Integer writeStatus;

    /**
     * 状态 0：库存 1：已分配 2：到期 3：停用
     */
    private Integer status;

    /**
     * sql审计功能
     */
    private SqlAudit sqlAudit;

    /**
     * @return {@link #rdsId} 的值
     */
    public String getRdsId() {
        return rdsId;
    }

    /**
     * @param rdsId 根据 rdsId 设置 {@link #rdsId}的值
     */
    public void setRdsId(String rdsId) {
        this.rdsId = rdsId;
    }

    /**
     * @return {@link #vip} 的值
     */
    public String getVip() {
        return vip;
    }

    /**
     * @param vip 根据 vip 设置 {@link #vip}的值
     */
    public void setVip(String vip) {
        this.vip = vip;
    }

    /**
     * @return {@link #modeltype} 的值
     */
    public Integer getModeltype() {
        return modeltype;
    }

    /**
     * @param modeltype 根据 modeltype 设置 {@link #modeltype}的值
     */
    public void setModeltype(Integer modeltype) {
        this.modeltype = modeltype;
    }

    /**
     * @return {@link #maxConnections} 的值
     */
    public Integer getMaxConnections() {
        return maxConnections;
    }

    /**
     * @param maxConnections 根据 maxConnections 设置 {@link #maxConnections}的值
     */
    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * @return {@link #maxUserConnections} 的值
     */
    public Integer getMaxUserConnections() {
        return maxUserConnections;
    }

    /**
     * @param maxUserConnections 根据 maxUserConnections 设置 {@link #maxUserConnections}的值
     */
    public void setMaxUserConnections(Integer maxUserConnections) {
        this.maxUserConnections = maxUserConnections;
    }

    /**
     * @return {@link #startAt} 的值
     */
    public String getStartAt() {
        return startAt;
    }

    /**
     * @param startAt 根据 startAt 设置 {@link #startAt}的值
     */
    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    /**
     * @return {@link #endAt} 的值
     */
    public String getEndAt() {
        return endAt;
    }

    /**
     * @param endAt 根据 endAt 设置 {@link #endAt}的值
     */
    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    /**
     * @return {@link #strategyForRWSplit} 的值
     */
    public Integer getStrategyForRWSplit() {
        return strategyForRWSplit;
    }

    /**
     * @param strategyForRWSplit 根据 strategyForRWSplit 设置 {@link #strategyForRWSplit}的值
     */
    public void setStrategyForRWSplit(Integer strategyForRWSplit) {
        this.strategyForRWSplit = strategyForRWSplit;
    }

    /**
     * @return {@link #weightForSlaveRWSplit} 的值
     */
    public Integer getWeightForSlaveRWSplit() {
        return weightForSlaveRWSplit;
    }

    /**
     * @param weightForSlaveRWSplit 根据 weightForSlaveRWSplit 设置 {@link #weightForSlaveRWSplit}的值
     */
    public void setWeightForSlaveRWSplit(Integer weightForSlaveRWSplit) {
        this.weightForSlaveRWSplit = weightForSlaveRWSplit;
    }

    /**
     * @return {@link #mysqlInfos} 的值
     */

    public List<MySQLInfo> getMysqlInfos() {
        if (mysqlInfos == null) {
            mysqlInfos = new ArrayList<>();
        }
        return mysqlInfos;
    }

    /**
     * @param mysqlInfos 根据 mysqlInfos 设置 {@link #mysqlInfos}的值
     */
    public void setMysqlInfos(List<MySQLInfo> mysqlInfos) {
        this.mysqlInfos = mysqlInfos;
    }

    /**
     * @return {@link #userInfos} 的值 可能为null
     */
    public List<UserInfo> getUserInfos() {
        if (userInfos == null) {
            userInfos = new ArrayList<>();
        }
        return userInfos;
    }

    /**
     * @param userInfos 根据 userInfos 设置 {@link #userInfos}的值
     */
    public void setUserInfos(List<UserInfo> userInfos) {
        this.userInfos = userInfos;
    }

    /**
     * 增加用户
     * 
     * @param userInfo
     */
    public void addUserInfo(UserInfo userInfo) {
        if (this.userInfos == null) {
            this.userInfos = new ArrayList<UserInfo>();
        }
        this.userInfos.add(userInfo);
    }

    /**
     * 增加数据库信息
     * 
     * @param dbInfo
     */
    public void addDBInfo(DBInfo dbInfo) {
        if (this.dbInfos == null) {
            this.dbInfos = new ArrayList<DBInfo>();
        }
        dbInfos.add(dbInfo);
    }

    /**
     * 清除实例中用户
     * 
     * @param userInfo
     */
    public void removeUserInfo(UserInfo userInfo) {
        if (this.userInfos != null) {
            this.userInfos.remove(userInfo);
        }
    }

    /**
     * 清除实例中数据库
     * 
     * @param dbInfo
     */
    public void removeDBInfo(DBInfo dbInfo) {
        if (this.dbInfos != null) {
            this.dbInfos.remove(dbInfo);
        }
    }

    /**
     * @return {@link #ipWhites} 的值
     */
    public IpWhites getIpWhites() {
        return ipWhites;
    }

    /**
     * @param ipWhites 根据 ipWhites 设置 {@link #ipWhites}的值
     */
    public void setIpWhites(IpWhites ipWhites) {
        this.ipWhites = ipWhites;
    }

    /**
     * @return {@link #wallConfig} 的值
     */
    public List<String> getWallConfig() {
        if (wallConfig == null) {
            wallConfig = new ArrayList<>();
        }
        return wallConfig;
    }

    /**
     * @param wallConfig 根据 wallConfig 设置 {@link #wallConfig}的值
     */
    public void setWallConfig(List<String> wallConfig) {
        this.wallConfig = wallConfig;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    /**
     * @return {@link #port} 的值
     */
    public Integer getPort() {
        return port;
    }

    /**
     * @param port 根据 port 设置 {@link #port}的值
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * @return {@link #status} 的值
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @param status 根据 status 设置 {@link #status}的值
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * @return {@link #writeStatus} 的值
     */
    public Integer getWriteStatus() {
        return writeStatus;
    }

    /**
     * @param writeStatus 根据 writeStatus 设置 {@link #writeStatus}的值
     */
    public void setWriteStatus(Integer writeStatus) {
        this.writeStatus = writeStatus;
    }

    /**
     * 根据JSON字节数组 返回对象
     * 
     * @param jsonStr
     * @return
     */
    public static RDSInstanceInfo loadRDSInstanceFromJSONBytes(byte[] jsonBytes) {
        return loadRDSInstanceFromJSONStr(new String(jsonBytes));
    }

    /**
     * 根据JSON字符串 返回对象
     * 
     * @param jsonStr
     * @return
     */
    public static RDSInstanceInfo loadRDSInstanceFromJSONStr(String jsonStr) {
        return JSON.parseObject(jsonStr, RDSInstanceInfo.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see cn.hotpu.hotdb.util.CollectionUtil.SortObject#toSort()
     */
    @Override
    public void toSort() {
        if (this.mysqlInfos != null) {
            CollectionUtil.toSortObject(this.mysqlInfos);
        }
        if (this.userInfos != null) {
            CollectionUtil.toSortObject(this.userInfos);
        }
        if (this.ipWhites != null) {
            CollectionUtil.toSortObject(this.ipWhites);
        }
        if (this.wallConfig != null) {
            CollectionUtil.toSortBaseObject(this.wallConfig);
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see cn.hotpu.hotdb.util.CollectionUtil.CloneAndSortObject#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        RDSInstanceInfo clone = (RDSInstanceInfo) super.clone();
        if (this.mysqlInfos != null) {
            clone.mysqlInfos = CollectionUtil.cloneObject(this.mysqlInfos);
        }
        if (this.userInfos != null) {
            clone.userInfos = CollectionUtil.cloneObject(this.userInfos);
        }
        if (this.ipWhites != null) {
            clone.ipWhites = (IpWhites) this.ipWhites.clone();
        }
        if (this.wallConfig != null) {
            clone.wallConfig = CollectionUtil.cloneBaseObject(this.wallConfig);
        }
        return clone;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(RDSInstanceInfo o) {
        return this.rdsId != null && o.rdsId != null ? this.rdsId.compareTo(o.rdsId) : 0;
    }

    /**
     * sql审计
     * 
     * @return
     */
    public SqlAudit getSqlAudit() {
        return sqlAudit;
    }

    public void setSqlAudit(SqlAudit sqlAudit) {
        this.sqlAudit = sqlAudit;
    }

    /**
     * @return {@link #dbInfos} 的值
     */
    public List<DBInfo> getDbInfos() {
        return dbInfos;
    }

    /**
     * @param dbInfos 根据 dbInfos 设置 {@link #dbInfos}的值
     */
    public void setDbInfos(List<DBInfo> dbInfos) {
        this.dbInfos = dbInfos;
    }

}
