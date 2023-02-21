# 实现自定义Binder

以下是实现自定义Binder的过程。


## 添加依赖



添加 spring-cloud-stream 依赖：


```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream</artifactId>
    <version>${spring.cloud.stream.version}</version>
</dependency>
```

## 提供 ProvisioningProvider 实现

ProvisioningProvider负责消费者和生产者destination的供应，并需要转换物理destination引用中application.yml或application.properties文件中包含的逻辑destination。

下面是ProvisioningProvider实现的一个示例，它简单地修剪了通过输入/输出 binding 配置提供的destination：



```java
public class FileMessageBinderProvisioner implements ProvisioningProvider<ConsumerProperties, ProducerProperties> {

    @Override
    public ProducerDestination provisionProducerDestination(
            final String name,
            final ProducerProperties properties) {

        return new FileMessageDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(
            final String name,
            final String group,
            final ConsumerProperties properties) {

        return new FileMessageDestination(name);
    }

    private class FileMessageDestination implements ProducerDestination, ConsumerDestination {

        private final String destination;

        private FileMessageDestination(final String destination) {
            this.destination = destination;
        }

        @Override
        public String getName() {
            return destination.trim();
        }

        @Override
        public String getNameForPartition(int partition) {
            throw new UnsupportedOperationException("Partitioning is not implemented for file messaging.");
        }

    }

}
```



## 提供 MessageProducer 实现



MessageProducer负责消费事件，并将其作为消息处理给配置为消费此类事件的客户端应用程序。

下面是MessageProducer实现的一个示例，它扩展了MessageProducerSupport抽象，以便轮询与修剪后的 destination 名称匹配且位于项目路径中的文件，同时存档读取的消息并丢弃随后的相同消息：


```java
public class FileMessageProducer extends MessageProducerSupport {

    public static final String ARCHIVE = "archive.txt";
    private final ConsumerDestination destination;
    private String previousPayload;

    public FileMessageProducer(ConsumerDestination destination) {
        this.destination = destination;
    }

    @Override
    public void doStart() {
        receive();
    }

    private void receive() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

        executorService.scheduleWithFixedDelay(() -> {
            String payload = getPayload();

            if(payload != null) {
                Message<String> receivedMessage = MessageBuilder.withPayload(payload).build();
                archiveMessage(payload);
                sendMessage(receivedMessage);
            }

        }, 0, 50, MILLISECONDS);
    }

    private String getPayload() {
        try {
            List<String> allLines = Files.readAllLines(Paths.get(destination.getName()));
            String currentPayload = allLines.get(allLines.size() - 1);

            if(!currentPayload.equals(previousPayload)) {
                previousPayload = currentPayload;
                return currentPayload;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private void archiveMessage(String payload) {
        try {
            Files.write(Paths.get(ARCHIVE), (payload + "\n").getBytes(), CREATE, APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
```


## 提供 MessageHandler 实现



MessageHandler提供生成事件所需的逻辑。

下面是MessageHandler实现的示例：

```java
public class FileMessageHandler implements MessageHandler{

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        //write message to file
    }

}
```

## 提供 Binder 实现


可以提供自己的Binder抽象实现。这可以通过以下方式轻松实现：

* 扩展AbstractMessageChannelBinder类
* 将ProvisioningProvider指定为AbstractMessageChannelBinder的泛型参数
* 重写createProducerMessageHandler和createConsumerEndpoint方法

举例：

```java
public class FileMessageBinder extends AbstractMessageChannelBinder<ConsumerProperties, ProducerProperties, FileMessageBinderProvisioner> {

    public FileMessageBinder(
            String[] headersToEmbed,
            FileMessageBinderProvisioner provisioningProvider) {

        super(headersToEmbed, provisioningProvider);
    }

    @Override
    protected MessageHandler createProducerMessageHandler(
            final ProducerDestination destination,
            final ProducerProperties producerProperties,
            final MessageChannel errorChannel) throws Exception {

        return message -> {
            String fileName = destination.getName();
            String payload = new String((byte[])message.getPayload()) + "\n";

            try {
                Files.write(Paths.get(fileName), payload.getBytes(), CREATE, APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    protected MessageProducer createConsumerEndpoint(
            final ConsumerDestination destination,
            final String group,
            final ConsumerProperties properties) throws Exception {

        return new FileMessageProducer(destination);
    }

}
```

## 创建 Binder 配置


严格要求您创建一个Spring配置来初始化binder 实现的bean（以及您可能需要的所有其他bean）：

```java
@Configuration
public class FileMessageBinderConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FileMessageBinderProvisioner fileMessageBinderProvisioner() {
        return new FileMessageBinderProvisioner();
    }

    @Bean
    @ConditionalOnMissingBean
    public FileMessageBinder fileMessageBinder(FileMessageBinderProvisioner fileMessageBinderProvisioner) {
        return new FileMessageBinder(null, fileMessageBinderProvisioner);
    }

}
```


## 在 META-INF/spring.binders 中定义binder


最后，必须在类路径上的META-INF/spring.binders文件中定义Binder，同时指定Binder的名称和Binder配置类的完整限定名：

```
myFileBinder:\
com.example.springcloudstreamcustombinder.config.FileMessageBinderConfiguration
```