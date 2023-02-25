package com.waylau.spring.cloud.stream.fixeddelay.demo;

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