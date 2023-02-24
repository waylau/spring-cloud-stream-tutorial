# 批量处理示例

Spring Cloud Stream支持动态batch-mode（类似于Kafka 的 batch size）。本例以Kafka binder为例，演示batch-mode的场景。





## 初始化应用


初始化应用，pom.xml中添加Kafka Binder。为了方便测试，我把Kafka、RabbitMQ、RocketMQ、JMS这四个Binder都加入了：

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
	<artifactId>spring-cloud-stream-batch-demo</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>spring-cloud-stream-batch-demo</name>
	<description>Batch demo for Spring Cloud Stream</description>
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * 批量消费与生产
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @since 2023-02-22
 */
@SpringBootApplication
public class DemoApplication {
    private final static List<Person> PERSON_LIST = Arrays.asList(
            new Person("Sam Spade"),
            new Person("Sam Po"),
            new Person("Sam Li"),
            new Person("Sam Bo"),
            new Person("Way Lau"),
            new Person("Fei Po"),
            new Person("Gu Li")
    );

    @Autowired
    private StreamBridge bridge;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public ApplicationRunner runner() {
        return arg -> {
            // 将消息批量转发到其他destination
            bridge.send("logBatchTransmitDestination", PERSON_LIST);

            // 将消息逐个转发到其他destination
            PERSON_LIST.stream().forEach(person -> {
                bridge.send("logBatchDestination", person);
            });
        };

    }

    /**
     * 单条消息处理器
     *
     * @return
     */
    @Bean
    public Consumer<Person> log() {
        return person -> {
            // 打印出接收到的消息
            System.out.println("Received: " + person);
        };
    }

    /**
     * 批量消息处理器
     *
     * @return
     */
    @Bean
    public Consumer<List<Person>> logBatch() {
        return personList -> {
            // 打印出接收到的消息
            System.out.println("Received personList: " + personList);

            // 将消息逐个转发到其他destination
            personList.stream().forEach(person -> {
                bridge.send("logSingleDestination", person);
            });

        };
    }

    public static class Person {
        private String name;

        public Person() {

        }

        public Person(String name) {
            this.name = name;
        }

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

* runner()用于在应用启动时，往logBatchTransmitDestination、logBatchDestination两个Destination里面发送测试消息。
其中，logBatchTransmitDestination接收的是一个`List<Person>`，而 logBatchDestination 接收的是单个Person。
* log()用于处理单条消息。
* logBatch()用于处理批量消息。并且在接收到批量消息后，再逐个将单个消息发送到 logSingleDestination。


## 应用配置

应用配置如下：


```
# rocketmq、jms暂不支持批量消费
spring.cloud.stream.defaultBinder=kafka

# 存在多个Supplier/Function/Consumer Bean时，配置function.definition
spring.cloud.function.definition=log;logBatch

spring.cloud.stream.bindings.log-in-0.destination=logSingleDestination
spring.cloud.stream.bindings.logBatch-in-0.destination=logBatchDestination
spring.cloud.stream.bindings.logBatch-in-0.consumer.batch-mode=true
```

## 测试

应用启动，runner()将会发送如下消息：

```
{"name":"Sam Spade"}
{"name":"Sam Po"}
{"name":"Sam Li"}
{"name":"Sam Bo"}
{"name":"Way Lau"}
{"name":"Fei Po"}
{"name":"Gu Li"}
```


此时，可以看到控制台输出如下：


```
Received personList: [Sam Spade, Sam Po, Sam Li]
Received: Sam Spade
Received: Sam Po
Received: Sam Li
Received personList: [Sam Bo, Way Lau, Fei Po, Gu Li]
Received: Sam Bo
Received: Way Lau
Received: Fei Po
Received: Gu Li
```


可以看到，logBatch()是分成了2批次处理了消息。

## 控制批量

Spring Cloud Stream 并没有设置多少一批的参数，这需要在各个中间件Binder配置里面设置。

比如，在Kafka里面设置：

```
####### start: Kafka 特有配置 #######
# 以下配置搭配批量使用
spring.kafka.consumer.max-poll-records=1
spring.kafka.consumer.fetch.max.wait.ms=5000
spring.kafka.consumer.fetch.min.bytes=5000
####### end:  Kafka 特有配置 #######
```

那么再次启动应用，可以看到控制台输出如下，批量数只有1条了：



```
Received personList: [Sam Spade]
Received personList: [Sam Po]
Received: Sam Spade
Received personList: [Sam Li]
Received: Sam Po
Received personList: [Sam Bo]
Received: Sam Bo
Received personList: [Way Lau]
Received: Sam Po
Received personList: [Fei Po]
Received: Way Lau
Received personList: [Gu Li]
Received: Fei Po
Received: Gu Li
```



在RabbitMQ配置如下：


```
####### start: RabbitMQ 特有配置 #######
# 以下配置搭配批量使用
spring.cloud.stream.rabbit.bindings.input-in-0.consumer.enable-batching=true
spring.cloud.stream.rabbit.bindings.input-in-0.consumer.batch-size=10
spring.cloud.stream.rabbit.bindings.input-in-0.consumer.receive-timeout=200
####### end:  RabbitMQ 特有配置 #######
```




## 源码

本节示例见`spring-cloud-stream-batch-demo`。