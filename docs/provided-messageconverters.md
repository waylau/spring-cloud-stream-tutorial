# 提供的消息转换器


如前所述，框架已经提供了一个MessageConverter堆栈来处理最常见的用例。以下列表按优先级顺序（使用第一个工作的MessageConverter）描述了提供的MessageConverters：

* ApplicationJsonMessageMarshallingConverter：`org.springframework.messaging.converter.MappingJackson2MessageConverter`的变体。当contentType为application/json时，支持将消息的有效负载转换为POJO或从POJO转换。
* ByteArrayMessageConverter：当contentType为application/octet-stream时，支持将消息的有效负载从`byte[]`转换为`byte[]`。它本质上是一种传递，主要用于向后兼容。
* ObjectStringMessageConverter：当contentType为text/plain时，支持将任何类型转换为String。它调用Object的toString()方法，如果有效负载是`byte[]`，则调用一个新的`String(byte[])`。
* JsonUnmarshallingConverter：类似于ApplicationJsonMessageMarshallingConverter。当contentType为application/x-java-object时，它支持任何类型的转换。它期望实际类型信息作为属性嵌入contentType中（例如`application/x-java-object;type=foo.bar.Cat`）。

当找不到合适的转换器时，框架会抛出异常。当发生这种情况时，应该检查代码和配置，确保没有遗漏任何内容（即，确保通过使用绑定或标头提供了contentType）。然而，最有可能的是，发现了一些不常见的情况（例如自定义contentType），并且当前提供的MessageConverter堆栈不知道如何转换。如果是这种情况，可以添加自定义MessageConverter。
