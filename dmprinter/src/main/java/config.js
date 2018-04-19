//---------------------------完整集群编组信息-------------------------------------------------
{
	"taskId" : "uuid" , // 任务唯一Id, RS响应必须带该字段值
	// --------私有配置 部分(后期细化)-----------------------
	"systemConfig": {
            "heartbeatToM": 2000,
            "usingAIO": 1
	}
	,
	//--------公有配置 部分---------
	"hcsGroup":[ // 集群节点信息
	   {
		   "hcsId": 1, // hcs节点 Id
		   "ip": "192.168.200.215", // 集群 hcs节点 IP
		   "port":9001 // 集群hcs 节点 Raft通讯端口
 	   },
	   {
		   "hcsId": 2, // hcs节点 Id
		   "ip": "192.168.200.215", // 集群 hcs节点 IP
		   "port":9002 // 集群hcs 节点 Raft通讯端口
	   },
	   {
		   "hcsId": 3, // hcs节点 Id
		   "ip": "192.168.200.215", // 集群 hcs节点 IP
		   "port":9003 // 集群hcs 节点 Raft通讯端口
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
	"rdsAllocation":[ // 实例分配
		"hcsId":1, // hcs节点id
		"rdsId":["HotDB_RDS_MM01_1"] // 实例id
	]

}


// -------------实例动态切换部分-------------------
{
	"rdsAllocation":[ // 实例分配
		"hcsId":1, // hcs节点id
		"rdsId":["HotDB_RDS_MM01_1"] ,// 实例id
		"opType": 1 // 1增加 2移除
	]

}


//-----------------添加实例、分配实例 、实例收回、实例移出 、成功回包  JSON协议与之前一致---------------------

//-----------------添加集群节点----------------------------
{
	"hcsGroup":[ // 集群节点信息
	   {
		   "hcsId": 3, // hcs节点 Id
		   "ip": "192.168.200.215", // 集群 hcs节点 IP
		   "port":9003 // 集群hcs 节点 Raft通讯端口
	   }
	]
}

//------------------移除集群节点-----------------------
{
	"hcsGroup":[ // 集群节点信息
	   {
		   "hcsId": 3 // hcs节点 Id
	   }
	]
}