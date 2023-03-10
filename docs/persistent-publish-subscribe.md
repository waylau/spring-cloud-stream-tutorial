# 持久发布订阅


应用程序之间的通信遵循发布-订阅模型，通过共享topic广播数据。这可以在下图中看到，该图显示了一组交互Spring Cloud Stream应用程序的典型部署。

![Spring Cloud Stream 发布订阅](../images/SCSt-sensors.png)

传感器向HTTP端点报告的数据被发送到一个名为“raw-sensor-data”的公共destination。从destination开始，它由计算时间窗口平均值的微服务应用程序和将原始数据摄取到HDFS（Hadoop Distributed File System）中的另一个微服务应用独立处理。为了处理数据，两个应用程序都在运行时将 topic 声明为其输入。

发布-订阅通信模型降低了生产者和消费者的复杂性，并允许在不中断现有流的情况下将新应用程序添加到拓扑中。例如，在平均值计算应用程序的下游，可以添加一个计算最高温度值以进行显示和监控的应用程序。然后，可以添加另一个应用程序，该应用程序解释用于故障检测的相同平均流。通过共享 topic 而不是点对点队列进行所有通信可以减少微服务之间的耦合。
虽然发布-订阅消息传递的概念并不新鲜，但Spring Cloud Stream采取了额外的步骤，使其成为其应用程序模型的一个有见解的选择。通过使用本地中间件支持，Spring Cloud Stream还简化了跨不同平台发布订阅模型的使用。