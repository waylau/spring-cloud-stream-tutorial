
# Binder配置属性

自定义Binder配置时，可以使用以下属性。这些属性通过org.springframework.cloud.stream.config.BinderProperties公开。


它们必须以“`spring.cloud.stream.binders.<configurationName>`”作为前缀。


* type：Binder类型。它通常引用类路径上的一个Binder，定义在META-INF/spring.binders文件中的key。默认情况下，它的值与配置名称相同。
* inheritEnvironment：配置是否继承应用程序本身的环境。默认值：true。
* environment：可用于自定义Binder环境的一组属性的根。设置此属性时，正在其中创建Binder的上下文不是应用程序上下文的子级。此设置允许Binder组件和应用程序组件之间完全分离。默认值：空。
* defaultCandidate：Binder配置是被视为默认Binder的候选项，还是只能在显式引用时使用。此设置允许在不干扰默认处理的情况下添加Binder配置。默认值：true。