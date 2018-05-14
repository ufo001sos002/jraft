// http://www.sojson.com/simple_json.html
//---------------------------DEMO 完整集群编组信息-------------------------------------------------
{
	"taskId" : "uuid" , // 任务唯一Id, RS响应必须带该字段值
	// --------私有配置 部分(后期细化)-----------------------
	"systemConfig": {
            "heartbeatToM": 2000,
            "usingAIO": 1
	}
	,
	//--------公有配置 部分---------
	// ------ hcsId、ip、port 为集群配置部分
	"hcsGroup":[ // 集群节点信息
	   {
		   "hcsId": "1", // hcs节点 Id
		   "ip": "192.168.200.215", // 集群 hcs节点 IP
		   "port":9001, // 集群hcs 节点 Raft通讯端口
		   "status":0, // 服务器状态: 0为在线可用(默认), 1为离线
			"sshPort":22,// SSH访问端口
		   "isUsedPrvkey":false,//是否使用私钥 true 使用
		   "userName":'root',// 服务器 用户名 远程ssh登录用的信息
		   "prvkeyFileContent":"AwABAgM=" ,// 对应byte[] 数组对象 ，认证的私钥文件内容(如果为null 则默认使用本地id_rsa私钥文件内容)
		   "password":"123456"// 登录用户密码或私钥密码(密文密码需使用id转换)   
 	   },
	   {
		   "hcsId": "2",
		   "ip": "192.168.200.215",
		   "port":9002,
		   "status":0,
			"sshPort":22,
		   "isUsedPrvkey":false,
		   "userName":'root',
		   "prvkeyFileContent":"AwABAgM=" ,
		   "password":"123456"
	   },
	   {
		   "hcsId": "3", 
		   "ip": "192.168.200.215", 
		   "port":9003,
		   "status":0,
			"sshPort":22,
		   "isUsedPrvkey":false,
		   "userName":'root',
		   "prvkeyFileContent":"AwABAgM=" ,
		   "password":"123456"
	   }
	]
	,
	"rdsInstances": [ //  集群中所需要处理的 实例集合 与原先一致-----
		{
			"rdsId": "HotDB_RDS_MM01_1",
			"vip": "192.168.200.214",
			"port": "3323",
			"modeltype": 2,
			"maxConnections": 0,
			"maxUserConnections": 0,
			"startAt": "2017-06-30 00:00:01",
			"endAt": "2018-11-30 23:59:59",
			"strategyForRWSplit": 2,
			"weightForSlaveRWSplit": 100,
			"status": 1,
			"mysqlInfos": [
				{
					"id": 1,
					"version": "5.6.1",
					"ip": "127.0.0.1",
					"port": 3306,
					"manageUser": "root",
					"managePassword": "64DF12CEBA7ECF40B630614DD6A68CD9",
					"masterSlaveType": 1
				},
				{
					"id": 2,
					"version": "5.6.1",
					"ip": "127.0.0.1",
					"port": 3307,
					"manageUser": "root",
					"managePassword": "ADB29AB47052A5D0F4D1548AA8CBDCB9",
					"masterSlaveType": 2
				}
			],
			"userInfos": [
				{
					"id": 1,
					"userName": "test",
					"password": "4761AC79ED0A03FEE7AC24B943BE44E9",
					"host": "127.0.0.1",
					"databases": [
						"test",
						"function_test_1",
						"function_test_2",
						"lzytest2",
						"autotest_3323",
						"utf8mb4testdb"
					]
				},
				{
					"id": 805,
					"userName": "internal",
					"password": "A9FB8054E867924D3F3AE2C3609247CAA323D9B940F0CEA36572A00F3CD9F6F5",
					"host": "127.0.0.1",
					"databases": [
						"test",
						"function_test_1",
						"function_test_2",
						"lzytest2",
						"autotest_3323",
						"utf8mb4testdb"
					]
				}
			]
		}
	]
	,
	"allocationRule":{// 实例分配规则
		"rule": 0 // 0为均分(默认) 所有节点轮询分配
	}
	,
	// --------分配配置 部分-----------------------
	"rdsAllocations":[ // 实例分配
		{
			"hcsId":"1", // hcs节点id
			"rdsIds":["HotDB_RDS_MM01_1"],//当前已分配的实例
			"adds":["HotDB_RDS_MM01_1"], // 增加实例id (实例动态切换部分 使用)
			"deletes":["HotDB_RDS_MM01_1"] // 增加实例id (实例动态切换部分使用)
		}
	]
}

//---------------------------集群信息变更相关-------------------------------------------------
// -------------实例归属分配部分 101-------------------
{
	"rdsAllocations":[ // 实例分配
		{
			"hcsId":"1", // hcs节点id
			"add":["HotDB_RDS_MM01_1"], // 增加实例id
		}
	]
}

// -------------实例动态切换部分 102-------------------
{
	"rdsAllocations":[ // 实例分配
		{
			"hcsId":"1", // hcs节点id
			"delete":["HotDB_RDS_MM01_1"] // 增加实例id
		}
		,
		{
			"hcsId":"2", // hcs节点id
			"delete":["HotDB_RDS_MM01_1"] // 增加实例id
		}
	]
}


//-----------------添加实例、分配实例 、实例收回、实例移出 、成功回包  JSON协议与之前一致 只是最终由对应管理实例的节点进行生效处理---------------------



if (logger.isDebugEnabled())
		logger.debug(String.format(

if (logger.isInfoEnabled())
			logger.info(String.format(

logger.error(String.format(

logger.warn(String.format(




//--------------------------------------------------------DEMO 测试用 JSON压缩版-------------------------------------------------------
下发配置  2   {"taskId":"uuid","systemConfig":{"heartbeatToM":2000,"usingAIO":1},"hcsGroup":[{"hcsId":"1","ip":"192.168.200.215","port":9001,"status":0,"sshPort":22,"isUsedPrvkey":false,"userName":"root","prvkeyFileContent":"AwABAgM=","password":"123456"},{"hcsId":"2","ip":"192.168.200.215","port":9002,"sshPort":22,"status":0,"isUsedPrvkey":false,"userName":"root","prvkeyFileContent":"AwABAgM=","password":"123456"},{"hcsId":"3","ip":"192.168.200.215","port":9003,"status":0,"sshPort":22,"isUsedPrvkey":false,"userName":"root","prvkeyFileContent":"AwABAgM=","password":"123456"}],"allocationRule":{"rule":0}}

增加实例  21  {"taskId":"taskId21","rdsInstances":[{"rdsId":"HotDB_RDS_MM01_1","vip":"192.168.200.214","port":"3323","modeltype":2,"maxConnections":0,"maxUserConnections":0,"startAt":"2017-06-30 00:00:01","endAt":"2018-11-30 23:59:59","strategyForRWSplit":2,"weightForSlaveRWSplit":100,"status":1,"mysqlInfos":[{"id":1,"version":"5.6.1","ip":"127.0.0.1","port":3306,"manageUser":"root","managePassword":"64DF12CEBA7ECF40B630614DD6A68CD9","masterSlaveType":1},{"id":2,"version":"5.6.1","ip":"127.0.0.1","port":3307,"manageUser":"root","managePassword":"ADB29AB47052A5D0F4D1548AA8CBDCB9","masterSlaveType":2}],"userInfos":[{"id":1,"userName":"test","password":"4761AC79ED0A03FEE7AC24B943BE44E9","host":"127.0.0.1","databases":["test","function_test_1","function_test_2","lzytest2","autotest_3323","utf8mb4testdb"]},{"id":805,"userName":"internal","password":"A9FB8054E867924D3F3AE2C3609247CAA323D9B940F0CEA36572A00F3CD9F6F5","host":"127.0.0.1","databases":["test","function_test_1","function_test_2","lzytest2","autotest_3323","utf8mb4testdb"]}]}]}

分配实例 22   {"taskId":"taskId22","rdsInstances":[{"rdsId":"HotDB_RDS_MM01_1","vip":"192.168.200.214","port":"3323","startAt":"2017-06-30 00:00:01","endAt":"2018-12-30 23:59:59"}]}








// ----------------------------------------------------HotDB Cloud Server Cluster 通讯协议增加或修改的------------------------------------------------------
先看老的 废弃 或 需要修改的  然后 再考虑需要 新增的 新的flags以及错误code
// -----------------完整 未带注释版(用于修改 生成 简短JSON)---------------------
{
    "taskId": "uuid",
    "systemConfig": {
        "managerPort": 3323,
        "managerUser": "root",
        "managerPassword": "123456",
        "heartbeatToM": 3000,
        "enableHeartbeat": true,
        "heartbeatPeriod": 3000,
        "heartbeatNextWaitTimes": 3000,
        "waitForSlaveInFailover": true,
        
        "usingAIO": 1,
        "processors": 16,
        "processorExecutor": 4,
        "timerExecutor": 6,
        "parkPeriod": 100000
    },
    "hcsGroup": [
        {
            "hcsId": "1",
            "ip": "192.168.200.215",
            "port": 9001,
            "status": 0,
            "sshPort": 22,
            "isUsedPrvkey": false,
            "userName": "root",
            "prvkeyFileContent": "AwABAgM=",
            "password": "123456"
        },
        {
            "hcsId": "2",
            "ip": "192.168.200.215",
            "port": 9002,
            "status": 0,
            "sshPort": 22,
            "isUsedPrvkey": false,
            "userName": "root",
            "prvkeyFileContent": "AwABAgM=",
            "password": "123456"
        },
        {
            "hcsId": "3",
            "ip": "192.168.200.215",
            "port": 9003,
            "status": 0,
            "sshPort": 22,
            "isUsedPrvkey": false,
            "userName": "root",
            "prvkeyFileContent": "AwABAgM=",
            "password": "123456"
        }
    ],
    "rdsInstances": [
        {
            "rdsId": "HotDB_RDS_MM01_1",
            "vip": "127.0.0.1",
            "port": 3306,
            "modeltype": 1,
            "maxConnections": 5000,
            "maxUserConnections": 0,
            "startAt": "yyyy-MM-dd HH:mm:ss",
            "endAt": "yyyy-MM-dd HH:mm:ss",
            "strategyForRWSplit": 0,
            "readerWeight": "40,30,20,0,10,10",
            "weightForSlaveRWSplit": 50,
            "writeStatus": 0,
            "status": 0,
            "mysqlInfos": [
                {
                    "id": 1,
                    "version": "5.7",
                    "ip": "127.0.0.1",
                    "port": 3306,
                    "manageUser": "root",
                    "managePassword": "123456",
                    "masterSlaveType": 1,
                    "copyfromId": 1,
                    "copyfromIp": "127.0.0.1",
                    "copyfromPort": 3306
                }
            ],
            "dbInfos": [
                {
                    "dbId": 1,
                    "databaseName": "test",
                    "characterset": "utf8"
                }
            ],
            "userInfos": [
                {
                    "id": 1,
                    "userName": "test",
                    "password": "test",
                    "host": "127.0.0.1",
                    "databases": [
                        "",
                        ""
                    ]
                }
            ],
            "ipWhites": {
                "isEnable": false,
                "ips": [
                    "127.0.0.1",
                    "%"
                ]
            },
            "wallConfig": [
                "delete_no_where_disallow",
                "update_no_where_disallow"
            ],
            "sqlAudit": {
                "rule": [
                    
                ],
                "isEnable": true
            }
        }
    ],
    "allocationRule": {
        "rule": 0
    },
    "rdsAllocations": [
        {
            "hcsId": "1",
            "rdsIds": [
                "HotDB_RDS_MM01_1"
            ],
            "adds": [
                "HotDB_RDS_MM01_1"
            ],
            "deletes": [
                "HotDB_RDS_MM01_1"
            ]
        }
    ],
    "namesrvAddr": "127.0.0.1:9786",
    "topic": "topicName",
    "tags": "tagsName",
    
}

/**
以下：
 HCM_S_S_HCS 表示 HCM 利用 SSL协议    的Socket连接发送消息 至  HCS  
 HCM_R_S_HCS 表示 HCM 利用 Raft协议 的Socket连接发送消息至   HCS
 HCS_S_S_HCM 表示 HCS 利用 SSL协议    的Socket连接发送消息 至  HCM  
 HCS_R_S_HCS 表示 HCS 利用 Raft协议 的Socket连接发送消息至    HCS
*/


//-------------------------------------1.2.1 HCM_S_S_HCS 配置信息(初次启动时集群配置) flags：2---修改----------------------------------
/** data：**/
{
    "taskId": "uuid", // 任务唯一Id, 由HCM下发时,HCS响应必须带该字段值(HCS集群主动发起时则不需要)
    "systemConfig": { // HCS集群各节点私有配置(可根据不同服务进行配置下发，各节点独自应用) 目前项不为最终项
        "managerPort": 3323, // 管理端port
        "managerUser": "root",// 管理端登录用户
        "managerPassword": "123456", // 管理端登录密码
        "heartbeatToM": 3000, // 发送心跳至HCM(ms)
        "enableHeartbeat": true, //是否启用心跳，是：true，否：false
        "heartbeatPeriod": 3000, // 心跳检测周期(ms)
        "heartbeatNextWaitTimes": 3000, // 下次心跳检测等待时间(ms)
        "waitForSlaveInFailover": true, // 故障切换中是否等待从机追上复// 制，是：true，否：false
        
        "usingAIO": 1, //是否使用AIO，是：1，否：0
        "processors": 16, //处理器数  所有前后连接对象绑定其中一个 （后续可能得考虑一个RDS实例 使用一套processors，互不影响）
        "processorExecutor": 4, //各处理器线程数  定时心跳等操作kill query、前端连接数据接收至下发MySQL之前处理、hold住等需要
        "timerExecutor": 6, //定时器线程数
        "parkPeriod": 100000 // 消息系统空闲时线程休眠周期(ns)
    },
    "hcsGroup": [ // HCS 集群节点信息
        {
            "hcsId": "1", // HCS id(唯一) 
            "ip": "192.168.200.215", // HCS 集群通信 IP
            "port": 9001, // HCS 集群通信 端口
            "status": 0, // 服务器状态: 0为在线可用(默认), 1为离线
            "sshPort": 22,// SSH访问端口
            "isUsedPrvkey": false,// SSH访问 是否使用私钥:true为使用，false为不使用
            "userName": "root",//SSH访问用户名
            "prvkeyFileContent": "AwABAgM=",// 对应byte[] 数组对象 ，认证的私钥文件内容(如果为null 则默认使用本地id_rsa私钥文件内容)
            "password": "123456" // 登录用户密码或私钥密码(密文密码需使用hcsId转换)
        }
    ]
}
/** 回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容
**/
/** 异常code：
121000：私有配置信息下发 (捕获异常)
121001：私有配置错误(json信息有误)
121011：集群信息错误(json信息有误)
121002：管理端口监听失败
--------------------------------------以上错误 中间件故障 无法工作 需重新下发配置
121003：配置文件保存失败  ---------------------（中间件异常，但可正常提供服务）
-------------------------------------以上121000、121001、121002、121003  code 为 >0  ,message为错误信息
**/


//-------------------------------------1.3.1 RS to M_自动异常切换 flags：	3-----废弃---------------------------------------


//------------------------------------1.4.1 M to RS_主从手动切换 flags：	4-----废弃--------------------------------------


//------------------------------------1.5.1 M to RS_RDS Server组停用 flags：	5-----废弃--------------------------------------


//------------------------------------1.6.1 M to RS_RDS Server组启用 flags：	6-----废弃--------------------------------------


//------------------------------------1.7.1 M to RS_RDS Server组移除 flags：	7-----废弃--------------------------------------


//------------------------------------1.8.1 HCM_S_S_HCS 配置变更 flags：	8---修改---------------------------------------------
/** data：**/
{
    "taskId": "uuid", // 任务唯一Id, 由HCM下发时,HCS响应必须带该字段值(HCS集群主动发起时则不需要)
    "systemConfig": { 
    	// HCS集群各节点私有配置(可根据不同服务进行配置下发，各节点独自应用) 目前项不为最终项,根据实际实现情况再添加
    }
}
/** 回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容
**/
/** 异常code：
181000：私有配置信息变更 (捕获异常)
181001：私有配置错误(json信息有误)
181002：新管理端口监听失败(依然使用老的端口)
181003：配置文件保存失败 
-------------------------------------以上181000、181001、181002、181003  code 为 >0  ,message为错误信息
**/


//------------------------------------1.9.1 M to RS_RDS Server组手动恢复 flags：	9-----废弃-----------------------------------


//------------------------------------2.21.1 HCM_R_S_HCS RDS实例初始化添加 flags：21---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，但如果多个实例时，且不同HCS节点将上报 同一个任务ID 下不同实例(每个集群节点监管部分RDS实例)的处理情况
CODE >0 则code对应的单个具体错误码，message为错误信息
CODE = -1 则message为JSON数组信息(即"message":[{code,message}] 其中code = 0 表示成功，>0表示对应错误码，message表示对应 实例ID字符串)
**/
/** 异常code：
2211000：RDS实例初始化 (捕获异常) (回复节点为Leader或监管节点，taskId  能拿到则回复实际taskId ，否则为 -1)
2211010：RDS实例初始化添加 信息错误(json信息有误) (仅Leader节点回复，taskId  能拿到则回复实际taskId ，否则为 -1)
-------------------------------------以上2211000、2211010  code 为 >0  ,message为错误信息
-------------------------------------以下code为=-1, 具体 内容在："message":[{code,message}]  中
2211011: RDS实例初始化数据源失败(但RDS实例已被集群节点监管，后续只能：处理数据源连接问题 或 移除RDS实例)
**/


//------------------------------------2.22.1 HCM_R_S_HCS RDS实例分配 flags：22---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，但如果多个实例时，且不同HCS节点将上报 同一个任务ID 下不同实例(每个集群节点监管部分RDS实例)的处理情况
CODE >0 则code对应的单个具体错误码，message为错误信息
CODE = -1 则message为JSON数组信息(即"message":[{code,message}] 其中code = 0 表示成功，>0表示对应错误码，message表示对应 实例ID字符串)
**/
/** 异常code：
2221000：RDS实例分配 失败(捕获异常)(回复节点为Leader或监管节点，taskId  能拿到则回复实际taskId ，否则为 -1)
2221001：RDS实例分配 信息内容错误(json信息有误)(仅Leader节点回复，taskId  能拿到则回复实际taskId ，否则为 -1)
以上为 code > 0，为实际异常编号（message为错误信息），以下 code 为 -1 ，详情在 message:[{code: ,message:}] 中
2221002：RDS实例不为 库存 状态(监管节点回复，其他节点 实例状态均不改变)
2221003：RDS实例监听失败(实例依然置为已分配状态，实例监管发生切换时需重新监听)  
2221011：RDS实例VIP添加失败(实例依然置为已分配状态，实例监管发生切换时需重新添加VIP)
**/


//------------------------------------2.23.1 HCM_R_S_HCS RDS实例手动启用 flags：	23---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，但如果多个实例时，且不同HCS节点将上报 同一个任务ID 下不同实例(每个集群节点监管部分RDS实例)的处理情况
CODE >0 则code对应的单个具体错误码，message为错误信息
 CODE = -1 则message为JSON数组信息(即"message":[{code,message}] 其中code = 0 表示成功，>0表示对应错误码，message表示对应 实例ID字符串)
**/


//------------------------------------2.24.1 HCM_R_S_HCS RDS实例手动停用 flags：	24---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，但如果多个实例时，且不同HCS节点将上报 同一个任务ID 下不同实例(每个集群节点监管部分RDS实例)的处理情况
CODE >0 则code对应的单个具体错误码，message为错误信息
 CODE = -1 则message为JSON数组信息(即"message":[{code,message}] 其中code = 0 表示成功，>0表示对应错误码，message表示对应 实例ID字符串)
**/


//------------------------------------2.25.1 HCM_R_S_HCS RDS实例续期 flags：	25---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，但如果多个实例时，且不同HCS节点将上报 同一个任务ID 下不同实例(每个集群节点监管部分RDS实例)的处理情况
CODE >0 则code对应的单个具体错误码，message为错误信息
 CODE = -1 则message为JSON数组信息(即"message":[{code,message}] 其中code = 0 表示成功，>0表示对应错误码，message表示对应 实例ID字符串)
**/


//------------------------------------2.26.1 HCM_R_S_HCS RDS实例到期停用 flags：	26---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，但如果多个实例时，且不同HCS节点将上报 同一个任务ID 下不同实例(每个集群节点监管部分RDS实例)的处理情况
CODE >0 则code对应的单个具体错误码，message为错误信息
 CODE = -1 则message为JSON数组信息(即"message":[{code,message}] 其中code = 0 表示成功，>0表示对应错误码，message表示对应 实例ID字符串)
**/


//------------------------------------2.27.1 HCM_R_S_HCS RDS实例回收 flags：	27---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，但如果多个实例时，且不同HCS节点将上报 同一个任务ID 下不同实例(每个集群节点监管部分RDS实例)的处理情况
CODE >0 则code对应的单个具体错误码，message为错误信息
 CODE = -1 则message为JSON数组信息(即"message":[{code,message}] 其中code = 0 表示成功，>0表示对应错误码，message表示对应 实例ID字符串)
**/


//------------------------------------2.27.2 M to RS_RDS实例回收任务成功通知 flags：40-----废弃--------------------------------------
 

//------------------------------------2.28.1  HCM_R_S_HCS RDS实例移除 flags：	28---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，但如果多个实例时，且不同HCS节点将上报 同一个任务ID 下不同实例(每个集群节点监管部分RDS实例)的处理情况
CODE >0 则code对应的单个具体错误码，message为错误信息
 CODE = -1 则message为JSON数组信息(即"message":[{code,message}] 其中code = 0 表示成功，>0表示对应错误码，message表示对应 实例ID字符串)
**/
 

//------------------------------------2.29.1  HCM_R_S_HCS RDS连接用户帐号变更 flags：	29---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，由监管实例节点按任务ID 进行上报
CODE >0 表示对应错误码，message为错误信息
code = 0 表示成功，message内容忽略
**/
 

//------------------------------------2.30.1 HCM_R_S_HCS RDS IP白名单变更 flags：30---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，由监管实例节点按任务ID 进行上报
CODE >0 表示对应错误码，message为错误信息
code = 0 表示成功，message内容忽略
**/
 

//-----------------------------------2.31.1  HCM_R_S_HCS RDS RDS SQL拦截 flags：31---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，由监管实例节点按任务ID 进行上报
CODE >0 表示对应错误码，message为错误信息
code = 0 表示成功，message内容忽略
**/
 

//-----------------------------------2.32.1 HCM_R_S_HCS RDS 数据库删除 flags：32---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，由监管实例节点按任务ID 进行上报
CODE >0 表示对应错误码，message为错误信息
code = 0 表示成功，message内容忽略
**/
 

//-----------------------------------2.33.1 HCM_R_S_HCS RDS实例编辑 flags：33---修改------------------------------------
/**请求说明：目前仅允许变更 **/

/** data：**/
{
    "taskId": "uuid", // 任务唯一Id, RS响应必须带该字段值
	"rdsId" : "HotDB_RDS_MM01_1", // 实例Id
	"maxUserConnections" : 0, // 用户前端最大连接数, 0为不限制
	"writeStatus:" 0, // 写状态：0为可写，1为不可写，只允许查询
}
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，由监管实例节点按任务ID 进行上报
CODE >0 表示对应错误码，message为错误信息
code = 0 表示成功，message内容忽略
**/
 

//-----------------------------------2.34.1 HCM_R_S_HCS RDS 数据库添加 flags：34---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，由监管实例节点按任务ID 进行上报
CODE >0 表示对应错误码，message为错误信息
code = 0 表示成功，message内容忽略
**/
 

//-----------------------------------2.35.1 M to RS_RDS 数据库编辑 flags：35-----废弃--------------------------------------
 

//-----------------------------------2.36.1 HCM_R_S_HCS RDS读写分离设置 flags：36---修改------------------------------------
/** data：**/
{
    "taskId": "uuid", // 任务唯一Id, RS响应必须带该字段值
	"rdsId" : "HotDB_RDS_MM01_1", // 实例Id
	"strategyForRWSplit" : 0, // 不开启读写分离：0；可分离的读请求按 weightForSlaveRWSplit 权重比随机计算后 发往 主 / 可用读数据源(包含备)：1；可分离的读请求发往可用备数据源：2；3 //按照分配的权重进行读操作
	"weightForSlaveRWSplit" : 50, // 从机读比例，默认50（百分比）,为0时代表该参数无效
	"readerWeight":"40,30,20,0,10,10" //strategyForRWSplit为3时使用此配置，固定格式第一个为主权重第二个为备的权重，后面的为只读的权重按id排序对应，保证总权重为100
}
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，由监管实例节点按任务ID 进行上报
CODE >0 表示对应错误码，message为错误信息
code = 0 表示成功，message内容忽略
**/
 

//-----------------------------------2.37.1 HCM_S_S_HCS RDS下MySQL高可用状态 flags：37---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容(且与原协议一致)，只是由管控通过 HCM_S_S_HCS 下发至 监管实例节点，监管节点再按任务ID 进行上报
**/
 

//-----------------------------------2.38.1 HCS_S_S_HCM RDS下MySQL实例状态变化通知 flags：38---修改------------------------------------
/** data：JSON内容保持不变 
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容(且与原协议一致)，只是由实例监管节点进行上报
**/
 

//-----------------------------------2.39.1 HCM_S_S_HCS RDS下操作数统计 flags：39---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容(且与原协议一致)，只是由管控通过 HCM_S_S_HCS 下发至 监管实例节点，监管节点再按任务ID  匹配对应rdsId进行上报
**/


//------------------------------------2.41.1 HCM_R_S_HCS sql审计配置信息 flags：	41---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，但如果多个实例时，且不同HCS节点将上报 同一个任务ID 下不同实例(每个集群节点监管部分RDS实例)的处理情况
CODE >0 则code对应的单个具体错误码，message为错误信息
 CODE = -1 则message为JSON数组信息(即"message":[{code,message}] 其中code = 0 表示成功，>0表示对应错误码，message表示对应 实例ID字符串)
**/


//----------------------------------2.42.1 HCM_S_S_HCS RDS实例所有后端连接新建 flags：42---修改------------------------------------
/** data：JSON内容保持不变**/
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容(且与原协议一致)，只是由管控通过 HCM_S_S_HCS 下发至 监管实例节点，监管节点再按任务ID 进行上报
**/
 

//-----------------------------------2.43.1 HCM_R_S_HCS 重置RDS数据库与连接用户 flags：43---修改------------------------------------
/** data：**/
{
    "taskId": "uuid", // 任务唯一Id, RS响应必须带该字段值
	"rdsId" : "HotDB_RDS_MM01_1", // 实例Id
	"dbInfos" : [ // 数据库数组
		{
			"dbId": 1 , // 数据库I
			"databaseName" : "test", // 数据库名称
			"characterset ": "utf8" // 数据库字符集
		}
	],
	"userInfos" : [ // 用户数组
		{
			"id":1,
			"userName" : "test",
			"password" : "test",
			"host" : "127.0.0.1", // 主机字符串
			"databases" : ["",""] // 可访问的数据库名
		}
	]
}
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，由监管实例节点按任务ID 进行上报
CODE >0 表示对应错误码，message为错误信息
code = 0 表示成功，message内容忽略
**/
 

//-----------------------------------2.44.1 HCM_R_S_HCS RDS实例升级 flags：44---修改------------------------------------
/** data：**/
{
    "taskId": "uuid", // 任务唯一Id, RS响应必须带该字段值
	"rdsId" : "HotDB_RDS_MM01_1", // 实例Id
	"modeltype": 1,
	"mysqlInfos": [
		{
			"copyfromId": 1,
			"copyfromIp": "127.0.0.1",
			"copyfromPort": 3306,
			"id": 1,
			"ip": "127.0.0.1",
			"managePassword": "64DF12CEBA7ECF40B630614DD6A68CD9",
			"manageUser": "root",
			"masterSlaveType": 1,
			"port": 3306,
			"version": "5.6"
		}
	]
}
/**回应：
HCS_S_S_HCM 复用	 1.0.0 Socket响应结果 内容，由监管实例节点按任务ID 进行上报
CODE >0 表示对应错误码，message为错误信息
code = 0 表示成功，message内容忽略
**/


//----------------------------------3.1.1 M to RS_RDS 应用连接数_管理端SQL-----废弃--------------------------------------


//-----------------------------------3.1.2 M to RS_RDS主备切换_管理端SQL---修改------------------------------------
/**请求说明（在原说明基础上追加）：	必须在连上 监管 该RDS 实例 的HCS 管理端 执行命令**/
/**返回（在原返回上追加）：
10050 (errno) :  RDS 不在该HCS节点(message)
**/


//-----------------------------------3.1.3 M to RS_RDS HOLD_管理端SQL---修改------------------------------------
/**请求说明（在原说明基础上追加）：	必须在连上 监管 该RDS 实例 的HCS 管理端 执行命令**/
/**返回（在原返回上追加）：
10050 (errno) :  RDS 不在该HCS节点(message)
**/


//-----------------------------------3.1.4 M to RS_RDS Server 当前版本_管理端SQL---不变------------------------------------
/**请求说明（在原说明基础上追加）：	必须在连上 各 HCS 管理端 执行命令**/


//------------------------------------ HCS_R_S_HCS flags: -----新增--------------------------------------
// 增加实例  HCS 组 之间 迁移接口(一个移除实例 一个 增加实例)， 一个HCS组移除实例 ，另一个HCS组  整个实例信息下发 并根据状态对应操作  
// 增加协议 由Leader 告知当前实例 分配情况 至HCM