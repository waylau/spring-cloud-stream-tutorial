# Spring Cloud Stream RabbitMQ binder示例

## 初始化应用

通过[Spring Initializr](https://start.spring.io/)来初始化Spring Cloud Stream应用。

![](images/spring-initializr-rabbitmq.jpg)

应用的依赖主要是两部分，一个是Spring Cloud Stream，另外一个是具体的MQ产品：

* Cloud Stream SPRING CLOUD MESSAGING
* Spring for RabbitMQ


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
	<groupId>com.waylau.spring.cloud.stream.binder.rabbitmq</groupId>
	<artifactId>spring-cloud-stream-binder-rabbit-demo</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>spring-cloud-stream-binder-rabbit-demo</name>
	<description>Demo project for Spring Cloud Stream RabbitMQ binder</description>
	<properties>
		<java.version>17</java.version>
		<spring-cloud.version>2022.0.1</spring-cloud.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-amqp</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-stream</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-stream-binder-rabbit</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.amqp</groupId>
			<artifactId>spring-rabbit-test</artifactId>
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
package com.waylau.spring.cloud.stream.binder.rabbitmq.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

/**
 * 应用入口
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @since 2023-02-09
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

启动应用控制台输出如下内容，则证明启动成功：

```
2023-02-09T19:27:23.859+08:00  INFO 20844 --- [           main] c.w.s.c.s.b.r.demo.DemoApplication       : Starting DemoApplication using Java 19.0.2 with PID 20844 (D:\workspace\github\spring-cloud-stream-tutorial\samples\spring-cloud-stream-binder-rabbitmq-demo\target\classes started by wayla in D:\workspace\github\spring-cloud-stream-tutorial\samples\spring-cloud-stream-binder-rabbitmq-demo)
2023-02-09T19:27:23.862+08:00  INFO 20844 --- [           main] c.w.s.c.s.b.r.demo.DemoApplication       : No active profile set, falling back to 1 default profile: "default"
2023-02-09T19:27:24.321+08:00  INFO 20844 --- [           main] faultConfiguringBeanFactoryPostProcessor : No bean named 'errorChannel' has been explicitly defined. Therefore, a default PublishSubscribeChannel will be created.
2023-02-09T19:27:24.345+08:00  INFO 20844 --- [           main] faultConfiguringBeanFactoryPostProcessor : No bean named 'integrationHeaderChannelRegistry' has been explicitly defined. Therefore, a default DefaultHeaderChannelRegistry will be created.
2023-02-09T19:27:25.268+08:00  INFO 20844 --- [           main] o.s.c.s.m.DirectWithAttributesChannel    : Channel 'application.log-in-0' has 1 subscriber(s).
2023-02-09T19:27:25.312+08:00  INFO 20844 --- [           main] o.s.i.endpoint.EventDrivenConsumer       : Adding {logging-channel-adapter:_org.springframework.integration.errorLogger} as a subscriber to the 'errorChannel' channel
2023-02-09T19:27:25.312+08:00  INFO 20844 --- [           main] o.s.i.channel.PublishSubscribeChannel    : Channel 'application.errorChannel' has 1 subscriber(s).
2023-02-09T19:27:25.313+08:00  INFO 20844 --- [           main] o.s.i.endpoint.EventDrivenConsumer       : started bean '_org.springframework.integration.errorLogger'
2023-02-09T19:27:25.413+08:00  INFO 20844 --- [           main] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port 8080
2023-02-09T19:27:25.415+08:00  INFO 20844 --- [           main] o.s.c.s.binder.DefaultBinderFactory      : Creating binder: rabbit
2023-02-09T19:27:25.415+08:00  INFO 20844 --- [           main] o.s.c.s.binder.DefaultBinderFactory      : Constructing binder child context for rabbit
2023-02-09T19:27:25.479+08:00  INFO 20844 --- [           main] o.s.c.s.binder.DefaultBinderFactory      : Caching the binder: rabbit
2023-02-09T19:27:25.500+08:00  INFO 20844 --- [           main] c.s.b.r.p.RabbitExchangeQueueProvisioner : declaring queue for inbound: log-in-0.anonymous.4XL0MhelTv2W1SN4ysmswg, bound to: log-in-0
2023-02-09T19:27:25.505+08:00  INFO 20844 --- [           main] o.s.a.r.c.CachingConnectionFactory       : Attempting to connect to: [localhost:5672]
```

如果此时RabbitMQ服务器没有启动，那么应用就会一直尝试去连接RabbitMQ服务器。

## 启动RabbitMQ服务器

启动RabbitMQ服务器后，应用就会打印出如下信息，证明已经获取到了连接：


```
2023-02-09T21:45:00.204+08:00  INFO 3284 --- [           main] o.s.a.r.c.CachingConnectionFactory       : Created new connection: rabbitConnectionFactory#64942607:0/SimpleConnection@d87d449 [delegate=amqp://guest@127.0.0.1:5672/, localPort=13959]
2023-02-09T21:45:00.281+08:00  INFO 3284 --- [           main] o.s.c.stream.binder.BinderErrorChannel   : Channel 'rabbit-1077109993.log-in-0.errors' has 1 subscriber(s).
2023-02-09T21:45:00.294+08:00  INFO 3284 --- [           main] o.s.i.a.i.AmqpInboundChannelAdapter      : started bean 'inbound.log-in-0.anonymous.18LbYCclSvOQw6ct_8PArQ'
2023-02-09T21:45:00.300+08:00  INFO 3284 --- [           main] c.w.s.c.s.b.r.demo.DemoApplication       : Started DemoApplication in 1.916 seconds (process running for 2.146)
```


其中，“log-in-0.anonymous.18LbYCclSvOQw6ct_8PArQ”就是group name，相应会有一条同名的队列。

## 发送消息与接收

在RabbitMQ管理界面发送以下JSON消息到队列“log-in-0.anonymous.18LbYCclSvOQw6ct_8PArQ”。


```json
{"name":"Sam Spade"}
```

![](images/rabbitmq-send-msg.jpg)


此时应用就能收到这条消息。

```
Received: Sam Spade
```


## 源码

本节示例见`spring-cloud-stream-binder-rabbitmq-demo`。