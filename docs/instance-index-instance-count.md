# 实例索引和实例计数


在扩展Spring Cloud Stream应用程序时，每个实例都可以接收关于同一应用程序存在多少其他实例以及其自己的实例索引是什么的信息。Spring Clound Stream通过spring.cloud.stream.instanceCount 和 spring.cloud.stream.instanceIndex属性实现这一点。例如，如果HDFS接收应用程序有三个实例，则所有三个实例的spring.cloud.stream.instanceCount设置为3，而各个应用程序的spring.cloud.stream.instanceIndex分别设置为0、1和2。

当通过 Spring Cloud Data Flow 部署 Spring Cloud Stream 应用程序时，这些属性会自动配置；当Spring Cloud Stream应用程序独立启动时，必须正确设置这些属性。默认情况下，Spring Cloud Stream为1，spring.cloud.stream.instanceIndex为0。

在扩展的场景中，这两个属性的正确配置对于解决一般的分区行为（见下文）非常重要，并且这两个属性始终是某些binder程序（例如，Kafka binder）所必需的，以确保数据在多个使用者实例中正确拆分。