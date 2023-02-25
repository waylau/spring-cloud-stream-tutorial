# 分区示例

分区是有状态处理中的一个关键概念，它对于确保所有相关数据一起处理至关重要（无论是出于性能还是一致性原因）。例如，在时间窗平均计算示例中，来自任何给定传感器的所有测量值都由同一应用实例处理是很重要的。


Spring Cloud Stream为以统一的方式实现分区处理用例提供了一个通用的抽象。因此，无论 broker 本身是否自然分区（例如Kafka是有分区的，而RabbitMQ是没有分区的），都可以使用分区。



## 初始化应用



初始化应用pom.xml中添加Kafka Binder。为了方便测试，我把Kafka、RabbitMQ、RocketMQ、JMS这四个Binder都加入了：

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
	<artifactId>spring-cloud-stream-partitioning-demo</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>spring-cloud-stream-partitioning-demo</name>
	<description>Partitioning demo for Spring Cloud Stream</description>
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
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 分区
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @since 2023-02-24
 */
@SpringBootApplication
public class DemoApplication {
    // 自定义分区头标
    private static final String PARTITION_KEY = "partitionKey";

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    private int id = 0;

    /**
     * 发送消息
     */
    @Bean
    public Supplier<Message<Person>> supplier() {
        return () -> {
            int msgId = id;
            Person person = new Person("Sam Spade " + id);
            id++;

            // 求模运算生成分区key，个位数相同的msgId放到同一个分区
            int key = msgId % 10;
            return MessageBuilder.withPayload(person)
                    .setHeader(PARTITION_KEY, key)
                    .build();
        };
    }

    /**
     * 单条消息处理器
     *
     * @return
     */
    @Bean
    public Consumer<Message<Person>> log() {
        return message -> {
            Person person = message.getPayload();
            MessageHeaders headers = message.getHeaders();

            // 打印出接收到的消息
            System.out.println("partition: " + headers.get(PARTITION_KEY) + "; " + person);
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


* supplier()用于在应用启动时发送测试消息。为了便于观察数据，在消息里面加了自增的id。
* 求模运算生成分区key，个位数相同的msgId放到同一个分区。分区标识放置在MessageHeaders里面。
* log()用于处理单条消息。


## 应用配置

应用配置如下：


```
# 默认binder，可选kafka、rabbit、rocketmq、jms。如果是ActiveMQ则配置jms
spring.cloud.stream.defaultBinder=kafka

# 存在多个Supplier/Function/Consumer Bean时，配置function.definition
spring.cloud.function.definition=log;supplier

spring.cloud.stream.bindings.log-in-0.group=logGroup
spring.cloud.stream.bindings.log-in-0.destination=logDestination
spring.cloud.stream.bindings.supplier-out-0.destination=logDestination

# 消费者使用分区及并发数
spring.cloud.stream.bindings.log-in-0.consumer.partitioned=true
spring.cloud.stream.default.consumer.concurrency=10

# 分区规则及分区数
spring.cloud.stream.bindings.supplier-out-0.producer.partitionKeyExpression=headers['partitionKey']
spring.cloud.stream.bindings.supplier-out-0.producer.partitionCount=10
spring.cloud.stream.bindings.supplier-out-0.producer.required-groups=logGroup
```


其中，partitionKeyExpression是分区表达式，分区规则取自MessageHeaders的“partitionKey”标识。



## 测试


应用启动，可以看到，log()接收到了消息控制台输出如下：

```
partition: 0; Sam Spade 0
partition: 1; Sam Spade 1
partition: 2; Sam Spade 2
partition: 3; Sam Spade 3
partition: 4; Sam Spade 4
partition: 5; Sam Spade 5
partition: 6; Sam Spade 6
partition: 7; Sam Spade 7
partition: 8; Sam Spade 8
partition: 9; Sam Spade 9
partition: 0; Sam Spade 10
partition: 1; Sam Spade 11
partition: 2; Sam Spade 12
partition: 3; Sam Spade 13
partition: 4; Sam Spade 14
partition: 5; Sam Spade 15
partition: 6; Sam Spade 16
partition: 7; Sam Spade 17
partition: 8; Sam Spade 18
partition: 9; Sam Spade 19
partition: 0; Sam Spade 20
partition: 1; Sam Spade 21
```

可以看到数据是从对应的分区里面取出的。




## 源码

本节示例见`spring-cloud-stream-partitioning-demo`。