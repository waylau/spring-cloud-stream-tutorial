# Binding可视化与控制


Spring Cloud Stream通过Actuator端点以及编程方式支持Binder的可视化和控制。


## 编程方式

从3.1版开始，公开了org.springframework.cloud.stream.binding.BindingsLifecycleController，它注册为bean并且注入时，就可以用来控制各个Binding的生命周期


例如，查看一个测试用例中的片段。如您所见，我们从Spring应用程序上下文中检索BindingsLifecycleController，并执行各个方法来控制Binding “echo-in-0”的生命周期。。


## Actuator


由于Actuator和web是可选的，您必须首先添加一个web依赖项，然后手动添加Actuator依赖项。以下示例显示了如何为Web框架添加依赖项：


```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

以下是添加 WebFlux 依赖：


```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

以下是添加 Actuator 依赖：


```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```



还必须通过设置以下属性来启用binding Actuator 端点：

```
--management.endpoints.web.exposure.include=bindings.
```

上述条件都满足的情况下，启动应用，可以看到控制台输出以下日志：

```
: Mapped "{[/actuator/bindings/{name}],methods=[POST]. . .
: Mapped "{[/actuator/bindings],methods=[GET]. . .
: Mapped "{[/actuator/bindings/{name}],methods=[GET]. . .
```



要可视化当前binding，请访问以下URL：`http://<host>:<port>/actuator/bindings`

或者，要查看单个binding，请访问与以下内容类似的URL之一：`http://<host>:<port>/actuator/bindings/<bindingName>`


还可以停止、启动、暂停和恢复单个binding，方法是在提供状态参数为JSON的同时发送POST请求到同一URL，如下例所示：


```xml
curl -d '{"state":"STOPPED"}' -H "Content-Type: application/json" -X POST http://<host>:<port>/actuator/bindings/myBindingName
curl -d '{"state":"STARTED"}' -H "Content-Type: application/json" -X POST http://<host>:<port>/actuator/bindings/myBindingName
curl -d '{"state":"PAUSED"}' -H "Content-Type: application/json" -X POST http://<host>:<port>/actuator/bindings/myBindingName
curl -d '{"state":"RESUMED"}' -H "Content-Type: application/json" -X POST http://<host>:<port>/actuator/bindings/myBindingName
```


PAUSED（暂停）和RESUMED（恢复）仅在相应的Binder及其底层技术支持时工作。否则，您会在日志中看到警告消息。目前，只有Kafka binder支持PAUSED和RESUMED状态。



