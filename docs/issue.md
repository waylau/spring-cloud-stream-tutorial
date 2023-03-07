# 问题

## RocketMQ 5 Unrecognized VM option


### 问题


RocketMQ 5启动“mqnamesrv”报错：


```
D:\dev\java\rocketmq-all-5.0.0-bin-release\bin>mqnamesrv
Unrecognized VM option 'UseConcMarkSweepGC'
Error: Could not create the Java Virtual Machine.
Error: A fatal exception has occurred. Program will exit.
```


JVM版本为“19.0.2”。

### 解法

方案1：更换JVM。因为[该参数UseConcMarkSweepGC已经在JDK 14版本中废弃](https://openjdk.org/jeps/363)，所以报找不到这个参数。因此可以降低JDK版本到14以下得到解决。


方案2：应用启动参数里面去掉这个参数。修改runserver.cmd文件，删除掉以下参数：

```
-XX:-UseConcMarkSweepGC 
-XX:+UseCMSCompactAtFullCollection
-XX:CMSInitiatingOccupancyFraction=70
-XX:+CMSParallelRemarkEnabled
-XX:+CMSClassUnloadingEnabled
-XX:-UseParNewGC
-XX:+PrintGCDateStamps
```


执行mqbroker命令也可能遇到类似的问题，按上述方法修改runbroker.cmd里面的VM参数即可，删除掉以下参数：

```
-XX:+PrintGCDateStamps
-XX:+PrintGCApplicationStoppedTime
-XX:+PrintAdaptiveSizePolicy
-XX:+UseGCLogFileRotation 
-XX:NumberOfGCLogFiles=5
-XX:GCLogFileSize=30m
-XX:-UseBiasedLocking
```


## RocketMQ 5 module java.base does not export sun.nio.ch to unnamed module


### 问题

执行mqbroker命令可能遇到下面的问题：

```
java.lang.IllegalAccessError: class org.apache.rocketmq.common.UtilAll (in unnamed module @0x5403f35f) cannot access class sun.nio.ch.DirectBuffer (in module java.base) because module java.base does not export sun.nio.ch to unnamed module @0x5403f35f
        at org.apache.rocketmq.common.UtilAll.viewed(UtilAll.java:720)
        at org.apache.rocketmq.common.UtilAll.cleanBuffer(UtilAll.java:684)
        at org.apache.rocketmq.store.logfile.DefaultMappedFile.cleanup(DefaultMappedFile.java:470)
        at org.apache.rocketmq.store.ReferenceResource.release(ReferenceResource.java:63)
        at org.apache.rocketmq.store.ReferenceResource.shutdown(ReferenceResource.java:47)
        at org.apache.rocketmq.store.logfile.DefaultMappedFile.destroy(DefaultMappedFile.java:481)
        at org.apache.rocketmq.store.index.IndexFile.destroy(IndexFile.java:97)
        at org.apache.rocketmq.store.index.IndexService.load(IndexService.java:72)
        at org.apache.rocketmq.store.DefaultMessageStore.load(DefaultMessageStore.java:287)
        at org.apache.rocketmq.broker.BrokerController.initialize(BrokerController.java:754)
        at org.apache.rocketmq.broker.BrokerStartup.createBrokerController(BrokerStartup.java:224)
        at org.apache.rocketmq.broker.BrokerStartup.main(BrokerStartup.java:58)
```

JVM版本为“19.0.2”。


### 原因

其中最主要的原因就是 Java 模块化之后，有些 JDK 内部的类不能被访问了。


### 解法

方案1：更换JVM，降到JDK 1.8。


方案2：修改runbroker.cmd里面的VM参数即可，增加下面一条参数，以启用访问封装的包：

```
set "JAVA_OPT=%JAVA_OPT% --add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
```

## ActiveMQ Address already in use: bind

### 问题

ActiveMQ启动报错：

```
Invocation of init method failed; nested exception is java.io.IOException: Transport Connector could not be registered in JMX: java.io.IOException: Failed to bind to server socket: amqp://0.0.0.0:5672?maximumConnections=1000&wireFormat.maxFrameSize=104857600 due to: java.net.BindException: Address already in use: bind
```



### 原因

端口5672被其他程序占用了。


### 解法

杀掉该端口的进程。一般是Erlang的“erl.exe”在用这个端口。





## RocketMQ binder com.alibaba.fastjson.JSONException: expect '[', but {

### 问题

RocketMQ使用批量消费启动报错：


```
2023-02-22 17:44:43.783 ERROR 33704 --- [chDestination_1] o.s.integration.handler.LoggingHandler   : org.springframework.messaging.MessageHandlingException: error occurred in message handler [org.springframework.cloud.stream.function.FunctionConfiguration$FunctionToDestinationBinder$1@69a85b60]; nested exception is com.alibaba.fastjson.JSONException: expect '[', but {, pos 1, line 1, column 2{"name":"Way Lau"}, failedMessage=GenericMessage [payload=byte[18], headers={ROCKET_MQ_BORN_TIMESTAMP=1677057922223, ROCKET_MQ_FLAG=0, ROCKET_MQ_MESSAGE_ID=7F00000183F478308DB16FE29CAF000A, ROCKET_MQ_TOPIC=logBatchDestination, ROCKET_MQ_BORN_HOST=10.10.52.63, id=f66f981b-f5ba-5a99-7f6d-680e95fbce08, ROCKET_MQ_SYS_FLAG=0, contentType=application/json, ROCKET_MQ_QUEUE_ID=0, target-protocol=kafka, timestamp=1677059080760}]
	at org.springframework.integration.support.utils.IntegrationUtils.wrapInHandlingExceptionIfNecessary(IntegrationUtils.java:191)
	at org.springframework.integration.handler.AbstractMessageHandler.handleMessage(AbstractMessageHandler.java:65)
	at org.springframework.integration.dispatcher.AbstractDispatcher.tryOptimizedDispatch(AbstractDispatcher.java:115)
	at org.springframework.integration.dispatcher.UnicastingDispatcher.doDispatch(UnicastingDispatcher.java:133)
	at org.springframework.integration.dispatcher.UnicastingDispatcher.dispatch(UnicastingDispatcher.java:106)
	at org.springframework.integration.channel.AbstractSubscribableChannel.doSend(AbstractSubscribableChannel.java:72)
	at org.springframework.integration.channel.AbstractMessageChannel.send(AbstractMessageChannel.java:317)
	at org.springframework.integration.channel.AbstractMessageChannel.send(AbstractMessageChannel.java:272)
	at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:187)
	at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:166)
	at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:47)
	at org.springframework.messaging.core.AbstractMessageSendingTemplate.send(AbstractMessageSendingTemplate.java:109)
	at org.springframework.integration.endpoint.MessageProducerSupport.sendMessage(MessageProducerSupport.java:216)
	at com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.RocketMQInboundChannelAdapter.lambda$consumeMessage$6(RocketMQInboundChannelAdapter.java:163)
	at org.springframework.retry.support.RetryTemplate.doExecute(RetryTemplate.java:329)
	at org.springframework.retry.support.RetryTemplate.execute(RetryTemplate.java:225)
	at com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.RocketMQInboundChannelAdapter.consumeMessage(RocketMQInboundChannelAdapter.java:162)
	at com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.RocketMQInboundChannelAdapter.lambda$onInit$5(RocketMQInboundChannelAdapter.java:124)
	at org.apache.rocketmq.client.impl.consumer.ConsumeMessageConcurrentlyService$ConsumeRequest.run(ConsumeMessageConcurrentlyService.java:402)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:577)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	at java.base/java.lang.Thread.run(Thread.java:1589)
Caused by: com.alibaba.fastjson.JSONException: expect '[', but {, pos 1, line 1, column 2{"name":"Way Lau"}
	at com.alibaba.fastjson.parser.DefaultJSONParser.parseArray(DefaultJSONParser.java:721)
	at com.alibaba.fastjson.serializer.CollectionCodec.deserialze(CollectionCodec.java:126)
	at com.alibaba.fastjson.parser.DefaultJSONParser.parseObject(DefaultJSONParser.java:688)
	at com.alibaba.fastjson.JSON.parseObject(JSON.java:396)
	at com.alibaba.fastjson.JSON.parseObject(JSON.java:461)
	at com.alibaba.fastjson.JSON.parseObject(JSON.java:429)
	at com.alibaba.fastjson.support.spring.messaging.MappingFastJsonMessageConverter.convertFromInternal(MappingFastJsonMessageConverter.java:69)
	at org.springframework.messaging.converter.AbstractMessageConverter.fromMessage(AbstractMessageConverter.java:185)
	at org.springframework.cloud.function.context.config.SmartCompositeMessageConverter.fromMessage(SmartCompositeMessageConverter.java:115)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.convertInputMessageIfNecessary(SimpleFunctionRegistry.java:1328)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.convertInputIfNecessary(SimpleFunctionRegistry.java:1089)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.doApply(SimpleFunctionRegistry.java:734)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.apply(SimpleFunctionRegistry.java:589)
	at org.springframework.cloud.stream.function.PartitionAwareFunctionWrapper.apply(PartitionAwareFunctionWrapper.java:84)
	at org.springframework.cloud.stream.function.FunctionConfiguration$FunctionWrapper.apply(FunctionConfiguration.java:791)
	at org.springframework.cloud.stream.function.FunctionConfiguration$FunctionToDestinationBinder$1.handleMessageInternal(FunctionConfiguration.java:623)
	at org.springframework.integration.handler.AbstractMessageHandler.handleMessage(AbstractMessageHandler.java:56)
	... 22 more
```


更新到最新版的fastjson：

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>2.0.12</version>
</dependency>
```


## RocketMQ binder batch mode com.alibaba.fastjson.JSONException: offset 1, character {, line 1, column 2, fastjson-version 2.0.24

暂不支持批量消费，fastjson解析报错。

：

```
2023-02-23 11:17:42.104 ERROR 16552 --- [chDestination_1] o.s.integration.handler.LoggingHandler   : org.springframework.messaging.MessageHandlingException: error occurred in message handler [org.springframework.cloud.stream.function.FunctionConfiguration$FunctionToDestinationBinder$1@79142e08]; nested exception is com.alibaba.fastjson.JSONException: offset 1, character {, line 1, column 2, fastjson-version 2.0.24 {"name":"Sam Bo"}, failedMessage=GenericMessage [payload=byte[17], headers={ROCKET_MQ_BORN_TIMESTAMP=1677121911796, ROCKET_MQ_FLAG=0, ROCKET_MQ_MESSAGE_ID=7F00000125A078308DB173B177CD0009, ROCKET_MQ_TOPIC=logBatchDestination, ROCKET_MQ_BORN_HOST=10.10.52.63, id=76628487-9d8e-915a-777a-cd23983c551b, ROCKET_MQ_SYS_FLAG=0, contentType=application/json, ROCKET_MQ_QUEUE_ID=0, target-protocol=kafka, timestamp=1677122259081}]
	at org.springframework.integration.support.utils.IntegrationUtils.wrapInHandlingExceptionIfNecessary(IntegrationUtils.java:191)
	at org.springframework.integration.handler.AbstractMessageHandler.handleMessage(AbstractMessageHandler.java:65)
	at org.springframework.integration.dispatcher.AbstractDispatcher.tryOptimizedDispatch(AbstractDispatcher.java:115)
	at org.springframework.integration.dispatcher.UnicastingDispatcher.doDispatch(UnicastingDispatcher.java:133)
	at org.springframework.integration.dispatcher.UnicastingDispatcher.dispatch(UnicastingDispatcher.java:106)
	at org.springframework.integration.channel.AbstractSubscribableChannel.doSend(AbstractSubscribableChannel.java:72)
	at org.springframework.integration.channel.AbstractMessageChannel.send(AbstractMessageChannel.java:317)
	at org.springframework.integration.channel.AbstractMessageChannel.send(AbstractMessageChannel.java:272)
	at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:187)
	at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:166)
	at org.springframework.messaging.core.GenericMessagingTemplate.doSend(GenericMessagingTemplate.java:47)
	at org.springframework.messaging.core.AbstractMessageSendingTemplate.send(AbstractMessageSendingTemplate.java:109)
	at org.springframework.integration.endpoint.MessageProducerSupport.sendMessage(MessageProducerSupport.java:216)
	at com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.RocketMQInboundChannelAdapter.lambda$consumeMessage$6(RocketMQInboundChannelAdapter.java:163)
	at org.springframework.retry.support.RetryTemplate.doExecute(RetryTemplate.java:329)
	at org.springframework.retry.support.RetryTemplate.execute(RetryTemplate.java:225)
	at com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.RocketMQInboundChannelAdapter.consumeMessage(RocketMQInboundChannelAdapter.java:162)
	at com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.RocketMQInboundChannelAdapter.lambda$onInit$5(RocketMQInboundChannelAdapter.java:124)
	at org.apache.rocketmq.client.impl.consumer.ConsumeMessageConcurrentlyService$ConsumeRequest.run(ConsumeMessageConcurrentlyService.java:402)
	at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:577)
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317)
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	at java.base/java.lang.Thread.run(Thread.java:1589)
Caused by: com.alibaba.fastjson.JSONException: offset 1, character {, line 1, column 2, fastjson-version 2.0.24 {"name":"Sam Bo"}
	at com.alibaba.fastjson.JSON.parseObject(JSON.java:776)
	at com.alibaba.fastjson.support.spring.messaging.MappingFastJsonMessageConverter.convertFromInternal(MappingFastJsonMessageConverter.java:68)
	at org.springframework.messaging.converter.AbstractMessageConverter.fromMessage(AbstractMessageConverter.java:185)
	at org.springframework.cloud.function.context.config.SmartCompositeMessageConverter.fromMessage(SmartCompositeMessageConverter.java:115)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.convertInputMessageIfNecessary(SimpleFunctionRegistry.java:1328)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.convertInputIfNecessary(SimpleFunctionRegistry.java:1089)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.doApply(SimpleFunctionRegistry.java:734)
	at org.springframework.cloud.function.context.catalog.SimpleFunctionRegistry$FunctionInvocationWrapper.apply(SimpleFunctionRegistry.java:589)
	at org.springframework.cloud.stream.function.PartitionAwareFunctionWrapper.apply(PartitionAwareFunctionWrapper.java:84)
	at org.springframework.cloud.stream.function.FunctionConfiguration$FunctionWrapper.apply(FunctionConfiguration.java:791)
	at org.springframework.cloud.stream.function.FunctionConfiguration$FunctionToDestinationBinder$1.handleMessageInternal(FunctionConfiguration.java:623)
	at org.springframework.integration.handler.AbstractMessageHandler.handleMessage(AbstractMessageHandler.java:56)
	... 22 more
Caused by: com.alibaba.fastjson2.JSONException: offset 1, character {, line 1, column 2, fastjson-version 2.0.24 {"name":"Sam Bo"}
	at com.alibaba.fastjson2.reader.ObjectReaderImplList.readObject(ObjectReaderImplList.java:482)
	at com.alibaba.fastjson.JSON.parseObject(JSON.java:766)
	... 33 more
```

已经向社区反馈了： <https://github.com/alibaba/spring-cloud-alibaba/issues/3172>




## “java.lang.UnsupportedClassVersionError”问题。

```
java.lang.UnsupportedClassVersionError: com/alibaba/cloud/stream/binder/rocketmq/autoconfigurate/RocketMQBinderAutoConfiguration has been compiled by a more recent version of the Java Runtime (class file version 61.0), this version of the Java Runtime only recognizes class file versions up to 52.0
```


### 原因

查了下JDK版本不兼容的引起的。

目前测试环境是JDK 1.8，而 spring-cloud-starter-stream-rocketmq 这个binder 当时选的是 “2022.0.0.0-RC1”，要求JDK为 JDK 17，而spring-cloud-stream-binder-jms 这个binder 要求是 JDK11。


### 解决思路

spring-cloud-starter-stream-rocketmq 这个binder 可以降为 2021.1 版本以兼容JDK 1.8，但spring-cloud-stream-binder-jms 这个binder目前要求的最低版本就是JDK 11，所以考虑下以下方案。


#### 方法1：环境升级到JDK 11

测试环境先升级到JDK 11。

风险：可能平台的其他代码又存在兼容性的问题。需要项目组花时间再投入测试。



#### 方法2：去掉  spring-cloud-stream-binder-jms 

因为spring-cloud-stream-binder-jms 这个binder目前要求的最低版本就是JDK 11，所以，如果测试环境来不及升级的话，建议是先去掉这个 binder（先不测ActiveMQ）
等到后续测试环境先升级到JDK 11再纳入这个binder。

因为技术预研的年度工作里面本身就规划有“JDK升级”这个工作，可能等这个工作完成了，再继续测试 spring-cloud-stream-binder-jms 这个binder。




## Rocket平台报“No route info of this topic”


我查了下可能有几个原因。

1、Rocket 没有启用自动创建Topic 。需要设置  autoCreateTopicEnable=true
2、防火墙问题。需要开放端口 9876 10911 10912  10909


mqbroker -n localhost:9876 &


9876 是 nameserver的
10911，10912，10909 是broker 的



```
RocketmqRemoting.info:95 -closeChannel: close the connection to remote address[] result: true
```


spring.cloud.stream.rocketmq.bindings.consumer.enable = false
spring.cloud.stream.rocketmq.bindings.producer.enable = false


spring.cloud.stream.rocketmq.bindings.log-in-0.consumer.enable = false
spring.cloud.stream.rocketmq.bindings.log-in-0.producer.enable = false