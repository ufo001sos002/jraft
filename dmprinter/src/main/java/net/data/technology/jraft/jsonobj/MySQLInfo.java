package net.data.technology.jraft.jsonobj;

import com.alibaba.fastjson.JSON;

import net.data.technology.jraft.CollectionUtil.CloneAndSortObject;


/**
 * RDS下 单台MySQL详细信息 <br>
 * <b>注：如字段为null，则表示未设置该值</b>
 */
public class MySQLInfo extends CloneAndSortObject<MySQLInfo> {
    /**
     * MySQL ID 不能为null
     */
    private Integer id;
    /**
     * MySQL版本
     */
    private String version;
    /**
     * MySQL IP 不能为null
     */
    private String ip;
    /**
     * MySQL port 不能为null
     */
    private Integer port;
    /**
     * 拥有最大权限的MySQL账户,有操作mysql库的权限
     */
    private String manageUser;
    /**
     * 拥有最大权限的MySQL账户密码 根据{@link RDSServerGroupInfo#cryptMandatory} 判断是否加密
     */
    private String managePassword;
    /**
     * 主从类型 1:主库、2:双主备库、// 3:从库(只读) 、4:MGR
     */
    private Integer masterSlaveType;
    /**
     * 复制源id
     */
    private Integer copyfromId;
    /**
     * 复制源ip
     */
    private String copyfromIp;
    /**
     * 复制源端口
     */
    private Integer copyfromPort;
    /**
     * 1为可用 0为不可用 2最后一个数据源 且异常 <br>
     */
    private Integer status;


    /**
     * @return {@link #version} 的值
     */

    public String getVersion() {
        return version;
    }

    /**
     * @param version 根据 version 设置 {@link #version}的值
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return {@link #ip} 的值
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip 根据 ip 设置 {@link #ip}的值 不能为null
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return {@link #port} 的值
     */
    public Integer getPort() {
        return port;
    }

    /**
     * @param port 根据 port 设置 {@link #port}的值 不能为null
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * @return {@link #manageUser} 的值
     */
    public String getManageUser() {
        return manageUser;
    }

    /**
     * @param manageUser 根据 manageUser 设置 {@link #manageUser}的值
     */
    public void setManageUser(String manageUser) {
        this.manageUser = manageUser;
    }

    /**
     * @return {@link #managePassword} 的值
     */
    public String getManagePassword() {
        return managePassword;
    }

    /**
     * @param managePassword 根据 managePassword 设置 {@link #managePassword}的值
     */
    public void setManagePassword(String managePassword) {
        this.managePassword = managePassword;
    }

    /**
     * @return {@link #masterSlaveType} 的值
     */
    public Integer getMasterSlaveType() {
        return masterSlaveType;
    }

    /**
     * @param masterSlaveType 根据 masterSlaveType 设置 {@link #masterSlaveType}的值
     */
    public void setMasterSlaveType(Integer masterSlaveType) {
        this.masterSlaveType = masterSlaveType;
    }

    /**
     * @return {@link #copyfromIp} 的值
     */
    public String getCopyfromIp() {
        return copyfromIp;
    }

    /**
     * @param copyfromIp 根据 copyfromIp 设置 {@link #copyfromIp}的值
     */
    public void setCopyfromIp(String copyfromIp) {
        this.copyfromIp = copyfromIp;
    }

    /**
     * @return {@link #copyfromPort} 的值
     */
    public Integer getCopyfromPort() {
        return copyfromPort;
    }

    /**
     * @param copyfromPort 根据 copyfromPort 设置 {@link #copyfromPort}的值
     */
    public void setCopyfromPort(Integer copyfromPort) {
        this.copyfromPort = copyfromPort;
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

    @Override
    public String toString() {// TODO 所有涉及密码 输出得控制以及保存配置文件时得加密
        return JSON.toJSONString(this);
    }

    /**
     * @return {@link #copyfromId} 的值
     */
    public Integer getCopyfromId() {
        return copyfromId;
    }

    /**
     * @param copyfromId 根据 copyfromId 设置 {@link #copyfromId}的值
     */
    public void setCopyfromId(Integer copyfromId) {
        this.copyfromId = copyfromId;
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
     * 根据JSON字节数组 返回对象
     * 
     * @param jsonStr
     * @return
     */
    public static MySQLInfo loadMySQLInfoFromJSONBytes(byte[] jsonBytes) {
        return loadMySQLInfoFromJSONStr(new String(jsonBytes));
    }

    /**
     * 根据JSON字符串 返回对象
     * 
     * @param jsonStr
     * @return
     */
    public static MySQLInfo loadMySQLInfoFromJSONStr(String jsonStr) {
        return JSON.parseObject(jsonStr, MySQLInfo.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(MySQLInfo o) {
        return this.id != null && o.id != null ? this.id - o.id : 0;
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

}
