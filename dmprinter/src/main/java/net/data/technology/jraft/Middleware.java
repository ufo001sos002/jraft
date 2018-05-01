package net.data.technology.jraft;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.data.technology.jraft.extensions.FileBasedServerStateManager;
import net.data.technology.jraft.extensions.Log4jLoggerFactory;
import net.data.technology.jraft.extensions.RpcTcpClientFactory;
import net.data.technology.jraft.extensions.RpcTcpListener;
import net.data.technology.jraft.jsonobj.HCSClusterAllConfig;
import net.data.technology.jraft.jsonobj.HCSNode;
import net.data.technology.jraft.jsonobj.RDSInstance;
import net.data.technology.jraft.jsonobj.RdsAllocation;

/**
 * <pre>
 * ★需要做的
 * 1、配置同步 
 * 2、集群状态(leader发送rpc失败即表示 节点故障,可能需要达到参数配置的次数，不然可能切换频繁，因为会一直发心跳包 或 数据包，发成功了即表示可用)
 * 3、初始化数据源
 * 4、VIP 绑定 监听
 * 5、客户端连接、数据源数据直接转发
 * 6、节点之间实例服务切换(动态 或 故障)
 * 监管实例建立(数据源 数据转发)、监管实例迁移(主动或故障)、实例由集群启动初始化时下发、实例动态添加、分配、移出
 * 
 * 
 * ★解决思路，可考虑：
 * 、考虑从slave对Master也有心跳(以便检测slave是否离线)---可不需要 由Leader发心跳包(raft规则) 判断(如实在网络问题 发包检测都有异常时才考虑 双向心跳包)
 * 、Leader 与 HCM 中Raft客户端进行后续通讯
 * 、长联通道——HCS启动后与HCM通讯 获取配置的连接 长联(拿完配置作为心跳) 以及后续一些 集群节点私有数据 或 私有命令(重启等) 可直接下发 无需通过Leader(或HCM_S_R) 以下简称(HCM_S_S)
 * 、集群配置由脚本初始化集群时写入配置文件(仅为 长联通道 被否定后 考虑)
 * 、整个Raft文件仅为 HCM中Raft客户端(以下简称HCM_S_R)通道下发配置的下发能到各节点接收，但应用 还是得由状态机 进行(所以最终完整 配置 状态机保留一份 作为快照 ) 
 * 并且 各节点应用结果可由各节点通过 HCM_S_S 告知 HCM( 即 整个Raft 仅负责 节点配置同步到位、节点状态告知，其他均由状态机操作)
 * 、添加实例时 Leader分配好实例归属哪个节点管理后，后续实例操作(包括实例数据源状态变更等)均对应某节点服务器，除非节点故障重新计算 在 分配配置中 
 * 、远程 SSH 移除 VIP的方法 以免本机程序挂了(相关SSH参数得拿) 实例迁移时可考虑Leader 远程移出 或 新增实例的节点 进行
 * 、所有配置的JSON定义 需 均为各节点都可以接收，并状态机对应处理保存(各种添加实例等中间命令，Raft仅保证下发到各节点，各节点保存更新完整配置完毕后根据 命令执行对应操作并HCM_S_S返回结果 )
 * 、超过半数挂了，raft目前预提交成功，但不commit。可能需要考虑 预提交时 直接 反馈 失败 (包括判断配置 会应用相关失败)HCM报警
 * 、对于新添加的节点 或 故障重启节点  可考虑将 未分配的 实例(即库存状态) 分配过去，以免浪费资源
 * 、Leader需对 公共配置中 实例 未指定 处理节点的（即实例在 分配配置 中无节点指定的进行指定操作以及考虑 未分配 的实例 重新指定分配(添加实例时、节点变更、选举完毕 时等)
 * 、原Raft通讯的相关文件文件不做任何改动依然复用
 * 、可以通过HCM_S_S告知HCM 各节点 状态(包含 角色等)
 * 、先定义流程————>再定义JSON
 * 、提供给HCM Raft客服端包，并编写方法 提供接口 由HCM直接调用
 * 、任务结果taskId回包要么Leader 要么节点 以HCM_S_S 回复告知
 * 、TODO 需要考虑节点初次连接不上HCM，如本地配置有，走了本地配置时，后面连上HCM之后，是否拿最新配置?
 * （如果拿的话，就需要考虑比对配置是否一致 主要判断 公有配置、私有配置 是否一致，是否方便变更，分配配置 由Leader 计算下发） 
 * 集群配置 仅 初次启动时 通过 HCM_S_S 应用一次(判断文件是否存在)，后续通过HCM_S_R同步、私有配置每次启动均通过HCM_S_S 拿变更
 * TODO Demo实现后，集群设计时 应考虑 负载转发 如何进行? 如何 模拟LVS，即节点可以考虑开启部分模拟LVS转发 至另一个节点处理，内部逻辑 仅做链路层包转发，还是 建立节点内部TCP通讯，转发数据包 以及回包
 * 
 * 
 * ★各集群节点完整配置包含(最终保存即一封完整的，leader连不上HCM时 即可读该份配置,进行操作)：
 * --集群配置(整个集群节点信息；由HCM 根据所处集群 下发，仅集群节点第一次启动时 状态机判断是否有集群信息，没有则问HCM要，连上集群后由Leader的集群信息覆盖，后续均走本地完整配置快照 并由集群同步至最新 ，)
 * --公共配置(集群共享配置；包含:整个集群中节点信息(除集群配置中 之外的配置信息)、整个集群需监控的实例相关、实例集群分派规则 等等；由Leader(或HCM_S_R)下发，各集群节点一致) 
 * --私有配置(各集群节点私有启动相关配置；由HCM 根据集群节点分别下发，各集群节点不一致)
 * --分配配置(各节点被分配到 需要管理的实例，由Leader(或HCM_S_R)下发，各集群节点一致，但主要由Leader计算下发，尤其是程序启动(初次或重启)时 )
 * 以上构成完整配置，存在本地配置文件LOCALCONFIG中(也可以拆分多个文件，视情况调整)
 * 
 * 
 * ★整个流程：创建启动集群、添加实例、分配实例(后续实例停用收回类似)、添加集群节点、实例动态切换、移除集群节点、集群节点故障发生(follower、Leader)、
 * 某个集群节点重启、所有集群节点都重启
 * 
 * 创建启动集群：(状态此时各种配置都为空)
 * 、HCS启动后与HCM通讯(心跳通道,即HCM_S_S)，HCM根据hcsId得出所属集群hcsGroupId 并 下发 公共配置、私有配置 并保持长联(作为后续通讯备用)
 * 、集群根据 公共配置 中 集群信息 选出Leader 与 HCM_S_R 进行后续通讯
 * 、等待后续 HCM_S_R 的 公共配置至Leader (也可以包含私有配置,但需指定hcsId) ，通过Raft下发 保证下发至个节点 并由状态机 应用(应用结果可由HCM_S_S 返回告知)
 * 
 * 添加实例：(可以批量下发)
 * 、HCM_S_R下发公共配置至Leader，Leader同步至各节点直至commit
 * 、Leader状态机 应用 时 根据 公共配置——分配规则 计算实例所属节点，并新增一条 分配配置下发至各节点直至commit
 * 、commit之后，各节点状态机 根据 节点Id:[实例Id] 进行匹配管控 并初始化数据源 将结果通过HCM_S_S返回告知
 * 
 * 分配实例：（实例归属已确定）
 * 、HCM_S_R下发公共配置至Leader，Leader同步至各节点直至commit
 * 、commit之后，各节点状态机 根据实例Id 对管辖的 实例进行 分配操作(绑定VIP 监听VIP等) 并将结果通过HCM_S_S返回告知
 * 
 * 添加集群节点：
 * 、HCS启动后与HCM通讯(心跳通道,即HCM_S_S)，HCM根据hcsId得出所属集群hcsGroupId 并 下发 公共配置、私有配置 并保持长联(作为后续通讯备用)
 * 、HCS_S_R下发添加集群至Leader,Leader同步至各节点直至commit
 * 、同步配置(但分配配置中无节点实例) 
 * 、并等待后续Leader下发的分配配置 包含 该节点 实例信息 则进行实例相应操作  
 * 
 * 实例动态切换：(实例都正常)
 * 、HCM_S_R下发实例切换配置至Leader,Leader同步至各节点直至commit（或直接不同步）
 * 、Leader重新计算实例归属后，新增一条 分配配置(某节点移除实例管理，某节点增加实例管理) 同步至各节点直至commit
 * 、commit之后，各节点状态机按配置进行移除、增加实例的操作(如VIP已监听(即已分配)的实例，增加的节点一直ping直到删除节点vip移除绑定结束后才监听，
 * 但初始化数据源操作可以先行，删除节点先移出VIP再停止监听，移除数据源连接) 两个节点均将结果通过HCM_S_S返回告知
 * 或
 * 、HCM_S_R下发 分配配置 指定 实例 新分配节点 至 Leader,Leader同步至各节点直至commit
 * 、后续 commit之后，可以与上面一致(只是和上面一致 可以 考虑 Leader(或增加实例节点)可以直接移除VIP，这样可实例添加和移除可同时进行)
 * 
 * 移除集群节点：(节点 分正常与非正常)
 * 、HCM_S_R下发实例切换配置至Leader,Leader同步至各节点直至commit（或直接不同步）
 * 、commit之后，节点正常的，状态机移除VIP 停止监听 并移除实例，节点非正常的 不做任何操作
 * 、Leader重新计算实例归属后，新增一条 分配配置(某节点增加实例管理) 同步至各节点直至commit
 * 、commit之后，各节点状态机根据配置(需考虑实例状态) 增加对应实例管理（对于监控状态的则需 尝试ping后看是否需要移除远端VIP 再绑定，也可考虑Leader操作） 并将结果通过HCM_S_S返回告知
 * 
 * 集群节点故障发生：
 * 、如果为Leader挂了，则先选出Leader
 * 、Leader判断(连接断开 或 数据包接收异常) 节点故障后，重新计算实例归属后，新增一条 分配配置(某节点增加实例管理) 同步至各节点直至commit
 * 、commit之后，各节点状态机根据配置(需考虑实例状态) 增加对应实例管理（对于监控状态的则需 尝试ping后看是否需要移除远端VIP 再绑定，也可考虑Leader操作） 并将结果通过HCM_S_S返回告知
 * (考虑 ： Leader是通过HCM_S_S 还是HCM_S_R通道告知HCM 节点故障，故障节点HCM_S_S通道未发送心跳也代表故障,但担心仅此通道异常而已，还需Leader再发送一次确认)
 * 
 * 某个集群节点重启：
 * 、与添加集群节点一致，HCM挂了 则超时即 应用本地配置(就算本地配置中有 该节点的 分配配置  但状态机不应用 分配配置)
 * 私有配置在 HCM_S_S通道可用(尝试重连，和之前SSLSocket一致)之后，从新拿配置(对比 私有配置 部分不同的进行变更操作)
 * 
 * 所有集群节点都重启：
 * 与某个集群节点重启一致，但均 忽略 分配配置，在选举出Leader之后，判断 公共配置中 有 未指定节点管理的 实例，则按规则指定 分配
 * </pre>
 */
public class Middleware implements StateMachine {
    /**
     * 日志对象
     */
    private static final Logger logger = LoggerFactory.getLogger(Middleware.class);
    /**
     * 程序根路径 属性名
     */
    public static final String SYS_HOME = "HOTDB_HOME";
    /**
     * 配置文件 文件夹名
     */
    public static final String CONFIG_FOLDER_NAME = "conf";
    /**
     * 集群配置文件夹名( {@ #CONFIG_FOLDER_NAME} 文件夹下)
     */
    public static final String CLUSTER_FOLDER_NAME = "cluster";
    /**
     * 本地配置文件名(conf文件夹下)
     */
    public static final String LOCAL_CONFIG_FILE = "localConfig";
    /**
     * 系统默认字符集： UTF-8
     */
    public static volatile Charset SYSTEM_CHARSET = StandardCharsets.UTF_8;// Charset.forName("UTF-8");
    /**
     * 节点状态 离线
     */
    public static final int STATUS_ONLINE = 0;
    /**
     * 节点状态 离线
     */
    public static final int STATUS_OFFLINE = 1;
    /**
     * 当前节点状态
     */
    public volatile int status = STATUS_ONLINE;

    /**
     * 中间件单例对象
     */
    private static Middleware middleware = new Middleware();

    /**
     * HCS 唯一Id
     */
    private volatile String rdsServerId;

    /**
     * HCM IP
     */
    private volatile String controlIP;

    /**
     * HCM port
     */
    private volatile int controlPort;

    /**
     * SSL连接客户端对象 保持长连
     */
    private SSLClient sslClient = null;

    /**
     * 集群配置文件目录对象(由 {@link #SYS_HOME}{@link #CONFIG_FOLDER_NAME}
     * {@link #CLUSTER_FOLDER_NAME}) 组合
     */
    public static volatile Path clusterDirectoryPath = null;
    /**
     * 全局配置JSON对象(集群启动后 not null)
     */
    private HCSClusterAllConfig hcsClusterAllConfig = null;
    /**
     * true 启动成功
     */
    public volatile AtomicBoolean startupComplete = new AtomicBoolean();
    /**
     * 当前节点在集群所处的状态(默认 {@link ServerRole#Follower} )
     */
    private volatile ServerRole serverRole = ServerRole.Follower;
    /**
     * Raft 内部消息发送者
     */
    private RaftMessageSender raftMessageSender;

    /**
     * 当前集群集合
     */
    private List<ClusterServer> servers;
    /**
     * 当前集群分配索引
     */
    private int index = 0;


    /**
     * 根据参数构造 类{@link Middleware} 对象
     */
    private Middleware() {
    }

    /**
     * 获取中间件单例
     * 
     * @return {@link #middleware}
     */
    public static Middleware getMiddleware() {
	return middleware;
    }

    /**
     * @throws IOException
     */
    public void initRdsServerId() throws Exception {
	rdsServerId = System.getProperty("RDSSERVERID");
	if (rdsServerId == null || rdsServerId.trim().length() == 0) {
	    throw new Exception("rds server id is value of null , please start command add value of -DRDSSERVERID!");
	}
    }
    
    /**
     * @return socket连接的远控地址
     * @throws Exception
     */
    public void intHCMSocketRemoteAddress() throws Exception {
	controlIP = System.getProperty("CONTROL_ADDRESS");
	if (controlIP == null || controlIP.trim().length() == 0) {
	    throw new Exception(
		    "management host is value of null , please start command add value of -DCONTROL_ADDRESS!");
	}
    }

    /**
     * @return socket连接的远控端口
     * @throws IOException
     */
    public void intHCMSocketPort() throws Exception {
	String port = System.getProperty("CONTROL_PORT");
	if (port == null || port.trim().length() == 0) {
	    throw new Exception(
		    "management port is value of null , please start command add value of -DCONTROL_PORT!");
	}
	try {
	    controlPort = Integer.parseInt(port);
	} catch (NumberFormatException e) {
	    throw new Exception(
		    "management port is value of not number , please start command add value of -DCONTROL_PORT!");
	}
    }

    /**
     * @return {@link #rdsServerId} 的值
     */
    public String getRdsServerId() {
	return rdsServerId;
    }

    /**
     * @return socket私钥文件路径
     */
    private String getSocketKeyPath() {
	return "hotpu.jks";
    }

    /**
     * @return socket私钥文件密码
     */
    private String getSocketKeyStorePass() {
	return "61559355";
    }

    /**
     * @return socket私钥密码
     */
    private String getSocketKeyPass() {
	return "61559355";
    }

    /**
     * @return socket公钥数字证书文件路径
     */
    private String getSocketTrustPath() {
	return "hotpu-trust.jks";
    }

    /**
     * @return socket公钥数字证书文件密码
     */
    private String getSocketTrustStorePass() {
	return "61559355";
    }

    /**
     * SSL通讯线程池核心线程大小
     * 
     * @return
     */
    public int getExecutorSize() {
	return Runtime.getRuntime().availableProcessors() * 2;
    }

    /**
     * 根据字节返回字符串
     * 
     * @param bytes
     * @return
     */
    public static String getStringFromBytes(byte[] bytes) {
	return new String(bytes, Middleware.SYSTEM_CHARSET);
    }

    /**
     * 初始化
     * 
     * @throws Exception
     */
    public void init() throws Exception {
	initRdsServerId();
	intHCMSocketRemoteAddress();
	intHCMSocketPort();
	this.sslClient = new NIOSSLClient(getSocketKeyPath(), getSocketKeyStorePass(), getSocketKeyPass(),
		getSocketTrustPath(), getSocketTrustStorePass(), getExecutorSize());
    }

    /**
     * 
     * @return 程序根路径
     */
    public static String getHomePath() {
	String home = System.getProperty(SYS_HOME);
	if (home != null) {
	    if (home.endsWith(File.pathSeparator)) {
		home = home.substring(0, home.length() - 1);
		System.setProperty(SYS_HOME, home);
	    }
	}
	return home;
    }

    /**
     * 
     * @return 集群配置文件目录对象(not null)
     */
    public static Path getClusterDirectoryPath() {
	if(clusterDirectoryPath == null) {
	    synchronized (CLUSTER_FOLDER_NAME) {
		if(clusterDirectoryPath == null) {
		    clusterDirectoryPath = Paths.get(SYS_HOME).resolve(CONFIG_FOLDER_NAME).resolve(CLUSTER_FOLDER_NAME);
		}
	    }
	}
	return clusterDirectoryPath;
    }

    /**
     * 保存配置文件
     * 
     * @return true 保存成功
     */
    public boolean saveConfigToFile() {
	if (hcsClusterAllConfig != null) {
	    try {
		hcsClusterAllConfig.writeToFile(LOCAL_CONFIG_FILE);
		return true;
	    } catch (IOException e) {
		// TODO 后期配置保存失败 后 生成异常编号 返回M
		logger.error(Markers.CONFIG, "save config to file is error:" + e.getMessage(), e);
	    }
	}
	return false;
    }

    /**
     * 根据 {@link HCSClusterAllConfig#getHcsGroup()} 构造 {@link ClusterConfiguration}
     * 对象
     * 
     * @param hcsClusterAllConfig
     *            json对象
     * @return {@link ClusterConfiguration} ，但仅变更
     *         {@link ClusterConfiguration#servers}信息
     */
    public static ClusterConfiguration getClusterConfigurationFromHCSClusterAllConfig(
	    HCSClusterAllConfig hcsClusterAllConfig) {
	ClusterConfiguration config = new ClusterConfiguration();
	if (hcsClusterAllConfig.getHcsGroup() != null) {
	    List<ClusterServer> servers = config.getServers();
	    for (HCSNode hcsNode : hcsClusterAllConfig.getHcsGroup()) {
		servers.add(new ClusterServer(hcsNode.getHcsId(), hcsNode.getIp(), hcsNode.getPort()));
	    }
	}
	return config;
    }

    /**
     * 处理配置对象
     * 
     * @param hcsClusterAllConfig
     * @return not null
     */
    public Tuple2<Integer, String> handleServerConfig(HCSClusterAllConfig hcsClusterAllConfig) {
	synchronized (this) {
	    if (startupComplete.get()) {
		return new Tuple2<Integer, String>(MsgSign.SUCCESS_CODE, "ok");
	    }
	    // 初始化集群
	    ServerStateManager stateManager = new FileBasedServerStateManager(getClusterDirectoryPath());
	    ClusterConfiguration config = null;
	    if (stateManager.existsClusterConfiguration()) { // 本地存在配置
		config = stateManager.loadClusterConfiguration();
	    } else { // 初次启动本地无配置
		config = getClusterConfigurationFromHCSClusterAllConfig(hcsClusterAllConfig);
		stateManager.saveClusterConfiguration(config);
	    }
	    try {
		this.sslClient.setHeartbeatInterval(hcsClusterAllConfig.getSystemConfig().getHeartbeatToM());
		this.servers = config.getServers();
		if (this.servers.isEmpty()) {
		    return new Tuple2<Integer, String>(MsgSign.ERROR_CODE_121000, "cluster info is empty in JSON");
		}
		URI localEndpoint = new URI(config.getServer(stateManager.getServerId()).getEndpoint());
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
			Runtime.getRuntime().availableProcessors() * 2);
		RaftParameters raftParameters = new RaftParameters().withElectionTimeoutUpper(5000)
			.withElectionTimeoutLower(3000).withHeartbeatInterval(1500).withRpcFailureBackoff(500)
			.withMaximumAppendingSize(200).withLogSyncBatchSize(5).withLogSyncStoppingGap(5)
			.withSnapshotEnabled(10).withSyncSnapshotBlockSize(0);
		// 构建状态机 对象
		RaftContext context = new RaftContext(stateManager, this, raftParameters,
			new RpcTcpListener(localEndpoint.getPort(), executor), new Log4jLoggerFactory(),
			new RpcTcpClientFactory(executor), executor);
		this.hcsClusterAllConfig = hcsClusterAllConfig;
		saveConfigToFile();
		RaftConsensus.run(context);
		startupComplete.compareAndSet(false, true);
		return new Tuple2<Integer, String>(MsgSign.SUCCESS_CODE, "ok");
	    } catch (Exception e) {
		// TODO 错误信息具体定义
		logger.error("init cluster error:" + e.getMessage(), e);
	    }
	    return null;
	}
    }

    /**
     * 从HCM启动集群超时时间,超时后从本地文件启动(ms)
     */
    public static long startClusterTimeOut = 5 * 60 * 1000;

    /**
     * 启动
     */
    public void start() {
	boolean getHCMNewConfig = false;
	try {
	    sslClient.start(this.controlIP, this.controlPort);
	    SocketPacket.sendStartupInfo(sslClient);
	    getHCMNewConfig = true;
	} catch (IOException e) {
	    logger.error(Markers.SSLCLIENT, "socket connection to management is fail", e);
	}
	if (getHCMNewConfig) {
	    // 等待新配置
	    long time = System.currentTimeMillis();
	    while (!startupComplete.get() || (System.currentTimeMillis() - time <= startClusterTimeOut)) {
		Tools.sleep(100);
	    }
	}
	// 用本地配置 启动
	try {
	    Tuple2<Integer, String> tuple2 = null;
	    HCSClusterAllConfig hcsClusterAllConfig = HCSClusterAllConfig.loadFromFile(LOCAL_CONFIG_FILE);
	    if (hcsClusterAllConfig != null) {
		tuple2 = handleServerConfig(hcsClusterAllConfig);
	    }
	    if (tuple2 == null || MsgSign.SUCCESS_CODE != tuple2._1()) {
		logger.error(Markers.CONFIG, "load config from file is failed,error:" + tuple2);
	    }
	} catch (IOException e) {
	    logger.error(Markers.CONFIG, "load config from file is failed:" + e.getMessage(), e);
	}
    }

    /**
     * 中间件启动完毕 且初始化完Raft集群后 初始化此方法(类似 {@link RaftConsensus#run(RaftContext)}调用阶段) 参数
     * {@link RaftMessageSender} 用于状态机后续 发配置给集群(即分配配置 可通过该途径)
     * 
     * @see net.data.technology.jraft.StateMachine#start(net.data.technology.jraft.RaftMessageSender)
     */
    @Override
    public void start(RaftMessageSender raftMessageSender) {
	// TODO Auto-generated method stub
	// 疑似 Raft数据存储(包含压缩)仅存储传输的 与快照无关(即不从快照读取) 快照可能更多用来状态机自身启动时 获取应用至最新 并且留意 幂等性

    }

    /**
     * 获取处理实例的 集群节点ID
     * 
     * @return 节点Id not null
     */
    private String getHandleServerId() {
	synchronized (servers) {
	    if (index == servers.size()) {
		index = 0;
	    }
	    // TODO 后续需判断是否在线  以及 保证 在增加 节点  或 删除节点 后 均衡(目前删除 会 均分到其他节点 一直调本方法即可，增加节点时 需追上当前分配数据 再均分)
	    return servers.get(index++).getId();
	}
    }

    /**
     * 处理 {@link MsgSign#FLAG_RDS_ADD} 结果
     * 
     * @param socketPacket
     */
    private void handleRDSInstanceAdd(SocketPacket socketPacket) {
	if (socketPacket.data == null || socketPacket.data.length <= 0) {
	    return;
	}
	HCSClusterAllConfig t_hcsClusterAllConfig = null;
	t_hcsClusterAllConfig = HCSClusterAllConfig.loadObjectFromJSONString(getStringFromBytes(socketPacket.data));
	if (t_hcsClusterAllConfig == null) {
	    return;
	}
	List<RDSInstance> rdss = t_hcsClusterAllConfig.getRdsInstances();
	if (rdss == null || rdss.isEmpty()) {
	    // TODO 根据任务号 回应
	    return;
	}
	// 先追加 实例至 JSON对象并保存
	this.hcsClusterAllConfig.getRdsInstances().addAll(rdss);
	this.saveConfigToFile();
	String handleId = null;
	ArrayList<String> adds = null;
	HashMap<String, ArrayList<String>> hcsAddsMap = new HashMap<String, ArrayList<String>>();
	for (RDSInstance rds : rdss) {
	    handleId = getHandleServerId();
	    adds = hcsAddsMap.get(handleId);
	    if (adds == null) {
		adds = new ArrayList<String>();
		hcsAddsMap.put(handleId, adds);
	    }
	    adds.add(rds.getRdsId());
	}
	t_hcsClusterAllConfig.setRdsInstances(null);
	ArrayList<RdsAllocation> rdsAllocations = new ArrayList<RdsAllocation>();
	for (Entry<String, ArrayList<String>> set : hcsAddsMap.entrySet()) {
	    rdsAllocations.add(new RdsAllocation(set.getKey(), set.getValue()));
	}
	t_hcsClusterAllConfig.setRdsAllocations(rdsAllocations);

	// TODO 发送分配
    }

    /**
     * 针对提交的 日志 进行应用(考虑)
     * 
     * @see net.data.technology.jraft.StateMachine#commit(long, byte[])
     */
    @Override
    public void commit(long logIndex, byte[] data) {
	SocketPacket socketPacket = new SocketPacket();
	socketPacket.read(data);
	if (logger.isDebugEnabled()) {
	    logger.debug(Markers.STATEMACHINE, "executor.execute socketPacket:" + socketPacket.getDebugValue());
	}
	if (MsgSign.TYPE_RDS_SERVER == socketPacket.type) {
	    switch (socketPacket.flags) {
	    case MsgSign.FLAG_RDS_ADD:
		handleRDSInstanceAdd(socketPacket);
		break;
	    }
	}
	// TODO Auto-generated method stub

    }

    /**
     * 对预提交的日志 进行 回滚(针对预提交中未commit的值，覆盖或清空)
     * 
     * @see net.data.technology.jraft.StateMachine#rollback(long, byte[])
     */
    @Override
    public void rollback(long logIndex, byte[] data) {
	// TODO Auto-generated method stub

    }

    /**
     * 预提交 发送 的日志 等待commit之后 正式提交应用(考虑队列存放 再根据commit值逐步应用)
     * 
     * @see net.data.technology.jraft.StateMachine#preCommit(long, byte[])
     */
    @Override
    public void preCommit(long logIndex, byte[] data) {
	// TODO Auto-generated method stub

    }

    /**
     * 保存快照数据(From Leader)
     * 
     * @see net.data.technology.jraft.StateMachine#saveSnapshotData(net.data.technology.jraft.Snapshot,
     *      long, byte[])
     */
    @Override
    public void saveSnapshotData(Snapshot snapshot, long offset, byte[] data) {
	// TODO Auto-generated method stub

    }

    /**
     * 应用快照数据(From Leader)
     * 
     * @see net.data.technology.jraft.StateMachine#applySnapshot(net.data.technology.jraft.Snapshot)
     */
    @Override
    public boolean applySnapshot(Snapshot snapshot) {
	// TODO Auto-generated method stub
	return false;
    }

    /**
     * 读取快照数据 用于 Send 刚起的 Follower
     * 
     * @see net.data.technology.jraft.StateMachine#readSnapshotData(net.data.technology.jraft.Snapshot,
     *      long, byte[])
     */
    @Override
    public int readSnapshotData(Snapshot snapshot, long offset, byte[] buffer) {
	// TODO Auto-generated method stub
	return 0;
    }

    /**
     * 获取最后一次保存的快照对象
     * 
     * @see net.data.technology.jraft.StateMachine#getLastSnapshot()
     */
    @Override
    public Snapshot getLastSnapshot() {
	// TODO Auto-generated method stub
	return null;
    }

    /**
     * 定期将收到的配置写入 文件<br>
     * <b>注：可能每次收到的配置应用成功后 才写入快照中，未应用成功的将不写入，但是记录最新commit了的值</b>
     */
    @Override
    public CompletableFuture<Boolean> createSnapshot(Snapshot snapshot) {
	// TODO Auto-generated method stub
	return null;
    }

    /**
     * 中间件故障 不提供服务 发送完对应通知之后 JVM退出
     * 
     * @param code
     * @see net.data.technology.jraft.StateMachine#exit(int)
     */
    @Override
    public void exit(int code) {
	// TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.data.technology.jraft.StateMachine#notifyServerRole(net.data.technology.
     * jraft.ServerRole)
     */
    @Override
    public void notifyServerRole(ServerRole serverRole) {
	this.serverRole = serverRole;
	// TODO 角色变化已埋点 只剩制定协议 HCM_S_S 发送HCM

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * net.data.technology.jraft.StateMachine#notifyServerStatus(java.lang.String,
     * int)
     */
    @Override
    public void notifyServerStatus(String hcsId, int status) {
	if (this.rdsServerId.equals(hcsId)) {
	    this.status = status;
	}
	// TODO 通知HCM 状态变更

    }

    /**
     * 启动方法
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
	Middleware middleware = getMiddleware();
	middleware.init();
	middleware.start();
    }

}
