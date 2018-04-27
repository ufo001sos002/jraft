package net.data.technology.jraft;


public class MsgSign {
    /**
     * type值区分业务（目前已使用1-11、127、128-143，固后续开发取值范围为Server to 从60至126，Client to 从180至255） <br>
     * 心跳包类型 [= {@value}]
     */
    public static final byte TYPE_PING = (byte) 127; // 心跳包类型
    /**
     * management跟rds server的沟通 [= {@value}]
     */
    public static final byte TYPE_RDS_SERVER = (byte) 181; // management跟rds server的沟通

    /**
     * TYPE=181，Flag值定义<br>
     * RDS-SERVER启动成功消息 [= {@value}]
     */
    public static final int FLAG_RDS_SERVER_START = 1; // RDS-SERVER启动成功消息
    /**
     * RDS-SERVER编组配置消息，回复RDS-SERVER编组启动成功 [= {@value}]
     */
    public static final int FLAG_RDS_SERVER_CONFIG = 2; // RDS-SERVER编组配置消息，回复RDS-SERVER编组启动成功
    /**
     * RDS-SERVER主从自动切换 [= {@value}]
     */
    public static final int FLAG_RDS_SERVER_AUTO_SWITCH = 3; // RDS-SERVER主从自动切换
    /**
     * RDS-SERVER主从手动切换 [= {@value}]
     */
    public static final int FLAG_RDS_SERVER_MANUAL_SWITCH = 4; // RDS-SERVER主从手动切换
    /**
     * RDS-SERVER停用 [= {@value}]
     */
    public static final int FLAG_RDS_SERVER_DISABLE = 5; // RDS-SERVER停用
    /**
     * RDS-SERVER启用 [= {@value}]
     */
    public static final int FLAG_RDS_SERVER_ENABLE = 6; // RDS-SERVER启用
    /**
     * RDS-SERVER移除 [= {@value}]
     */
    public static final int FLAG_RDS_SERVER_REMOVE = 7; // RDS-SERVER移除
    /**
     * RDS-SERVER配置变更，基础参数变更 [= {@value}]
     */
    public static final int FLAG_RDS_SERVER_CONFIG_CHANGE = 8; // RDS-SERVER配置变更，基础参数变更
    /**
     * RDS-SERVER手动恢复 [= {@value}]
     */
    public static final int FLAG_RDS_SERVER_RECOVERY = 9; // RDS-SERVER手动恢复
    /**
     * RDS实例添加（绑定RDS-SERVER） [= {@value}]
     */
    public static final int FLAG_RDS_ADD = 21; // RDS实例添加（绑定RDS-SERVER）
    /**
     * RDS实例分配 [= {@value}]
     */
    public static final int FLAG_RDS_ALLOCATE = 22; // RDS实例分配
    /**
     * RDS实例手动启用 [= {@value}]
     */
    public static final int FLAG_RDS_ENABLE = 23; // RDS实例手动启用
    /**
     * RDS实例手动停用 [= {@value}]
     */
    public static final int FLAG_RDS_DISABLE = 24; // RDS实例手动停用
    /**
     * RDS实例续期 [= {@value}]
     */
    public static final int FLAG_RDS_RENEWAL = 25; // RDS实例续期
    /**
     * RDS实例到期停用 [= {@value}]
     */
    public static final int FLAG_RDS_EXPIRE = 26; // RDS实例到期停用
    /**
     * RDS实例回收 [= {@value}]
     */
    public static final int FLAG_RDS_TAKE_BACK = 27; // RDS实例回收
    /**
     * RDS实例移除 [= {@value}]
     */
    public static final int FLAG_RDS_REMOVE = 28; // RDS实例移除
    /**
     * RDS连接用户帐号变更 [= {@value}]
     */
    public static final int FLAG_RDS_CONNECT_USER_CHANGE = 29; // RDS连接用户帐号变更
    /**
     * RDS IP白名单变更 [= {@value}]
     */
    public static final int FLAG_RDS_IP_WHITE_LIST_CHANGE = 30; // RDS IP白名单变更
    /**
     * RDS SQL拦截 [= {@value}]
     */
    public static final int FLAG_RDS_SQL_INTERCEPT = 31; // RDS SQL拦截
    /**
     * RDS数据库删除 [= {@value}]
     */
    public static final int FLAG_RDS_DB_DROP = 32; // RDS数据库删除
    /**
     * RDS实例编辑（绑定RDS-SERVER） [= {@value}]
     */
    public static final int FLAG_RDS_UPDATE = 33; // RDS实例编辑（绑定RDS-SERVER）
    /**
     * RDS数据库增加 [= {@value}]
     */
    public static final int FLAG_RDS_DB_ADD = 34; // RDS数据库增加
    /**
     * RDS数据库编辑 [= {@value}]
     */
    public static final int FLAG_RDS_DB_EDIT = 35; // RDS数据库增加
    /**
     * RDS读写分离设置 [= {@value}]
     */
    public static final int FLAG_RDS_STRATEGYFORRWSPLIT = 36;
    /**
     * RDS MySQL高可用状态 [= {@value}]
     */
    public static final int FLAG_RDS_MYSQL_HA_STATUS = 37;
    /**
     * RDS MySQL异常切换 [= {@value}]
     */
    public static final int FLAG_RDS_MYSQL_AUTO_SWITCH = 38;
    /**
     * RDS 操作数 [= {@value}]
     */
    public static final int FLAG_RDS_OP_NUM = 39;
    /**
     * RDS实例回收成功 [= {@value}]
     */
    public static final int FLAG_RDS_TAKE_BACK_SUCCESS = 40; // RDS实例回收

    /**
     * sql审计配置
     */
    public static final int FLAG_RDS_SQL_AUDIT = 41;

    /**
     * RDS实例所有后端连接新建 <br/>
     * 参照：<a href='http://wiki.hotpu.cn:8090/pages/viewpage.action?pageId=15696390' >2.42.1 M to
     * RS_RDS实例所有后端连接新建</a>
     */
    public static final int FLAG_RDS_REBUILD_BACKCONNECTION = 42;

    /**
     * 2.43.1 M to RS_重置RDS数据库与连接用户<br/>
     * 参照：<a href='http://wiki.hotpu.cn:8090/pages/viewpage.action?pageId=15697562' >2.43.1 M to
     * RS_重置RDS数据库与连接用户</a>
     */
    public static final int FLAG_RDS_RESET_DB_USER = 43;

    /**
     * 2.44.1 M to RS_RDS实例升级 http://wiki.hotpu.cn:8090/pages/viewpage.action?pageId=15700056
     */
    public static final int FLAG_RDS_MYSQL_ADD = 44;


    /**
     * 默认版本 [= {@value}]
     */
    public static final byte DEFAULT_VERSION = 1;
    /**
     * {"token":"rds_server_id"}
     */
    private static volatile byte[] SEND_DATA_SERVER_ID_BYTES = null;


    /**
     * 
     * @return {@link #SEND_DATA_SERVER_ID_BYTES}
     */
    public static byte[] getSendDataFromSererIdBytes() {
        if (SEND_DATA_SERVER_ID_BYTES == null) {
            SEND_DATA_SERVER_ID_BYTES =
		    new TaskResponse().toString().getBytes(Middleware.SYSTEM_CHARSET);
        }
        return SEND_DATA_SERVER_ID_BYTES;
    }

    /**
     * 
     * @return 当前协议版本 目前返回{@link #DEFAULT_VERSION}值
     */
    public static byte getCurrentVersion() {
        return DEFAULT_VERSION;
    }

    
    


    /**
     * 成功消息返回码
     */
    public static final int SUCCESS_CODE = 0;
    /**
     * 错误消息返回码 多个错误
     */
    public static final int MORE_ERROR_CODE = -1;
    
    /**
     * 以下错误编码格式为
     * <a href='http://192.168.200.22:8090/pages/viewpage.action?pageId=13533384'>与Management交互协议
     * </a><br>
     * 下对应协议 A.B.C xxxx 的错误编码定义，例：<br>
     * "1.2.1 M to RS_RS 编组信息" 协议的 "1号" "错误编码" 对应为: ERROR_CODE_121001 = 121001 <br>
     * 即后面3位(001)为错误编号 前面N位(121)对应 协议编号 1.2.1
     */
    public static final int ERROR_CODE_ABC001 = 0;
    /**
     * HCS 配置为空
     */
    public static final int ERROR_CODE_100001 = 100001;
    /**
     * HCS 已停用
     */
    public static final int ERROR_CODE_100002 = 100002;
    /**
     * HCS编组信息下发 (捕获异常)
     */
    public static final int ERROR_CODE_121000 = 121000;
    /**
     * 编组信息内容错误
     */
    public static final int ERROR_CODE_121001 = 121001;
    /**
     * 管理端口监听失败
     */
    public static final int ERROR_CODE_121002 = 121002;
    /**
     * 配置文件保存失败
     */
    public static final int ERROR_CODE_121003 = 121003;
    /**
     * RDS下数据库源都发生故障
     */
    public static final int ERROR_CODE_121004 = 121004;
    /**
     * RDS实例监听失败
     */
    public static final int ERROR_CODE_121005 = 121005;
    /**
     * HCS组停用 操作失败(捕获异常)
     */
    public static final int ERROR_CODE_151000 = 151000;
    /**
     * HCS组停用 操作失败(HCS非 就绪 且 非停用 状态)
     */
    public static final int ERROR_CODE_151001 = 151001;
    /**
     * HCS组启用 操作失败(捕获异常)
     */
    public static final int ERROR_CODE_161000 = 161000;
    /**
     * HCS组启用 操作失败(HCS非停用且非就绪状态)
     */
    public static final int ERROR_CODE_161001 = 161001;
    /**
     * HCS组配置变更失败(捕获异常)
     */
    public static final int ERROR_CODE_181000 = 181000;
    /**
     * HCS组手动恢复 失败(捕获异常)
     */
    public static final int ERROR_CODE_191000 = 191000;
    /**
     * RDS实例初始化添加 失败(捕获异常)
     */
    public static final int ERROR_CODE_2211000 = 2211000;
    /**
     * RDS实例分配 失败(捕获异常)
     */
    public static final int ERROR_CODE_2221000 = 2221000;
    /**
     * RDS实例分配 信息内容错误(json信息有误)
     */
    public static final int ERROR_CODE_2221001 = 2221001;
    /**
     * RDS实例分配 RDS实例不为 库存 或 分配状态
     */
    public static final int ERROR_CODE_2221002 = 2221002;
    /**
     * RDS实例监听失败（中间件正常，message 为 对应RDSCode ，需将对应 RDS 置为库存状态 且 提示为“分配失败，监听失败 或 分配失败，端口被占用”）
     */
    public static final int ERROR_CODE_2221003 = 2221003;
    /**
     * RDS实例手动启用 失败(捕获异常)
     */
    public static final int ERROR_CODE_2231000 = 2231000;
    /**
     * RDS实例手动停用 失败(捕获异常)
     */
    public static final int ERROR_CODE_2241000 = 2241000;
    /**
     * RDS实例续期 失败(捕获异常)
     */
    public static final int ERROR_CODE_2251000 = 2251000;
    /**
     * RDS实例到期停用 失败(捕获异常)
     */
    public static final int ERROR_CODE_2261000 = 2261000;
    /**
     * RDS实例回收 失败(捕获异常)
     */
    public static final int ERROR_CODE_2271000 = 2271000;
    /**
     * RDS实例回收成功通知 失败(捕获异常)
     */
    public static final int ERROR_CODE_2272000 = 2272000;
    /**
     * RDS实例移除 失败(捕获异常)
     */
    public static final int ERROR_CODE_2281000 = 2281000;
    /**
     * RDS实例连接用户帐号变更 失败(捕获异常)
     */
    public static final int ERROR_CODE_2291000 = 2291000;
    /**
     * RDS实例IP白名单变更 失败(捕获异常)
     */
    public static final int ERROR_CODE_2301000 = 2301000;
    /**
     * RDS实例SQL拦截变更 失败(捕获异常)
     */
    public static final int ERROR_CODE_2311000 = 2311000;
    /**
     * RDS实例数据库删除 失败(捕获异常)
     */
    public static final int ERROR_CODE_2321000 = 2321000;
    /**
     * RDS实例编辑 失败(捕获异常)
     */
    public static final int ERROR_CODE_2331000 = 2331000;
    /**
     * RDS实例编辑 配置 保存至文件失败
     */
    public static final int ERROR_CODE_2331001 = 2331001;
    /**
     * VIP编辑不为库存状态
     */
    public static final int ERROR_CODE_2331002 = 2331002;
    /**
     * RDS实例数据库添加 失败(捕获异常)
     */
    public static final int ERROR_CODE_2341000 = 2341000;
    /**
     * RDS实例数据库编辑失败(捕获异常)
     */
    public static final int ERROR_CODE_2351000 = 2351000;
    /**
     * RDS实例读写分离设置 失败(捕获异常)
     */
    public static final int ERROR_CODE_2361000 = 2361000;
    /**
     * RDS实例MySQL高可用状态获取 失败(捕获异常)
     */
    public static final int ERROR_CODE_2371000 = 2371000;
    /**
     * RDS实例下操作数统计获取 失败(捕获异常)
     */
    public static final int ERROR_CODE_2391000 = 2391000;
    /**
     * HCS sql审计信息下发 (捕获异常)
     */
    public static final int ERROR_CODE_2411000 = 2411000;
    /**
     * RDS实例所有后端连接新建 失败(捕获异常)
     */
    public static final int ERROR_CODE_2421000 = 2421000;
    /**
     * RDS实例升级配置失败捕获异常
     */
    public static final int ERROR_CODE_2441000 = 2441000;
}
