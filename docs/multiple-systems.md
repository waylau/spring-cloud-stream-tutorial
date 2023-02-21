# 连接到多个系统

默认情况下，Binder共享应用程序的Spring Boot自动配置，因此在类路径中找到的每个Binder都会创建一个实例。如果应用程序应连接到多个相同类型的代理，则可以指定多个Binder配置，每个配置具有不同的环境设置。

以下示例显示了连接到两个RabbitMQ代理实例的处理器应用程序的典型配置：


```
spring:
  cloud:
    stream:
      bindings:
        input:
          destination: thing1
          binder: rabbit1
        output:
          destination: thing2
          binder: rabbit2
      binders:
        rabbit1:
          type: rabbit
          environment:
            spring:
              rabbitmq:
                host: <host1>
        rabbit2:
          type: rabbit
          environment:
            spring:
              rabbitmq:
                host: <host2>
```


**注**：特定binder 的environment属性也可以用于任何Spring Boot属性，包括这个spring.main.sources，它可以用于为特定Binder添加其他配置，例如覆盖自动配置的bean。


举例：

```
environment:
    spring:
        main:
           sources: com.acme.config.MyCustomBinderConfiguration
```


要激活特定Binder环境的特定配置文件，应使用spring.profiles.active属性：

```
environment:
    spring:
        profiles:
           active: myBinderProfile
```