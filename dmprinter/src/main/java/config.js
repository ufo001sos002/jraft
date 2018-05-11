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








// ----------------------------------------------------HotDB Cloud Server Cluster 通讯协议完整版--------------------------------------------------------------------------------------

// ---------------------------------------1.2.1 M to RS_RS 编组信息   2--------------------------------------------------
{
    "taskId": "uuid",
    "currentIp": "127.0.0.1",
    "managerPort": 3323,
    "managerUser": "root",
    "managerPassword": "123456",
    "namesrvAddr": "127.0.0.1:9786",
    "topic": "topicName",
    "tags": "tagsName",
    "systemConfig": {
        "heartbeatToM": 3000,
        "usingAIO": 1,
        "enableHeartbeat": true,
        "heartbeatPeriod": 3000,
        "heartbeatNextWaitTimes": 3000,
        "processors": 16,
        "processorExecutor": 4,
        "timerExecutor": 6,
        "parkPeriod": 100000,
        "waitForSlaveInFailover": true,
        
    },
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
    ]
}