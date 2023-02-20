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
