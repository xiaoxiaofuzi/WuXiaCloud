# 分布式事务（Seata1.4）

**这里都已1.4版本为例。**

## seata+nacos部署安装

[seata官方文档](http://seata.io/zh-cn/docs/overview/what-is-seata.html)

前提：[nacos](https://nacos.io/zh-cn/docs/quick-start.html) 安装成功。

### seata服务启动

1. 下载[seata-server1.4.0](https://github.com/seata/seata/releases)

 [seata-server-1.4.0.tar.gz](F:\谷歌下载\seata-server-1.4.0.tar.gz) 

1. 修改参数

### seata参数初始化到nacos

1. 下载执行脚本[nacos-config.sh](https://github.com/seata/seata/blob/1.4.0/script/config-center/nacos/nacos-config.sh)和初始化参数文本[config.txt](https://github.com/seata/seata/blob/1.4.0/script/config-center/config.txt)

   这里已经有下载好的[seata源码](./wx-dynamic-seata-images/seata-1.4.0.zip)

   [nacos-config.sh](./wx-dynamic-seata-images/nacos-config.sh)

   ```sh
   #!/usr/bin/env bash
   # Copyright 1999-2019 Seata.io Group.
   #
   # Licensed under the Apache License, Version 2.0 (the "License");
   # you may not use this file except in compliance with the License.
   # You may obtain a copy of the License at、
   #
   #      http://www.apache.org/licenses/LICENSE-2.0
   #
   # Unless required by applicable law or agreed to in writing, software
   # distributed under the License is distributed on an "AS IS" BASIS,
   # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   # See the License for the specific language governing permissions and
   # limitations under the License.
   
   while getopts ":h:p:g:t:u:w:" opt
   do
     case $opt in
     h)
       host=$OPTARG
       ;;
     p)
       port=$OPTARG
       ;;
     g)
       group=$OPTARG
       ;;
     t)
       tenant=$OPTARG
       ;;
     u)
       username=$OPTARG
       ;;
     w)
       password=$OPTARG
       ;;
     ?)
       echo " USAGE OPTION: $0 [-h host] [-p port] [-g group] [-t tenant] [-u username] [-w password] "
       exit 1
       ;;
     esac
   done
   
   if [[ -z ${host} ]]; then
       host=localhost
   fi
   if [[ -z ${port} ]]; then
       port=8848
   fi
   if [[ -z ${group} ]]; then
       group="SEATA_GROUP"
   fi
   if [[ -z ${tenant} ]]; then
       tenant=""
   fi
   if [[ -z ${username} ]]; then
       username=""
   fi
   if [[ -z ${password} ]]; then
       password=""
   fi
   
   nacosAddr=$host:$port
   contentType="content-type:application/json;charset=UTF-8"
   
   echo "set nacosAddr=$nacosAddr"
   echo "set group=$group"
   
   failCount=0
   tempLog=$(mktemp -u)
   function addConfig() {
     curl -X POST -H "${contentType}" "http://$nacosAddr/nacos/v1/cs/configs?dataId=$1&group=$group&content=$2&tenant=$tenant&username=$username&password=$password" >"${tempLog}" 2>/dev/null
     if [[ -z $(cat "${tempLog}") ]]; then
       echo " Please check the cluster status. "
       exit 1
     fi
     if [[ $(cat "${tempLog}") =~ "true" ]]; then
       echo "Set $1=$2 successfully "
     else
       echo "Set $1=$2 failure "
       (( failCount++ ))
     fi
   }
   
   count=0
   for line in $(cat $(dirname "$PWD")/config.txt | sed s/[[:space:]]//g); do
     (( count++ ))
   	key=${line%%=*}
       value=${line#*=}
   	addConfig "${key}" "${value}"
   done
   
   echo "========================================================================="
   echo " Complete initialization parameters,  total-count:$count ,  failure-count:$failCount "
   echo "========================================================================="
   
   if [[ ${failCount} -eq 0 ]]; then
   	echo " Init nacos config finished, please start seata-server. "
   else
   	echo " init nacos config fail. "
   fi
   ```

   [config.txt](./wx-dynamic-seata-images/config.txt)

   ```properties
   transport.type=TCP
   transport.server=NIO
   transport.heartbeat=true
   transport.enableClientBatchSendRequest=false
   transport.threadFactory.bossThreadPrefix=NettyBoss
   transport.threadFactory.workerThreadPrefix=NettyServerNIOWorker
   transport.threadFactory.serverExecutorThreadPrefix=NettyServerBizHandler
   transport.threadFactory.shareBossWorker=false
   transport.threadFactory.clientSelectorThreadPrefix=NettyClientSelector
   transport.threadFactory.clientSelectorThreadSize=1
   transport.threadFactory.clientWorkerThreadPrefix=NettyClientWorkerThread
   transport.threadFactory.bossThreadSize=1
   transport.threadFactory.workerThreadSize=default
   transport.shutdown.wait=3
   service.vgroupMapping.my_test_tx_group=default
   service.default.grouplist=127.0.0.1:8091
   service.enableDegrade=false
   service.disableGlobalTransaction=false
   client.rm.asyncCommitBufferLimit=10000
   client.rm.lock.retryInterval=10
   client.rm.lock.retryTimes=30
   client.rm.lock.retryPolicyBranchRollbackOnConflict=true
   client.rm.reportRetryCount=5
   client.rm.tableMetaCheckEnable=false
   client.rm.sqlParserType=druid
   client.rm.reportSuccessEnable=false
   client.rm.sagaBranchRegisterEnable=false
   client.tm.commitRetryCount=5
   client.tm.rollbackRetryCount=5
   client.tm.defaultGlobalTransactionTimeout=60000
   client.tm.degradeCheck=false
   client.tm.degradeCheckAllowTimes=10
   client.tm.degradeCheckPeriod=2000
   store.mode=file
   store.file.dir=file_store/data
   store.file.maxBranchSessionSize=16384
   store.file.maxGlobalSessionSize=512
   store.file.fileWriteBufferCacheSize=16384
   store.file.flushDiskMode=async
   store.file.sessionReloadReadSize=100
   store.db.datasource=druid
   store.db.dbType=mysql
   store.db.driverClassName=com.mysql.jdbc.Driver
   store.db.url=jdbc:mysql://127.0.0.1:3306/seata?useUnicode=true
   store.db.user=username
   store.db.password=password
   store.db.minConn=5
   store.db.maxConn=30
   store.db.globalTable=global_table
   store.db.branchTable=branch_table
   store.db.queryLimit=100
   store.db.lockTable=lock_table
   store.db.maxWait=5000
   store.redis.host=127.0.0.1
   store.redis.port=6379
   store.redis.maxConn=10
   store.redis.minConn=1
   store.redis.database=0
   store.redis.password=null
   store.redis.queryLimit=100
   server.recovery.committingRetryPeriod=1000
   server.recovery.asynCommittingRetryPeriod=1000
   server.recovery.rollbackingRetryPeriod=1000
   server.recovery.timeoutRetryPeriod=1000
   server.maxCommitRetryTimeout=-1
   server.maxRollbackRetryTimeout=-1
   server.rollbackRetryTimeoutUnlockEnable=false
   client.undo.dataValidation=true
   client.undo.logSerialization=jackson
   client.undo.onlyCareUpdateColumns=true
   server.undo.logSaveDays=7
   server.undo.logDeletePeriod=86400000
   client.undo.logTable=undo_log
   client.log.exceptionRate=100
   transport.serialization=seata
   transport.compressor=none
   metrics.enabled=false
   metrics.registryType=compact
   metrics.exporterList=prometheus
   metrics.exporterPrometheusPort=9898
   
   ```

   

2. 

   

## seata+dynamic-datasource 集成

[seata+dynamic-datasource官方文档](https://dynamic-datasource.com/guide/tx/Seata.html)