package net.data.technology.jraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.data.technology.jraft.jsonobj.MySQLInfo;

/**
 * MySQL数据源对象
 */
public class MySQLDataSource {

    private static final Logger logger = LoggerFactory.getLogger(MySQLDataSource.class);
    /**
     * MySQL 双主 主库[value={@value}]
     */
    public static final int TYPE_MASTER = 1;
    /**
     * MySQL 双主 备库[value={@value}]
     */
    public static final int TYPE_BACKUP = 2;
    /**
     * MySQL 只读库[value={@value}]
     */
    public static final int TYPE_READONLY = 3;
    /**
     * MySQL MGR[value={@value}]
     */
    public static final int TYPE_MGR = 4;
    /**
     * 状态 0不可用(故障)[value={@value}]
     */
    public static final int STATUS_ERROR = 0;
    /**
     * 状态 1可用[value={@value}]
     */
    public static final int STATUS_OK = 1;
    /**
     * 状态 2最后一个数据源 且异常[value={@value}]
     */
    public static final int STATUS_FAULT = 2;
    /**
     * 状态 3 已关闭[value={@value}]
     */
    public static final int STATUS_CLOSE = 3;

    /**
     * 所属RDS 实例 not null
     */
    private RDSInstance rdsInstance = null;
    /**
     * 实时配置JSON对象(本地保存依赖该对象,通讯时任何json变更成功均需同步该对象变更并保存)
     */
    private MySQLInfo mysqlInfo = null;

    /**
     * @return {@link #rdsInstance} 的值
     */
    public RDSInstance getRdsInstance() {
	return rdsInstance;
    }

    /**
     * @param rdsInstance
     *            根据 rdsInstance 设置 {@link #rdsInstance}的值
     */
    public void setRdsInstance(RDSInstance rdsInstance) {
	this.rdsInstance = rdsInstance;
    }

    /**
     * @return {@link #mysqlInfo} 的值
     */
    public MySQLInfo getMysqlInfo() {
	return mysqlInfo;
    }

    /**
     * @param mysqlInfo
     *            根据 mysqlInfo 设置 {@link #mysqlInfo}的值
     */
    public void setMysqlInfo(MySQLInfo mysqlInfo) {
	this.mysqlInfo = mysqlInfo;
    }

    /**
     * @return true 表示是主
     */
    public boolean isMaster() {
	return TYPE_MASTER == this.mysqlInfo.getMasterSlaveType();
    }

    /**
     * @return true 表示从库(只读)
     */
    public boolean isReadOnly() {
	return TYPE_READONLY == this.mysqlInfo.getMasterSlaveType();
    }

    /**
     * 初始化 对象
     * 
     * @param mysqlInfo
     * @return
     * @throws Exception
     *             解密MySQL实例管理密码失败
     */
    public static MySQLDataSource initMySQLDataSource(RDSInstance rdsInstance, MySQLInfo mysqlInfo,
	    boolean isCryptMandatory) {
	MySQLDataSource mysqlDataSource = new MySQLDataSource();
	mysqlDataSource.setRdsInstance(rdsInstance);
	mysqlDataSource.setMysqlInfo(mysqlInfo);
	if (mysqlInfo.getId() == null) {
	    logger.warn(Markers.CONFIG, "init MySQL Instance id is null");
	}
	if (mysqlInfo.getIp() == null) {
	    logger.warn(Markers.CONFIG, "init MySQL Instance ip is null");
	}
	if (mysqlInfo.getPort() == null) {
	    logger.warn(Markers.CONFIG, "init MySQL Instance port is null");
	}
	if (mysqlInfo.getManageUser() == null) {
	    logger.warn(Markers.CONFIG, "init MySQL Instance manager user is null");
	    return null;
	}
	if (mysqlInfo.getManagePassword() == null) {
	    logger.warn(Markers.CONFIG, "init MySQL Instance manager password is null");
	}
	if (mysqlInfo.getMasterSlaveType() == null) {
	    logger.warn(Markers.CONFIG, "init MySQL Instance master/slave Type is null");
	}
	if (mysqlInfo.getCopyfromId() == null && mysqlDataSource.isReadOnly()) {
		logger.warn(Markers.CONFIG,
			"init MySQL Instance slave(read Only) copy from id is null");
	}
	if (mysqlInfo.getCopyfromIp() == null && mysqlDataSource.isReadOnly()) {
	    logger.warn(Markers.CONFIG,
			"init MySQL Instance slave(read Only) copy from ip is null");
	}
	if (mysqlInfo.getCopyfromPort() == null && mysqlDataSource.isReadOnly()) {
	    logger.warn(Markers.CONFIG,
			"init MySQL Instance slave(read Only) copy from ip is null");
	}
	if (mysqlInfo.getVersion() == null) {
	    logger.warn(Markers.CONFIG, "init MySQL Instance version is null");
	}
	return mysqlDataSource;
    }
}

