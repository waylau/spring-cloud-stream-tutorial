# 多Binder（Kafka、RabbitMQ、RocketMQ、ActiveMQ）示例


Spring Cloud Stream支持多个Binder。比如本例将演示Kafka、RabbitMQ、RocketMQ、ActiveMQ这四个Binder并存的场景。


## 初始化应用


初始化应用，pom.xml中添加Kafka、RabbitMQ、RocketMQ、ActiveMQ这四个Binder：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<!--spring-boot必须是2.x-->
		<version>2.7.8</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.waylau.spring.cloud.stream.binder.jms</groupId>
	<artifactId>spring-cloud-stream-muti-binder-kafka-rabbitmq-rocketmq-activemq-demo</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>spring-cloud-stream-muti-binder-kafka-rabbitmq-rocketmq-activemq-demo</name>
	<description>Muti binder demo for Spring Cloud Stream</description>
	<properties>
		<java.version>17</java.version>
		<!--spring-cloud必须是2021.x-->
		<spring-cloud.version>2021.0.5</spring-cloud.version>
		<spring-cloud-stream-binder-jms.version>1.0.0.RELEASE</spring-cloud-stream-binder-jms.version>
		<spring-cloud-starter-stream-rocketmq.version>2022.0.0.0-RC1</spring-cloud-starter-stream-rocketmq.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-activemq</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-stream</artifactId>
		</dependency>

		<!--start: 添加binder-->
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-stream-binder-kafka</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-stream-binder-rabbit</artifactId>
		</dependency>
		<dependency>
			<groupId>com.alibaba.cloud</groupId>
			<artifactId>spring-cloud-starter-stream-rocketmq</artifactId>
			<version>${spring-cloud-starter-stream-rocketmq.version}</version>
		</dependency>
		<dependency>
			<groupId>com.boutouil</groupId>
			<artifactId>spring-cloud-stream-binder-jms</artifactId>
			<version>${spring-cloud-stream-binder-jms.version}</version>
		</dependency>
		<!--end: 添加binder-->

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-stream</artifactId>
			<scope>test</scope>
			<classifier>test-binder</classifier>
			<type>test-jar</type>
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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

/**
 * 应用入口
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @since 2023-02-20
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

## 添加应用配置


此时启动应用，会报以下错误：

```
org.springframework.context.ApplicationContextException: Failed to start bean 'inputBindingLifecycle'; nested exception is java.lang.IllegalStateException: A default binder has been requested, but there is more than one binder available for 'org.springframework.cloud.stream.messaging.DirectWithAttributesChannel' : jms,rocketmq,kafka,rabbit, and no default binder has been set.
	at org.springframework.context.support.DefaultLifecycleProcessor.doStart(DefaultLifecycleProcessor.java:181) ~[spring-context-5.3.25.jar:5.3.25]
	at org.springframework.context.support.DefaultLifecycleProcessor.access$200(DefaultLifecycleProcessor.java:54) ~[spring-context-5.3.25.jar:5.3.25]
	at org.springframework.context.support.DefaultLifecycleProcessor$LifecycleGroup.start(DefaultLifecycleProcessor.java:356) ~[spring-context-5.3.25.jar:5.3.25]
	at java.base/java.lang.Iterable.forEach(Iterable.java:75) ~[na:na]
	at org.springframework.context.support.DefaultLifecycleProcessor.startBeans(DefaultLifecycleProcessor.java:155) ~[spring-context-5.3.25.jar:5.3.25]
	at org.springframework.context.support.DefaultLifecycleProcessor.onRefresh(DefaultLifecycleProcessor.java:123) ~[spring-context-5.3.25.jar:5.3.25]
	at org.springframework.context.support.AbstractApplicationContext.finishRefresh(AbstractApplicationContext.java:935) ~[spring-context-5.3.25.jar:5.3.25]
	at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:586) ~[spring-context-5.3.25.jar:5.3.25]
	at org.springframework.boot.SpringApplication.refresh(SpringApplication.java:731) ~[spring-boot-2.7.8.jar:2.7.8]
	at org.springframework.boot.SpringApplication.refreshContext(SpringApplication.java:408) ~[spring-boot-2.7.8.jar:2.7.8]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:307) ~[spring-boot-2.7.8.jar:2.7.8]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1303) ~[spring-boot-2.7.8.jar:2.7.8]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1292) ~[spring-boot-2.7.8.jar:2.7.8]
	at com.waylau.spring.cloud.stream.binder.jms.demo.DemoApplication.main(DemoApplication.java:19) ~[classes/:na]
Caused by: java.lang.IllegalStateException: A default binder has been requested, but there is more than one binder available for 'org.springframework.cloud.stream.messaging.DirectWithAttributesChannel' : jms,rocketmq,kafka,rabbit, and no default binder has been set.
	at org.springframework.cloud.stream.binder.DefaultBinderFactory.doGetBinder(DefaultBinderFactory.java:212) ~[spring-cloud-stream-3.2.6.jar:3.2.6]
	at org.springframework.cloud.stream.binder.DefaultBinderFactory.getBinder(DefaultBinderFactory.java:153) ~[spring-cloud-stream-3.2.6.jar:3.2.6]
	at org.springframework.cloud.stream.binding.BindingService.getBinder(BindingService.java:402) ~[spring-cloud-stream-3.2.6.jar:3.2.6]
	at org.springframework.cloud.stream.binding.BindingService.bindConsumer(BindingService.java:106) ~[spring-cloud-stream-3.2.6.jar:3.2.6]
	at org.springframework.cloud.stream.binding.AbstractBindableProxyFactory.createAndBindInputs(AbstractBindableProxyFactory.java:118) ~[spring-cloud-stream-3.2.6.jar:3.2.6]
	at org.springframework.cloud.stream.binding.InputBindingLifecycle.doStartWithBindable(InputBindingLifecycle.java:58) ~[spring-cloud-stream-3.2.6.jar:3.2.6]
	at java.base/java.util.LinkedHashMap$LinkedValues.forEach(LinkedHashMap.java:655) ~[na:na]
	at org.springframework.cloud.stream.binding.AbstractBindingLifecycle.start(AbstractBindingLifecycle.java:57) ~[spring-cloud-stream-3.2.6.jar:3.2.6]
	at org.springframework.cloud.stream.binding.InputBindingLifecycle.start(InputBindingLifecycle.java:34) ~[spring-cloud-stream-3.2.6.jar:3.2.6]
	at org.springframework.context.support.DefaultLifecycleProcessor.doStart(DefaultLifecycleProcessor.java:178) ~[spring-context-5.3.25.jar:5.3.25]
	... 13 common frames omitted
```


这个错误提示是说“存在多个Binder，需要指定默认的Binder”。以下示例指定了kafka作为全局全局默认binder：

```
# 全局默认binder，支持配置中心切换配置。可选值为：kafka、rabbit、rocketmq、jms
spring.cloud.stream.defaultBinder=jms
```

如果是启用ActiveMQ binder，还需要有以下配置：

```
####### start: ActiveMQ 特有配置 #######
# 是否使用内嵌ActiveMQ。正式项目选false
spring.activemq.in-memory=false

# 必须配置destination，值是以“queue://”或“topic://”开头
spring.cloud.stream.bindings.log-in-0.destination=queue://ticks
spring.cloud.stream.bindings.log-in-0.dlq.destination=queue://ticks
####### end:  ActiveMQ 特有配置 #######
```




## 发送消息与接收

用法与“Spring Cloud Stream Kafka binder示例”一致，因此不再赘述。

## 源码

本节示例见`spring-cloud-stream-muti-binder-kafka-rabbitmq-rocketmq-activemq-demo`。


