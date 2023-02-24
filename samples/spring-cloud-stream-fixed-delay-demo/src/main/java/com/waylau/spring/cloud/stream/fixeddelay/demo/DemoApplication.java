package com.waylau.spring.cloud.stream.fixeddelay.demo;

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