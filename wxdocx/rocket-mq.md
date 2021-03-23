# Rocket-MQ

## 1. 安装

1. 64位操作系统，Linux
2. jdk 1.8+

### 开启name服务

[rocketmq-all-4.8.0-bin-release.zip](./accessory/rocketmq-all-4.8.0-bin-release.zip)

```shell
#解压
unzip rocketmq-all-4.8.0-bin-release.zip 
cd rocketmq-all-4.8.0-bin-release
#开启名称服务器
nohup sh bin/mqnamesrv >./logs/namesrv.log 2>1 &
tailf logs/namesrv.log
```

![1616400247486](./accessory/名称服务器启动成功.png)

### 开启broker服务

```shell
#开启broker
nohup sh bin/mqbroker -n localhost:9876 -c conf/broker.conf >./logs/broker.log 2>1 &
tailf logs/broker.log 
```

![1616402508239](./accessory/broker启动成功.png)

### 内存不足解决方案

**如果遇到虚拟机启动不了：内存不够**，则需要重新设置启动参数，namesrv默认4g,broker默认8g，

修改bin目录中的两个文件。

![1616402668857](./accessory/修改参数.png)

### 发送和接收消息

```shell
#临时设置环境变量，下面类中需要用到
export NAMESRV_ADDR=localhost:9876
#开启生产者发送消息
sh bin/tools.sh org.apache.rocketmq.example.quickstart.Producer
#开启消费者消费消息
sh bin/tools.sh org.apache.rocketmq.example.quickstart.Consumer
```

**生产者**

![1616403115867](./accessory/生产者.png)

 **消费者**

​                          ![1616403160140](./accessory/\消费者.png) 

### 关闭服务器

```shell
sh bin/mqshutdown broker
sh bin/mqshutdown namesrv
```

## 2. 介绍

[Apache RocketMQ开发者指南](https://github.com/apache/rocketmq/tree/master/docs/cn#apache-rocketmq%E5%BC%80%E5%8F%91%E8%80%85%E6%8C%87%E5%8D%97)

架构图：

![](./accessory/架构图.png)



### 1. 消息存储

#### 1.1 消息存储整体架构

Broker 单个实例下 所有队列共用一个 CommitLog(日志数据文件)存储。producer 发送消息到 Broker，Broker 会采用同步或者一部的方式对消息进行刷盘持久化，保存进 CommitLog中。Consumer 消费消息通过ConsumeQueue【**consumequeue文件采取定长设计，每一个条目共20个字节，分别为8字节的commitlog物理偏移量、4字节的消息长度、8字节tag hashcode，单个文件由30W个条目组成，可以像数组一样随机访问每一个条目，每个ConsumeQueue文件大小约5.72M**】提高消费性能。消费端当无法拉取到消息时，可以等下一次消息拉取，同时服务端也支持长轮询模式，如果一个消息拉取请求未拉取到消息，Broker允许等待30s的时间，只要这段时间内有新消息到达，将直接返回给消费端。这里，Rocket MQ的具体做法是，使用Broker端的后台服务线程 — ReputMessageService 不停地分发请求并异步构建 ConsumeQueue（逻辑消费队列）和 IndexFile（索引文件）数据。

#### 1.2 页缓存与内存映射

页缓存（PageCache)是OS对文件的缓存，用于加速对文件的读写。一般来说，程序对文件进行顺序读写的速度几乎接近于内存的读写速度，主要原因就是由于OS使用PageCache机制对读写访问操作进行了性能优化，将一部分的内存用作PageCache。对于数据的写入，OS会先写入至Cache内，随后通过异步的方式由pdflush内核线程将Cache内的数据刷盘至物理磁盘上。对于数据的读取，如果一次读取文件时出现未命中PageCache的情况，OS从物理磁盘上访问读取文件的同时，会顺序对其他相邻块的数据文件进行预读取。

在RocketMQ中，ConsumeQueue逻辑消费队列存储的数据较少，并且是顺序读取，在page cache机制的预读取作用下，Consume Queue文件的读性能几乎接近读内存，即使在有消息堆积情况下也不会影响性能。而对于CommitLog消息存储的日志数据文件来说，读取消息内容时候会产生较多的随机访问读取，严重影响性能。如果选择合适的系统IO调度算法，比如设置调度算法为“Deadline”（此时块存储采用SSD的话），随机读的性能也会有所提升。

另外，RocketMQ主要通过MappedByteBuffer对文件进行读写操作。其中，利用了NIO中的FileChannel模型将磁盘上的物理文件直接映射到用户态的内存地址中（这种Mmap的方式:零拷贝技术），将对文件的操作转化为直接对内存地址进行操作，从而极大地提高了文件的读写效率（正因为需要使用内存映射机制，故RocketMQ的文件存储都使用定长结构来存储，方便一次将整个文件映射至内存）。

#### 1.3 消息刷盘

(1) 同步刷盘：只有在消息真正持久化至磁盘后RocketMQ的Broker端才会真正返回给Producer端一个成功的ACK响应。同步刷盘对MQ消息可靠性来说是一种不错的保障，但是性能上会有较大影响，一般适用于金融业务应用该模式较多。

(2) 异步刷盘：能够充分利用OS的PageCache的优势，只要消息写入PageCache即可将成功的ACK返回给Producer端。消息刷盘采用后台异步线程提交的方式进行，降低了读写延迟，提高了MQ的性能和吞吐量。

### 2. 通信机制

RocketMQ消息队列集群主要包括NameServer、Broker(Master/Slave)、Producer、Consumer 4个角色，基本通讯流程如下：

1. Broker 启动后需要完成一次将自己注册到 NameServer 的操作，随后每隔30s定时向 NameServer 上报Topic 路由信息。
2. producer 发送消息会根据消息的Topic从本地缓存 TopicPublishInfoTable 获取路由信息。如果没有会拉取 NameServer 上的路由信息，并默认 每隔30s 向 NameServer 拉取一次路由信息。
3. producer 从路由信息中 选择一个 队列（MesageQueue）进行消息发送，Broker 作为消息的接受者接受消息并落盘存储。
4. Consumer 获取路由信息，再通过客户端的负载均衡之后，选择其中的某一个或某几个消息队列来拉取消息并进行消费。

从上面1）~3）中可以看出在消息生产者, Broker和NameServer之间都会发生通信（这里只说了MQ的部分通信），因此如何设计一个良好的网络通信模块在MQ中至关重要，它将决定RocketMQ集群整体的消息传输能力与最终的性能。

rocketmq-remoting 模块是 RocketMQ消息队列中负责网络通信的模块，它几乎被其他所有需要网络通信的模块（诸如rocketmq-client、rocketmq-broker、rocketmq-namesrv）所依赖和引用。为了实现客户端与服务器之间高效的数据请求与接收，RocketMQ消息队列自定义了通信协议并在Netty的基础之上扩展了通信模块



#### 2.1 协议设计与编解码

在Client和Server之间完成一次消息发送时，需要对发送的消息进行一个协议约定，因此就有必要自定义RocketMQ的消息协议。同时，为了高效地在网络中传输消息和对收到的消息读取，就需要对消息进行编解码。在RocketMQ中，**RemotingCommand** 这个类在消息传输过程中对所有数据内容的封装，不但包含了所有的数据结构，还包含了编码解码操作

![1616465519736](accessory/RemotingCommand.png)



![](accessory/rocketmq_design_4.png)

可见传输内容主要可以分为以下4部分：

(1) 消息长度：总长度，四个字节存储，占用一个int类型；

(2) 序列化类型&消息头长度：同样占用一个int类型，第一个字节表示序列化类型，后面三个字节表示消息头长度；

(3) 消息头数据：经过序列化后的消息头数据；

(4) 消息主体数据：消息主体的二进制字节数据内容；

#### 2.2 消息的通信方式和流程

在RocketMQ消息队列中支持通信的方式主要有同步(sync)、异步(async)、单向(oneway)三种。其中“单向”通信模式相对简单，一般用在发送心跳包场景下，无需关注其Response。

#### 2.3 Reactor多线程设计

*理解一下*

### 3. 消息过滤

RocketMQ分布式消息队列的消息过滤方式有别于其它MQ中间件，是在Consumer端订阅消息时再做消息过滤的。RocketMQ这么做是在于其Producer端写入消息和Consumer端订阅消息采用分离存储的机制来实现的，Consumer端订阅消息是需要通过ConsumeQueue这个消息消费的逻辑队列拿到一个索引，然后再从CommitLog里面读取真正的消息实体内容，所以说到底也是还绕不开其存储结构。其ConsumeQueue的存储结构如下，可以看到其中有8个字节存储的Message Tag的哈希值，基于Tag的消息过滤正式基于这个字段值的。

![](accessory/rocketmq_design_7.png)

主要支持如下2种的过滤方式

#### 3.1 TAG

(1) Tag过滤方式：Consumer端在订阅消息时除了指定Topic还可以指定TAG，如果一个消息有多个TAG，可以用||分隔。其中，Consumer端会将这个订阅请求构建成一个 SubscriptionData，发送一个Pull消息的请求给Broker端。Broker端从RocketMQ的文件存储层—Store读取数据之前，会用这些数据先构建一个MessageFilter，然后传给Store。Store从 ConsumeQueue读取到一条记录后，会用它记录的消息tag hash值去做过滤，由于在**服务端**只是**根据hashcode**进行判断，**无法精确**对tag原始字符串进行**过滤**，故在消息**消费端**拉取到消息后，还需要对消息的原始**tag字符串**进行**比对**，如果不同，则丢弃该消息，不进行消息消费。

```cpp
DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("test");
consumer.subscribe("TOPIC", "TAG1 || TAG2 || TAG3");
```

不过一条消息只能包含一个tag，无法处理复杂的场景，这个时候可以使用SQL92的过滤方式，实现一些有趣的过滤逻辑。

#### 3.2 SQL92

(2) SQL92的过滤方式：这种方式的大致做法和上面的Tag过滤方式一样，只是在Store层的具体过滤过程不太一样，真正的 SQL expression 的构建和执行由rocketmq-filter模块负责的。每次过滤都去执行SQL表达式会影响效率，所以RocketMQ使用了BloomFilter避免了每次都去执行。SQL92的表达式上下文为消息的属性。

[参考](https://www.jianshu.com/p/ef74f350e3bc)

```shell
sh bin/mqshutdown broker
sh bin/mqshutdown namesrv

#broker.conf中添加一下属性，并重启 broker  namesrv
enablePropertyFilter=true

nohup sh bin/mqnamesrv >./logs/namesrv.log 2>1 &
nohup sh bin/mqbroker -n localhost:9876 -c conf/broker.conf >./logs/broker.log 2>1 &

```



![1616468271345](accessory/sql92支持.png)

```java
//生产者
 Message msg = new Message("TopicTest",
                        "TagA"+i,
                        "OrderID188",
                        ("sql92过滤").getBytes(RemotingHelper.DEFAULT_CHARSET));
msg.putUserProperty("test",String.valueOf(i));

//消费者,去 tags为 TagA1,TagA2 并且自定义值test在[7,9]之间的消息
consumer.subscribe("TopicTest",MessageSelector.bySql("(TAGS is not null and TAGS in ('TagA1','TagA2')) " +
                "or (test is not null and test between 7 and 9)"));
```

### 4. 负载均衡

都在 Client 端完成。

#### 4.1 Producer的负载均衡

Producer端在发送消息的时候，会先根据Topic找到指定的TopicPublishInfo，在获取了TopicPublishInfo路由信息后，RocketMQ的客户端在默认方式下selectOneMessageQueue()方法会从TopicPublishInfo中的messageQueueList中选择一个队列（MessageQueue）进行发送消息。具体的容错策略均在MQFaultStrategy这个类中定义。这里有一个sendLatencyFaultEnable开关变量，如果开启，在随机递增取模的基础上，再过滤掉not available的Broker代理。所谓的"latencyFaultTolerance"，是指对之前失败的，按一定的时间做退避。例如，如果上次请求的latency超过550Lms，就退避3000Lms；超过1000L，就退避60000L；如果关闭，采用随机递增取模的方式选择一个队列（MessageQueue）来发送消息，latencyFaultTolerance机制是实现消息发送高可用的核心关键所在。

```java
//发送消息DefaultMQProducerImpl.sendDefaultImpl()
private SendResult sendDefaultImpl()(
        Message msg,
        final CommunicationMode communicationMode,
        final SendCallback sendCallback,
        final long timeout
    ) throws MQClientException, RemotingException, MQBrokerException, InterruptedException {
        this.makeSureStateOK();
        //检查消息的topic是否符合规则（正则，长度127），是否使用了初始化的 TopicValidator.NOT_ALLOWED_SEND_TOPIC_SET
        //检查消息的body是否符合规则（默认最大4M）
        Validators.checkMessage(msg, this.defaultMQProducer);
    ...
        //根据 Topic 找到指定的 TopicPublishInfo
        TopicPublishInfo topicPublishInfo = this.tryToFindTopicPublishInfo(msg.getTopic());
        if (topicPublishInfo != null && topicPublishInfo.ok()) {
            boolean callTimeout = false;
            MessageQueue mq = null;
            Exception exception = null;
            SendResult sendResult = null;
            //重试次数，只在同步时生效
            int timesTotal = communicationMode == CommunicationMode.SYNC ? 1 + this.defaultMQProducer.getRetryTimesWhenSendFailed() : 1;
            int times = 0;
            String[] brokersSent = new String[timesTotal];
            for (; times < timesTotal; times++) {
                String lastBrokerName = null == mq ? null : mq.getBrokerName();
                //选择一个队列进行发送消息，具体的容错策略均在 MQFaultStrategy 这个类中定义[随机递增取模]
                MessageQueue mqSelected = this.selectOneMessageQueue(topicPublishInfo, lastBrokerName);
...
                        //发送消息
                        sendResult = this.sendKernelImpl(msg, mq, communicationMode, sendCallback, topicPublishInfo, timeout - costTime);
...
}

//容错策略类
public class MQFaultStrategy {
    private final static InternalLogger log = ClientLogger.getLog();
    /**
     * latencyFaultTolerance 是指对之前失败的，按一定的时间做退避。例如，如果上次请求的latency超过550Lms，就退避3000Lms；超过1000L，就退避60000L；
     * 如果关闭，采用随机递增取模的方式选择一个队列（MessageQueue）来发送消息，latencyFaultTolerance机制是实现消息发送高可用的核心关键所在。
     */
    private final LatencyFaultTolerance<String> latencyFaultTolerance = new LatencyFaultToleranceImpl();

    /**
     * 如果开启，在随机递增取模的基础上，再过滤掉not available（无法使用）的Broker代理
     */
    private boolean sendLatencyFaultEnable = false;

    private long[] latencyMax = {50L, 100L, 550L, 1000L, 2000L, 3000L, 15000L};
    private long[] notAvailableDuration = {0L, 0L, 30000L, 60000L, 120000L, 180000L, 600000L};

   ...

    public MessageQueue selectOneMessageQueue(final TopicPublishInfo tpInfo, final String lastBrokerName) {
        if (this.sendLatencyFaultEnable) {
            //过滤无法使用的broker
            try {
            //随机递增取模
                int index = tpInfo.getSendWhichQueue().getAndIncrement();
                for (int i = 0; i < tpInfo.getMessageQueueList().size(); i++) {
                    int pos = Math.abs(index++) % tpInfo.getMessageQueueList().size();
                    if (pos < 0)
                        pos = 0;
                    MessageQueue mq = tpInfo.getMessageQueueList().get(pos);
                    if (latencyFaultTolerance.isAvailable(mq.getBrokerName()))
                        return mq;
                }

                final String notBestBroker = latencyFaultTolerance.pickOneAtLeast();
                int writeQueueNums = tpInfo.getQueueIdByBroker(notBestBroker);
                if (writeQueueNums > 0) {
                    final MessageQueue mq = tpInfo.selectOneMessageQueue();
                    if (notBestBroker != null) {
                        mq.setBrokerName(notBestBroker);
                        mq.setQueueId(tpInfo.getSendWhichQueue().getAndIncrement() % writeQueueNums);
                    }
                    return mq;
                } else {
                    latencyFaultTolerance.remove(notBestBroker);
                }
            } catch (Exception e) {
                log.error("Error occurred when selecting message queue", e);
            }

            return tpInfo.selectOneMessageQueue();
        }

        return tpInfo.selectOneMessageQueue(lastBrokerName);
    }

   ....
}


```







#### 4.2 Consumer的负载均衡

在RocketMQ中，Consumer端的两种消费模式（Push/Pull）都是基于拉模式来获取消息的，而在Push模式只是对pull模式的一种封装，其本质实现为消息拉取线程在从服务器拉取到一批消息后，然后提交到消息消费线程池后，又“马不停蹄”的继续向服务器再次尝试拉取消息。如果未拉取到消息，则延迟一下又继续拉取。在两种基于拉模式的消费方式（Push/Pull）中，均需要Consumer端在知道从Broker端的哪一个消息队列—队列中去获取消息。因此，有必要在Consumer端来做负载均衡，即Broker端中多个MessageQueue分配给同一个ConsumerGroup中的哪些Consumer消费。

```java
//1.消费者开启
consumer.start();

//2.主要是mQClientFactory
mQClientFactory.start();

public void start() throws MQClientException {

        synchronized (this) {
            switch (this.serviceState) {
                case CREATE_JUST:
                    this.serviceState = ServiceState.START_FAILED;
                    // If not specified,looking address from name server
                    if (null == this.clientConfig.getNamesrvAddr()) {
                        this.mQClientAPIImpl.fetchNameServerAddr();
                    }
                    // Start request-response channel
                    //Consumer端的心跳包发送
                    this.mQClientAPIImpl.start();
                    // Start various schedule tasks
                    this.startScheduledTask();
                    // Start pull service
                    this.pullMessageService.start();
                    // Start rebalance service
                    //负载均衡服务线程开启，每隔20s执行一次，RebalanceImpl类的rebalanceByTopic()该方法是实现Consumer端负载均衡的核心。
                    this.rebalanceService.start();
                    // Start push service
                    this.defaultMQProducer.getDefaultMQProducerImpl().start(false);
                    log.info("the client factory [{}] start OK", this.clientId);
                    this.serviceState = ServiceState.RUNNING;
                    break;
                case START_FAILED:
                    throw new MQClientException("The Factory object[" + this.getClientId() + "] has been created before, and failed.", null);
                default:
                    break;
            }
        }
    }
//3.this.rebalanceService.start(): Consumer端实现负载均衡的核心类—RebalanceImpl（每隔20s执行一次）

private void rebalanceByTopic(final String topic, final boolean isOrder) {
                    switch (messageModel) {
            case BROADCASTING: {
                //广播
               ...
            }
            case CLUSTERING: {
                //集群
                //获取该Topic主题下的消息消费队列集合（mqSet）；
                Set<MessageQueue> mqSet = this.topicSubscribeInfoTable.get(topic);
                //向Broker端发送获取该消费组下消费者Id列表的RPC通信请求（Broker端基于前面Consumer端上报的心跳包数据而构建的consumerTable做出响应返回，业务请求码：GET_CONSUMER_LIST_BY_GROUP）
                List<String> cidAll = this.mQClientFactory.findConsumerIdList(topic, consumerGroup);
                if (null == mqSet) {
                    if (!topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
                        log.warn("doRebalance, {}, but the topic[{}] not exist.", consumerGroup, topic);
                    }
                }

                if (null == cidAll) {
                    log.warn("doRebalance, {} {}, get consumer id list failed", consumerGroup, topic);
                }

                if (mqSet != null && cidAll != null) {
                    List<MessageQueue> mqAll = new ArrayList<MessageQueue>();
                    mqAll.addAll(mqSet);
                    // 先对Topic下的消息消费队列、消费者Id排序
                    Collections.sort(mqAll);
                    Collections.sort(cidAll);


                    //然后用消息队列分配策略算法（默认为：消息队列的平均分配算法），计算出待拉取的消息队列。这里的平均分配算法，类似于分页的算法，
                    // 将所有MessageQueue排好序类似于记录，将所有消费端Consumer排好序类似页数，
                    // 并求出每一页需要包含的平均size和每个页面记录的范围range，最后遍历整个range而计算出当前Consumer端应该分配到的记录（这里即为：allocateResult）
                    AllocateMessageQueueStrategy strategy = this.allocateMessageQueueStrategy;
                    List<MessageQueue> allocateResult = null;
                    try {
                        allocateResult = strategy.allocate(
                            this.consumerGroup,
                            this.mQClientFactory.getClientId(),
                            mqAll,
                            cidAll);
                    } catch (Throwable e) {
                        log.error("AllocateMessageQueueStrategy.allocate Exception. allocateMessageQueueStrategyName={}", strategy.getName(),
                                  e);
                        return;
                    }

                    Set<MessageQueue> allocateResultSet = new HashSet<MessageQueue>();
                    if (allocateResult != null) {
                        allocateResultSet.addAll(allocateResult);
                    }

                    //查看方法介绍
                    boolean changed = this.updateProcessQueueTableInRebalance(topic, allocateResultSet, isOrder);
                    if (changed) {
                        log.info(
                            "rebalanced result changed. allocateMessageQueueStrategyName={}, group={}, topic={}, clientId={}, mqAllSize={}, cidAllSize={}, rebalanceResultSize={}, rebalanceResultSet={}",
                            strategy.getName(), consumerGroup, topic, this.mQClientFactory.getClientId(), mqSet.size(), cidAll.size(),
                            allocateResultSet.size(), allocateResultSet);
                        this.messageQueueChanged(topic, mqSet, allocateResultSet);
                    }
                }
                break;
            }
            default:
                break;
                    }
}

//4.updateProcessQueueTableInRebalance（）
 private boolean updateProcessQueueTableInRebalance(final String topic, final Set<MessageQueue> mqSet,
        final boolean isOrder) {
        boolean changed = false;
        //先将分配到的消息队列集合（mqSet）与processQueueTable做一个过滤比对。
        Iterator<Entry<MessageQueue, ProcessQueue>> it = this.processQueueTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<MessageQueue, ProcessQueue> next = it.next();
            MessageQueue mq = next.getKey();
            ProcessQueue pq = next.getValue();

            if (mq.getTopic().equals(topic)) {
                if (!mqSet.contains(mq)) {
                    //与分配到的消息队列集合mqSet互不包含
                    pq.setDropped(true);
                    //查看这些队列是否可以移除出processQueueTable缓存变量
                    //每隔1s 查看是否可以获取当前消费处理队列的锁，拿到的话返回true。如果等待1s后，仍然拿不到当前消费处理队列的锁则返回false。
                    //如果返回true，则从processQueueTable缓存变量中移除对应的Entry
                    if (this.removeUnnecessaryMessageQueue(mq, pq)) {
                        it.remove();
                        changed = true;
                        log.info("doRebalance, {}, remove unnecessary mq, {}", consumerGroup, mq);
                    }
                } else if (pq.isPullExpired()) {
                    //与分配到的消息队列集合mqSet的交集
                    switch (this.consumeType()) {
                        case CONSUME_ACTIVELY:
                            //Pull模式的不用管
                            break;
                        case CONSUME_PASSIVELY:
                            //Push模式的，设置Dropped属性为true，并且调用removeUnnecessaryMessageQueue()方法，像上面一样尝试移除Entry；
                            pq.setDropped(true);
                            if (this.removeUnnecessaryMessageQueue(mq, pq)) {
                                it.remove();
                                changed = true;
                                log.error("[BUG]doRebalance, {}, remove unnecessary mq, {}, because pull is pause, so try to fixed it",
                                    consumerGroup, mq);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        List<PullRequest> pullRequestList = new ArrayList<PullRequest>();
        for (MessageQueue mq : mqSet) {
            if (!this.processQueueTable.containsKey(mq)) {
                if (isOrder && !this.lock(mq)) {
                    log.warn("doRebalance, {}, add a new mq failed, {}, because lock failed", consumerGroup, mq);
                    continue;
                }
                this.removeDirtyOffset(mq);

                ProcessQueue pq = new ProcessQueue();
                //获取该MessageQueue对象的下一个进度消费值offset，随后填充至接下来要创建的pullRequest对象属性中
                long nextOffset = this.computePullFromWhere(mq);
                if (nextOffset >= 0) {
                    //为过滤后的消息队列集合（mqSet）中的每个MessageQueue创建一个ProcessQueue对象并存入RebalanceImpl的processQueueTable队列中
                    ProcessQueue pre = this.processQueueTable.putIfAbsent(mq, pq);
                    if (pre != null) {
                        log.info("doRebalance, {}, mq already exists, {}", consumerGroup, mq);
                    } else {
                        log.info("doRebalance, {}, add a new mq, {}", consumerGroup, mq);
                        //并创建拉取请求对象—pullRequest添加到拉取列表—pullRequestList中
                        PullRequest pullRequest = new PullRequest();
                        pullRequest.setConsumerGroup(consumerGroup);
                        pullRequest.setNextOffset(nextOffset);
                        pullRequest.setMessageQueue(mq);
                        pullRequest.setProcessQueue(pq);
                        pullRequestList.add(pullRequest);
                        changed = true;
                    }
                } else {
                    log.warn("doRebalance, {}, add new mq failed, {}", consumerGroup, mq);
                }
            }
        }

        //将Pull消息的请求对象PullRequest依次放入PullMessageService服务线程的阻塞队列pullRequestQueue中，待该服务线程取出后向Broker端发起Pull消息的请求。
        // 其中，可以重点对比下，RebalancePushImpl和RebalancePullImpl两个实现类的dispatchPullRequest()方法不同，RebalancePullImpl类里面的该方法为空
        this.dispatchPullRequest(pullRequestList);

        //消息消费队列在同一消费组不同消费者之间的负载均衡，其核心设计理念是
        // 在一个消息消费队列在同一时间只允许被同一消费组内的一个消费者消费，一个消息消费者能同时消费多个消息队列。
        return changed;


```



### 5. 事务消息

RocketMQ采用了2PC的思想来实现了提交事务消息，同时增加一个补偿逻辑来处理二阶段超时或者失败的消息



### 6. 消息查询

RocketMQ支持按照下面两种维度（“按照Message Id查询消息”、“按照Message Key查询消息”）进行消息查询。

#### 6.1   按照MessageId查询消息

RocketMQ中的MessageId的长度总共有16字节，其中包含了消息存储主机地址（IP地址和端口），消息Commit Log offset。

查找：Client端从MessageId中解析出Broker的地址（IP地址和端口）和Commit Log的偏移地址后封装成一个RPC请求后通过Remoting通信层发送（业务请求码：VIEW_MESSAGE_BY_ID）。Broker端走的是QueryMessageProcessor，读取消息的过程用其中的 commitLog offset 和 size 去 commitLog 中找到真正的记录并解析成一个完整的消息返回。

#### 6.2  按照Message Key查询消息

RocketMQ的索引文件逻辑结构，类似JDK中HashMap的实现。索引文件的具体结构如下：

![](accessory/key查询.png)

IndexFile索引文件为用户提供通过“按照Message Key查询消息”的消息索引查询服务，IndexFile文件的存储位置是：$HOME\store\index\${fileName}，文件名fileName是以创建时的时间戳命名的，文件大小是固定的，等于40+500 W\*4+2000 W\*20= 420000040个字节大小。如果消息的properties中设置了UNIQ_KEY这个属性，就用 topic + “#” + UNIQ_KEY的value作为 key 来做写入操作。如果消息设置了KEYS属性（多个KEY以空格分隔），也会用 topic + “#” + KEY 来做索引。

其中的**索引数据**包含了Key Hash/CommitLog Offset/Timestamp/NextIndex offset 这四个字段，一**共20 Byte**。NextIndex offset 即前面读出来的 slotValue，如果有 hash冲突，就可以用这个字段将所有冲突的索引用链表的方式串起来了。Timestamp记录的是消息storeTimestamp之间的差，并不是一个绝对的时间。整个Index File的结构如图，40 Byte 的Header用于保存一些总的统计信息，4\*500 W的 Slot Table并不保存真正的索引数据，而是保存每个槽位对应的单向链表的头（4*500 W中的4表示 4 byte）。20\*2000 W 是真正的**索引数据**，即一个 Index File 可以保存 2000 W个索引。

查找：通过Broker端的QueryMessageProcessor业务处理器来查询，读取消息的过程就是用topic和key找到IndexFile索引文件中的一条记录，根据其中的commitLog offset从CommitLog文件中读取消息的实体内容。

## [源码](https://github.com/apache/rocketmq.git)