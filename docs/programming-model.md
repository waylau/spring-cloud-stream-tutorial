# 编程模型

要理解编程模型，应该熟悉以下核心概念：

* Destination Binder：负责提供与外部消息传递系统集成的组件。
* Binding：外部消息传递系统与应用程序提供的消息生产者和消费者之间的桥梁（由 Destination Binder创建）。
* 消息：生产者和消费者使用的规范数据结构，用于通过外部消息传递系统与 Destination Binder（以及其他应用程序）进行通信。


![Spring Cloud Stream编程模型](../images/SCSt-overview.png)



