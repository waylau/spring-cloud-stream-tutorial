# 连接多个应用程序实例


虽然Spring Cloud Stream使单个Spring Boot应用程序能够轻松连接到消息传递系统，但Spring Cloud Stream的典型场景是创建多应用程序管道，其中微服务应用程序相互发送数据。您可以通过关联“相邻”应用程序的输入和输出destination来实现此场景。

假设一个设计调用Time Source应用程序将数据发送到Log Sink应用程序。您可以在两个应用程序中为绑定使用一个名为ticktock的公共destination。


Time Source（具有名为output的binding ）将设置以下属性：

```
spring.cloud.stream.bindings.output.destination=ticktock
```

Log Sink（具有名为input的binding ）将设置以下属性：


```
spring.cloud.stream.bindings.input.destination=ticktock
```