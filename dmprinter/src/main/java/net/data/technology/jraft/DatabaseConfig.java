package net.data.technology.jraft;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.data.technology.jraft.jsonobj.DBInfo;

/**
 * RDS 实例数据库对象
 * 
 */
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    public static final Charset UTF8_FOR_JAVA = Charset.forName("UTF-8");
    public static final Charset LATIN1_FOR_JAVA = Charset.forName("ISO-8859-1");
    public static final Charset GBK_FOR_JAVA = Charset.forName("GBK");
    /**
     * 数据库ID <b>暂未使用</b>
     */
    private int dbId;
    /**
     * 数据库名称
     */
    private String databaseName;
    /**
     * 数据库字符集<b>暂未使用</b>
     */
    private Charset characterset;
    /**
     * json对象
     */
    private DBInfo dbInfo;

    /**
     * 根据字符串 返回Java对应的字符集
     * 
     * @param charset
     * @return
     */
    public static Charset getCharsetForJava(String charset) {
	switch (charset.toUpperCase()) {
	case "UTF8":
	case "UTF8MB4":
	case "BINARY":
	    return UTF8_FOR_JAVA;
	case "LATIN1":
	    return LATIN1_FOR_JAVA;
	case "GBK":
	    return GBK_FOR_JAVA;
	default:
	    return UTF8_FOR_JAVA;
	}
    }

    /**
     * 
     * <p>
     * Description: 构造对象
     * </p>
     * 
     * @param dbInfo 数据库信息JSON对象
     */
    public DatabaseConfig(DBInfo dbInfo) {
        this.dbInfo = dbInfo;
        if (dbInfo.getDatabaseName() != null) {
            setDatabaseName(dbInfo.getDatabaseName().toUpperCase());
        }
        if (dbInfo.getDbId() != null) {
            setDbId(dbInfo.getDbId());
        }
        if (dbInfo.getCharacterset() != null) {
	    setCharacterset(getCharsetForJava(dbInfo.getCharacterset()));
        }
    }

    /**
     * @return {@link #dbId} 的值
     */
    public int getDbId() {
        return dbId;
    }

    /**
     * @param dbId 根据 dbId 设置 {@link #dbId}的值
     */
    public void setDbId(int dbId) {
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
        this.databaseName = databaseName.toUpperCase();
    }

    /**
     * @return {@link #characterset} 的值
     */
    public Charset getCharacterset() {
        return characterset;
    }

    /**
     * @param characterset 根据 characterset 设置 {@link #characterset}的值
     */
    public void setCharacterset(Charset characterset) {
        this.characterset = characterset;
    }

    /**
     * @return {@link #dbInfo} 的值
     */
    public DBInfo getDbInfo() {
        return dbInfo;
    }

    /**
     * 根据JSON对象 生成本对象
     * 
     * @param dbInfo
     * @return
     */
    public static DatabaseConfig initDatabaseConfig(DBInfo dbInfo) {
        if (dbInfo.getDatabaseName() == null) {
            logger.error(Markers.CONFIG, "init RDS Instance databases name is null");
        }
        return new DatabaseConfig(dbInfo);
    }
}
