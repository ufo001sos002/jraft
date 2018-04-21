package net.data.technology.jraft;

import java.util.concurrent.CompletableFuture;

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
 * 、长联通道——HCS启动后与HCM通讯 获取配置的连接 长联(拿完配置作为心跳) 以及后续一些 集群节点私有数据 或 私有命令(重启等) 可直接下发 无需通过Leader(HCM_S_R) 以下简称(HCM_S_S)
 * 、集群配置由脚本初始化集群时写入配置文件(仅为 长联通道 被否定后 考虑)
 * 、整个Raft文件仅为 HCM中Raft客户端(以下简称HCM_S_R)通道下发配置的下发能到各节点接收，但应用 还是得由状态机 进行(所以最终完整 配置 状态机保留一份 作为快照 ) 
 * 并且 各节点应用结果可由各节点通过 HCM_S_S 告知 HCM
 * 、添加实例时 Leader分配好实例归属哪个节点管理后，后续实例操作均对应某节点服务器，除非节点故障重新计算 在 分配配置中
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
 * 
 * 
 * ★各集群节点完整配置包含(最终保存即一封完整的，leader连不上HCM时 即可读该份配置,进行操作)：
 * --公共配置(集群共享配置；包含:整个集群中节点信息、整个集群需监控的实例相关、实例集群分派规则 等等；由HCM 根据所处集群 下发，各集群节点一致) 
 * --私有配置(各集群节点私有启动相关配置；由HCM 根据集群节点分别下发，各集群节点不一致)
 * --分配配置(各节点被分配到 需要管理的实例，由Leader或HCM_S_R下发，各集群节点一致)
 * 以上构成完整配置，存在本地配置文件LOCALCONFIG中(也可以拆分多个文件，视情况调整)
 * 
 * 
 * ★整个流程：创建集群、添加实例、分配实例(后续实例停用收回类似)、添加集群节点、实例动态切换、移除集群节点、集群节点故障发生(follower、Leader)、
 * 某个集群节点重启、所有集群节点都重启
 * 
 * 创建集群：(状态此时各种配置都为空)
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
     * 中间件启动完毕 且初始化完Raft集群后 初始化此方法(类似 {@link RaftConsensus#run(RaftContext)}调用阶段) 参数
     * {@link RaftMessageSender} 用于状态机后续 发配置给集群(即分配日志 可通过该途径)
     * 
     * @see net.data.technology.jraft.StateMachine#start(net.data.technology.jraft.RaftMessageSender)
     */
    @Override
    public void start(RaftMessageSender raftMessageSender) {
	// TODO Auto-generated method stub

    }

    /**
     * 针对提交的 日志 进行应用(考虑)
     * 
     * @see net.data.technology.jraft.StateMachine#commit(long, byte[])
     */
    @Override
    public void commit(long logIndex, byte[] data) {
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
	// TODO Auto-generated method stub

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
	// TODO Auto-generated method stub

    }

    /**
     * 初始化启动
     */
    private void initStart() {
	// TODO 初始化启动
    }

    /**
     * 根据参数构造 类{@link Middleware} 对象
     */
    public Middleware() {
	super();
	// TODO Auto-generated constructor stub
    }

    /**
     * 启动方法
     * 
     * @param args
     */
    public static void main(String[] args) {

    }

}
