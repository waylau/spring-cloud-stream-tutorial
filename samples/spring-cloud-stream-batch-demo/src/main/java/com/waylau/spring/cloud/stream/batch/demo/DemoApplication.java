package com.waylau.spring.cloud.stream.batch.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

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
    private final static List<Person> PERSON_LIST = List.of(
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