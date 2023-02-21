# 用户定义的消息转换器

Spring Cloud Stream公开了一种定义和注册其他MessageConverter的机制。要使用它，请实现`org.springframework.messaging.converter.MessageConverter`，将其配置为`@Bean`。然后将其附加到现有的“MessageConverter”堆栈中。

自定义MessageConverter实现被添加到现有堆栈的头部，因此，自定义MessageConverter实现优先于现有的实现，这使您可以覆盖现有的转换器，也可以添加到现有的转换器中。
以下示例显示如何创建消息转换器bean以支持名为application/bar的新内容类型：


```java
@SpringBootApplication
public static class SinkApplication {

    ...

    @Bean
    public MessageConverter customMessageConverter() {
        return new MyCustomMessageConverter();
    }
}

public class MyCustomMessageConverter extends AbstractMessageConverter {

    public MyCustomMessageConverter() {
        super(new MimeType("application", "bar"));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return (Bar.class.equals(clazz));
    }

    @Override
    protected Object convertFromInternal(Message<?> message, Class<?> targetClass, Object conversionHint) {
        Object payload = message.getPayload();
        return (payload instanceof Bar ? payload : new Bar((byte[]) payload));
    }
}
```
