# Spring Cloud Alibaba RocketMQ Binder示例

## 初始化应用

通过[Spring Initializr](https://start.spring.io/)来初始化Spring Cloud Stream应用。

![](images/spring-initializr-rocketmq.jpg)

应用的依赖主要是两部分，一个是Spring Cloud Stream，另外一个是具体的MQ产品Apache RocketMQ


其中，Spring Cloud Stream可以在Spring Initializr搜素添加，而Apache RocketMQ则需要手动添加。


点击“Generate Project”来生成应用原型。

应用的pom.xml文件如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>3.0.2</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.waylau.spring.cloud.stream.binder.rocketmq</groupId>
	<artifactId>demo</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>spring-cloud-stream-binder-rocketmq-demo</name>
	<description>Demo project for Spring Cloud Alibaba RocketMQ Binder</description>
	<properties>
		<java.version>17</java.version>
		<spring-cloud.version>2022.0.1</spring-cloud.version>
		<spring-cloud-starter-stream-rocketmq.version>2022.0.0.0-RC1</spring-cloud-starter-stream-rocketmq.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-stream</artifactId>
		</dependency>

		<!--start: 添加binder-->
		<dependency>
			<groupId>com.alibaba.cloud</groupId>
			<artifactId>spring-cloud-starter-stream-rocketmq</artifactId>
			<version>${spring-cloud-starter-stream-rocketmq.version}</version>
		</dependency>
		<!--end: 添加binder-->

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-stream-test-binder</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.springframework.cloud</groupId>
				<artifactId>spring-cloud-dependencies</artifactId>
				<version>${spring-cloud.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
</project>
```


## 添加消息处理器


在DemoApplication中添加如下消息处理器：


```java
package com.waylau.spring.cloud.stream.binder.rocketmq.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

/**
 * 应用入口
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @since 2023-02-13
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

	/**
	 * 消息处理器
	 * @return
	 */
    @Bean
    public Consumer<Person> log() {
        return person -> {
            System.out.println("Received: " + person);
        };
    }

    public static class Person {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}
```


如上述代码所示：

* 该示例是使用函数式编程模型，将单个消息处理程序定义为Consumer。
* 自动将传入的消息负载转换为Person类型。


## 启动应用


先启动RocketMQ服务器后。

而后启动应用，控制台输出如下内容，则证明启动成功：

```
2023-02-13T15:19:20.885+08:00  INFO 29768 --- [           main] c.w.s.c.s.b.r.demo.DemoApplication       : Starting DemoApplication using Java 19.0.2 with PID 29768 (D:\workspace\github\spring-cloud-stream-tutorial\samples\spring-cloud-stream-binder-rocketmq-demo\target\classes started by wayla in D:\workspace\github\spring-cloud-stream-tutorial\samples\spring-cloud-stream-binder-rocketmq-demo)
2023-02-13T15:19:20.887+08:00  INFO 29768 --- [           main] c.w.s.c.s.b.r.demo.DemoApplication       : No active profile set, falling back to 1 default profile: "default"
2023-02-13T15:19:21.297+08:00  INFO 29768 --- [           main] faultConfiguringBeanFactoryPostProcessor : No bean named 'errorChannel' has been explicitly defined. Therefore, a default PublishSubscribeChannel will be created.
2023-02-13T15:19:21.312+08:00  INFO 29768 --- [           main] faultConfiguringBeanFactoryPostProcessor : No bean named 'integrationHeaderChannelRegistry' has been explicitly defined. Therefore, a default DefaultHeaderChannelRegistry will be created.
2023-02-13T15:19:21.943+08:00  INFO 29768 --- [           main] o.s.c.s.m.DirectWithAttributesChannel    : Channel 'application.log-in-0' has 1 subscriber(s).
2023-02-13T15:19:22.018+08:00  INFO 29768 --- [           main] o.s.i.endpoint.EventDrivenConsumer       : Adding {logging-channel-adapter:_org.springframework.integration.errorLogger} as a subscriber to the 'errorChannel' channel
2023-02-13T15:19:22.018+08:00  INFO 29768 --- [           main] o.s.i.channel.PublishSubscribeChannel    : Channel 'application.errorChannel' has 1 subscriber(s).
2023-02-13T15:19:22.019+08:00  INFO 29768 --- [           main] o.s.i.endpoint.EventDrivenConsumer       : started bean '_org.springframework.integration.errorLogger'
2023-02-13T15:19:22.019+08:00  INFO 29768 --- [           main] o.s.c.s.binder.DefaultBinderFactory      : Creating binder: rocketmq
2023-02-13T15:19:22.020+08:00  INFO 29768 --- [           main] o.s.c.s.binder.DefaultBinderFactory      : Constructing binder child context for rocketmq
2023-02-13T15:19:22.065+08:00  INFO 29768 --- [           main] o.s.c.s.binder.DefaultBinderFactory      : Caching the binder: rocketmq
2023-02-13T15:19:22.133+08:00  INFO 29768 --- [           main] o.s.c.stream.binder.BinderErrorChannel   : Channel '780570776.log-in-0.errors' has 1 subscriber(s).
2023-02-13T15:19:22.407+08:00  INFO 29768 --- [           main] .s.b.r.i.i.RocketMQInboundChannelAdapter : started com.alibaba.cloud.stream.binder.rocketmq.integration.inbound.RocketMQInboundChannelAdapter@65f40689
2023-02-13T15:19:22.412+08:00  INFO 29768 --- [           main] c.w.s.c.s.b.r.demo.DemoApplication       : Started DemoApplication in 1.846 seconds (process running for 2.141)
```


上述应用，对应用RocketMQ信息如下：

* Topic：`log-in-0`和`%RETRY%anonymous_log-in-0`
* groupName：anonymous_log-in-0
* consumerGroup：anonymous_log-in-0


## 发送消息与接收


RocketMQ Dashboard是一个RocketMQ的管理界面，可以获得客户端和应用程序的事件、性能和系统信息的各种图表和统计信息。也可以作为测试工具，用于消息的发送。


![](images/rocketmq-topic.jpg)




在RocketMQ Dashboard界面发送以下JSON消息到Topic “%RETRY%anonymous_log-in-0”。


```json
{"name":"Sam Spade"}
```

![](images/rocketmq-send-msg.jpg)


此时应用就能收到这条消息。

```
Received: Sam Spade
```


## 源码

本节示例见`spring-cloud-stream-binder-rocketmq-demo`。