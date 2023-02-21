# 多Binder应用里面自定义Binder

当一个应用程序中有多个Binder并希望自定义Binder时，可以通过提供BinderCustomizer实现来实现。对于具有单个Binder的应用程序，这种特殊的定制器是不必要的，因为Binder上下文可以直接访问定制bean。然而，在多Binder场景中并非如此，因为不同的Binder存在于不同的应用程序上下文中。通过提供BinderCustomizer接口的实现，Binder虽然驻留在不同的应用程序上下文中，但将接收定制。Spring Cloud Stream确保在应用程序开始使用Binder之前进行定制。用户必须检查Binder类型，然后应用必要的自定义设置。


下面是一个提供BinderCustomizer bean的示例。


```java
@Bean
public BinderCustomizer binderCustomizer() {
    return (binder, binderName) -> {
        if (binder instanceof KafkaMessageChannelBinder) {
            ((KafkaMessageChannelBinder) binder).setRebalanceListener(...);
        }
        else if (binder instanceof KStreamBinder) {
            ...
        }
        else if (binder instanceof RabbitMessageChannelBinder) {
            ...
        }
    };
}
```

注意，当有多个相同类型的Binder实例时，Binder名称可用于过滤自定义。