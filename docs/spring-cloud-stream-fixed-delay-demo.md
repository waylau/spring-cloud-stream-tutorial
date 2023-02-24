# 延迟消息示例

延迟消息（Delay Message）是RabbitMQ、RocketMQ里面的概念。虽然Kafka原生不支持延迟消息，但Spring Cloud Stream通过了定时轮询的方式，提供了延迟消息的功能。






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
	<artifactId>spring-cloud-stream-fixed-delay-demo</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>spring-cloud-stream-fixed-delay-demo</name>
	<description>Fixed Delay demo for Spring Cloud Stream</description>
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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

import java.time.LocalTime;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 批量消费与生产
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @since 2023-02-22
 */
@SpringBootApplication
public class DemoApplication {

    @Autowired
    private StreamBridge bridge;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    private int id = 0;

    /**
     * 发送消息
     */
    @Bean
    public Supplier<Person> supplier() {
        return () -> {
            return new Person("Sam Spade" + (id++));
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
            System.out.println("Received: " + person + ", " + LocalTime.now());
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

spring.cloud.stream.bindings.supplier-out-0.group=logGroup
spring.cloud.stream.bindings.supplier-out-0.destination=logDestination

# 延迟发送消息，3秒
spring.cloud.stream.bindings.supplier-out-0.producer.poller.fixed-delay=3000
```

其中，fixed-delay就是作用在supplier()这个函数上，实现每间隔3秒就发送一次消息。


## 测试

应用启动，可以看到控制台输出如下：


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


可以看到，log()接收到了消息。

```
Received: Sam Spade0, 10:07:43.103968900
Received: Sam Spade1, 10:07:44.228530800
Received: Sam Spade2, 10:07:47.245681600
Received: Sam Spade3, 10:07:50.267271900
Received: Sam Spade4, 10:07:53.257599700
Received: Sam Spade5, 10:07:56.270057900
Received: Sam Spade6, 10:07:59.286673800
Received: Sam Spade7, 10:08:02.288192400
```


除了第1条和第2条间隔上面有点偏差之外，其他的消息都是按照3秒为间隔发送的。



## 源码

本节示例见`spring-cloud-stream-fixed-delay-demo`。