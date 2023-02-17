# 应用模型

Spring Cloud Stream 应用程序由中间件中立核心组成。应用程序通过在外部broker（代理）公开的destination（目的地）和代码中的输入/输出参数之间建立 bindings 来与外部世界通信。建立bindings 所需的特定于broker的详细信息由特定于中间件的Binder实现处理。


![Spring Cloud Stream 应用程序](../images/SCSt-with-binder.png)