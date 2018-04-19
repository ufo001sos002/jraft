//---------------------------������Ⱥ������Ϣ-------------------------------------------------
{
	"taskId" : "uuid" , // ����ΨһId, RS��Ӧ��������ֶ�ֵ
	// --------˽������ ����(����ϸ��)-----------------------
	"systemConfig": {
            "heartbeatToM": 2000,
            "usingAIO": 1
	}
	,
	//--------�������� ����---------
	"hcsGroup":[ // ��Ⱥ�ڵ���Ϣ
	   {
		   "hcsId": 1, // hcs�ڵ� Id
		   "ip": "192.168.200.215", // ��Ⱥ hcs�ڵ� IP
		   "port":9001 // ��Ⱥhcs �ڵ� RaftͨѶ�˿�
 	   },
	   {
		   "hcsId": 2, // hcs�ڵ� Id
		   "ip": "192.168.200.215", // ��Ⱥ hcs�ڵ� IP
		   "port":9002 // ��Ⱥhcs �ڵ� RaftͨѶ�˿�
	   },
	   {
		   "hcsId": 3, // hcs�ڵ� Id
		   "ip": "192.168.200.215", // ��Ⱥ hcs�ڵ� IP
		   "port":9003 // ��Ⱥhcs �ڵ� RaftͨѶ�˿�
	   }
	]
	,
	"rdsInstances": [ //  ��Ⱥ������Ҫ����� ʵ������ ��ԭ��һ��-----
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
	"allocationRule":{// ʵ���������
		"rule": 0 // 0Ϊ����(Ĭ��) ���нڵ���ѯ����
	}
	,
	// --------�������� ����-----------------------
	"rdsAllocation":[ // ʵ������
		"hcsId":1, // hcs�ڵ�id
		"rdsId":["HotDB_RDS_MM01_1"] // ʵ��id
	]

}


// -------------ʵ����̬�л�����-------------------
{
	"rdsAllocation":[ // ʵ������
		"hcsId":1, // hcs�ڵ�id
		"rdsId":["HotDB_RDS_MM01_1"] ,// ʵ��id
		"opType": 1 // 1���� 2�Ƴ�
	]

}


//-----------------���ʵ��������ʵ�� ��ʵ���ջء�ʵ���Ƴ� ���ɹ��ذ�  JSONЭ����֮ǰһ��---------------------

//-----------------��Ӽ�Ⱥ�ڵ�----------------------------
{
	"hcsGroup":[ // ��Ⱥ�ڵ���Ϣ
	   {
		   "hcsId": 3, // hcs�ڵ� Id
		   "ip": "192.168.200.215", // ��Ⱥ hcs�ڵ� IP
		   "port":9003 // ��Ⱥhcs �ڵ� RaftͨѶ�˿�
	   }
	]
}

//------------------�Ƴ���Ⱥ�ڵ�-----------------------
{
	"hcsGroup":[ // ��Ⱥ�ڵ���Ϣ
	   {
		   "hcsId": 3 // hcs�ڵ� Id
	   }
	]
}