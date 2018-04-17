//---------------------------完整集群编组信息-------------------------------------------------
{
    "taskId": "taskId1",
    "currentIp": "192.168.200.215",
    "managerIp": "192.168.200.215",
    "managerPort": 3325,
    "managerUser": "root",
    "managerPassword": "A408F9A4940DDA8AB12E0F28D2F40735",
    "cryptMandatory": true,
    "groupId": "1",
    "masterIp": "192.168.200.215",
    "masterRSId": "rds001",
    "slaveIp": "127.0.0.1",
    "slaveRSId": "rds002",
    "master": {
        "systemConfig": {
            "heartbeatToM": 2000,
            "usingAIO": 1,
            "enableHeartbeat": false,
            "heartbeatPeriod": 2000,
            "heartbeatNextWaitTimes": 500,
            "processors": 8,
            "processorExecutor": 8,
            "timerExecutor": 4,
            "parkPeriod": 100000,
            "waitForSlaveInFailover": true
        }
    },
    "slave": {
        "systemConfig": {
            "heartbeatToM": 2000,
            "usingAIO": 1,
            "enableHeartbeat": true,
            "heartbeatPeriod": 2000,
            "heartbeatNextWaitTimes": 500,
            "processors": 8,
            "processorExecutor": 4,
            "timerExecutor": 4,
            "parkPeriod": 100000,
            "waitForSlaveInFailover": true
        }
    },
    "rdsInstances": [
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
}
