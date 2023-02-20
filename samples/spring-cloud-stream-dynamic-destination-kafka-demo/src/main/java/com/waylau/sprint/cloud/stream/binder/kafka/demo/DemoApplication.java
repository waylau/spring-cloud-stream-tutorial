package com.waylau.sprint.cloud.stream.binder.kafka.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

// 导入StreamBridge
import org.springframework.cloud.stream.function.StreamBridge;

/**
 * 应用入口
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @since 2023-02-20
 */
@SpringBootApplication
public class DemoApplication {
    @Autowired
    private StreamBridge streamBridge;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    /**
     * 消息处理器
     *
     * @return
     */
    @Bean
    public Consumer<Person> log() {
        return person -> {
            System.out.println("Received: " + person);

            String personName = person.getName();

            // 模拟业务场景，比如包含Sam的消息去到指定的Destination；否则发到另外的Destination
            if (personName.contains("Sam")) {
                // 通过StreamBridge发送到指定的Destination
                streamBridge.send("sam-out-0", person);
            } else {
                streamBridge.send("notSam-out-0", person);
            }

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