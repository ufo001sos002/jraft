package net.data.technology.jraft;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.data.technology.jraft.jsonobj.DBInfo;
import net.data.technology.jraft.jsonobj.MySQLInfo;
import net.data.technology.jraft.jsonobj.RDSInstanceInfo;

/**
 * 实例相关对象
 */
public class RDSInstance {
    private static final Logger logger = LoggerFactory.getLogger(RDSInstance.class);
    /**
     * 实例默认端口
     */
    public static final int DEFAULTPORT = 3306;
    /**
     * 实例库存状态
     */
    public static final int RDS_INSTANCE_STATUS_STOCK = 0;
    /**
     * 实例已分配状态
     */
    public static final int RDS_INSTANCE_STATUS_ALLOCATED = 1;
    /**
     * 实例过期状态
     */
    public static final int RDS_INSTANCE_STATUS_EXPIRE = 2;
    /**
     * 实例手动停用状态
     */
    public static final int RDS_INSTANCE_STATUS_STOP = 3;
    /**
     * 实例类型 1：单机版[value={@value}]
     */
    public static final int MODELTYPE_SIMPLE = 1;
    /**
     * 实例类型 2：双机双主版[value={@value}]
     */
    public static final int MODELTYPE_DOUBLE_MASTER_2 = 2;
    /**
     * 实时配置JSON对象(本地保存依赖该对象,通讯时任何json变更成功均需同步该对象变更并保存) not null
     */
    private RDSInstanceInfo rdsInstanceInfo;
    
    /**
     * 所有 MySQL实例详细信息列表 KEY : Ip,port not null
     */
    private ConcurrentHashMap<Integer, MySQLDataSource> mysqlDataSourceMap = new ConcurrentHashMap<Integer, MySQLDataSource>();
    /**
     * 数据库对象集合 TODO 后续二期可能需要考虑 数据库 等等 各种字符集处理
     */
    private ConcurrentHashMap<String, DatabaseConfig> databaseConfigMap = new ConcurrentHashMap<String, DatabaseConfig>();
    /**
     * 前端连接集合
     */
    private ConcurrentHashMap<Long, FrontendConnection> frontendConnMap = new ConcurrentHashMap<Long, FrontendConnection>();

    /**
     * 当前连接数据源 实例 not null
     */
    private MySQLDataSource currentDataSource = null;

    /**
     * @return {@link #rdsInstanceInfo} 的值
     */
    public RDSInstanceInfo getRdsInstanceInfo() {
	return rdsInstanceInfo;
    }

    /**
     * @param rdsInstanceInfo
     *            根据 rdsInstanceInfo 设置 {@link #rdsInstanceInfo}的值
     */
    public void setRdsInstanceInfo(RDSInstanceInfo rdsInstanceInfo) {
	this.rdsInstanceInfo = rdsInstanceInfo;
    }

    /**
     * @return {@link #mysqlDataSourceMap} 的值
     */
    public ConcurrentHashMap<Integer, MySQLDataSource> getMysqlDataSourceMap() {
	return mysqlDataSourceMap;
    }

    /**
     * @return {@link #databaseConfigMap} 的值
     */
    public ConcurrentHashMap<String, DatabaseConfig> getDatabaseConfigMap() {
	return databaseConfigMap;
    }

    /**
     * @return {@link #frontendConnMap} 的值
     */
    public ConcurrentHashMap<Long, FrontendConnection> getFrontendConnMap() {
	return frontendConnMap;
    }

    /**
     * @return {@link #currentDataSource} 的值
     */
    public MySQLDataSource getCurrentDataSource() {
	return currentDataSource;
    }

    /**
     * @param currentDataSource
     *            根据 currentDataSource 设置 {@link #currentDataSource}的值
     */
    public void setCurrentDataSource(MySQLDataSource currentDataSource) {
	this.currentDataSource = currentDataSource;
    }

    /**
     * 根据参数 初始化配置对象
     * 
     * @param rdsInstance
     * @return
     * @throws Exception
     *             解密MySQL实例相关用户密码失败
     */
    public static RDSInstance initRDSInstance(RDSInstanceInfo rdsInstanceInfo, boolean isCryptMandatory) {
	RDSInstance rdsInstance = new RDSInstance();
	rdsInstance.setRdsInstanceInfo(rdsInstanceInfo);
	if (rdsInstanceInfo.getRdsId() == null) {
	    logger.error(Markers.CONFIG, "init RDS Instance id is null");
	}
	if (rdsInstanceInfo.getModeltype() == null) {
	    logger.error(Markers.CONFIG, "init RDS Instance type is null");
	}
	if (rdsInstanceInfo.getStatus() == null) {
	    logger.error(Markers.CONFIG, "init RDS Instance status is null");
	}
	if (rdsInstanceInfo.getStatus() >= RDS_INSTANCE_STATUS_ALLOCATED && rdsInstanceInfo.getVip() == null) {
	    logger.error(Markers.CONFIG, "init RDS allocated Instance virtual ip is null");
	}
	if (rdsInstanceInfo.getMysqlInfos() != null && rdsInstanceInfo.getMysqlInfos().size() > 0) {
	    ConcurrentHashMap<Integer, MySQLDataSource> mysqlDataSourceMap = rdsInstance.getMysqlDataSourceMap();
	    MySQLDataSource mysqlDataSource = null;
	    for (MySQLInfo mysqlInfo : rdsInstanceInfo.getMysqlInfos()) {
		mysqlDataSource = MySQLDataSource.initMySQLDataSource(rdsInstance, mysqlInfo, isCryptMandatory);
		if (mysqlDataSource == null) {
		    logger.error(Markers.CONFIG, "init RDS Instance mysql config is error");
		    continue;
		}
		if (mysqlDataSource.isMaster()) {
		    rdsInstance.setCurrentDataSource(mysqlDataSource);
		}
		mysqlDataSourceMap.put(mysqlInfo.getId(), mysqlDataSource);
	    }
	} else {
	    logger.error(Markers.CONFIG, "init RDS Instance MySQL instance is empty");
	}
	if (rdsInstanceInfo.getDbInfos() != null) {
	    ConcurrentHashMap<String, DatabaseConfig> databaseConfigMap = rdsInstance.getDatabaseConfigMap();
	    DatabaseConfig databaseConfig = null;
	    for (DBInfo dbInfo : rdsInstanceInfo.getDbInfos()) {
		databaseConfig = DatabaseConfig.initDatabaseConfig(dbInfo);
		if (databaseConfig == null) {
		    logger.error(Markers.CONFIG,
			    "init RDS Instance mysql database config is error:" + dbInfo);
		}
		databaseConfigMap.put(databaseConfig.getDatabaseName(), databaseConfig);
	    }
	}
	return rdsInstance;
    }

    /**
     * 关闭实例连接
     * 
     * @param reason
     */
    public void closeConnection(String reason) {
	for (FrontendConnection conn : frontendConnMap.values()) {
	    conn.close(reason);
	}
    }
}

