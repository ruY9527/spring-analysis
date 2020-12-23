## 						Spring 工作流程分析



#### 前言

   这里是再次整理的阅读 Spring 的源码, 相对比上次的阅读，我希望这次可以更清晰&更深刻的理解Spring,也不仅仅会从一个案例来进行分析，会结合多方面的知识来进行整理分析.

​    这里放上之前阅读的比例 :    https://github.com/baoyang23/source-notes/tree/master/java/spring_bean

​    该目录下面有 :  bean/get/extend  三个主要地方的分析. 

​    此模块还是讲述 整体的 flow,后面会对单个进行分析&Spring提供怎么样的扩展方式来进行增强扩展等.

 案例入门操作的话,可以参考之前的博客.



#### 分析

​    这里我们先不忙这其他类型的bean分析, 就对我们作为 config 的 bean 进行分析. 先单个分析容易理解些.

​    入口类 : 

```java
public class InitWorkFlowSpring {


    public static void main(String[] args) {

        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(YangBeanScannerConfig.class);
        YangBeanScannerConfig yangBeanScannerConfig = context.getBean(YangBeanScannerConfig.class);
        yangBeanScannerConfig.say();

    }

}
```



​    配置类:

```java
@ComponentScan(basePackages = "com.iyang.spring")
@Description(value = "This is GavinYang DemoWorld.")
public class YangBeanScannerConfig {

    public YangBeanScannerConfig(){
        System.out.println("配置扫描初始化打印");
    }

    public void say(){
        System.out.println("我是从Spring容器中获取出来的");
    }
}
```

可以看到，当我们启动 main 方法的时候，是可以看到 YangBeanScannerConfig 中构造函数打印的内容和调用say方法打印出来的内容.



基于这个基础上,我们debug一层一层的走进去看,Spring做了什么事情.

先进入到我们new出来的AnnotationConfigApplicationContext中来

调用自身的无参构造函数

调用 register 注册方法

最后调用一个 refresh, refresh 方法中是做了很多事的.

```java
public AnnotationConfigApplicationContext(Class<?>... componentClasses) {
   this();
   register(componentClasses);
   refresh();
}
```

那么有了入口，我们就根据这些方法来一个一个的分析.



#### this() 方法 --->  org.springframework.context.annotation.AnnotationConfigApplicationContext#AnnotationConfigApplicationContext()

先来看 this 方法做了什么事情.

创建了二个对象，分别是 注解bd读取/类路口db扫描.

比如有意思的是,传入this(AnnotationConfigApplicationContext), 然后返回来的reader/scanner又属于this.也是相互之间各自都持有各自的引用.

```java
public AnnotationConfigApplicationContext() {
   this.reader = new AnnotatedBeanDefinitionReader(this);
   this.scanner = new ClassPathBeanDefinitionScanner(this);
}
```



##### new AnnotatedBeanDefinitionReader

来，看下new一个对象做了什么事情.

```java

public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
// 这里的 getOrCreateEnvironment 方法中,AnnotationConfigApplicationContext是EnvironmentCapable的子类,
// 所以Environment也是从AnnotationConfigApplicationContext中获取出来的.    
   this(registry, getOrCreateEnvironment(registry));
}

--------------
    
public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry, Environment environment) {
 // 检验 registry/environment都不能为null.   
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		Assert.notNull(environment, "Environment must not be null");
		this.registry = registry;
// 这里将 registry/environment 给传入构造到 org.springframework.context.annotation.ConditionEvaluator 中来.
// ConditionEvaluator又借助org.springframework.context.annotation.ConditionEvaluator.ConditionContextImpl#ConditionContextImpl 来存储这些信息,所以这里最后的信息是在ConditionContextImpl中来了.    
		this.conditionEvaluator = new ConditionEvaluator(registry, environment, null);
// org.springframework.context.annotation.AnnotationConfigUtils#registerAnnotationConfigProcessors(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)
// 从该方法的名字上看,是对注册注解配置进行处理.    
		AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
}

```



###### org.springframework.context.annotation.AnnotationConfigUtils#registerAnnotationConfigProcessors(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object) 分析

这里根据我们的案列，传入进来的source是null.

```java
public static Set<BeanDefinitionHolder> registerAnnotationConfigProcessors(
      BeanDefinitionRegistry registry, @Nullable Object source) {
// 根据 registry 的类型来获取 DefaultListableBeanFactory.
// 这里的registry属于GenericApplicationContext,调用其getDefaultListableBeanFactory来获取.    
   DefaultListableBeanFactory beanFactory = unwrapDefaultListableBeanFactory(registry);
   if (beanFactory != null) {
// beanFactory.getDependencyComparator() 返回的是null,满足条件.       
      if (!(beanFactory.getDependencyComparator() instanceof AnnotationAwareOrderComparator)) {
// 设置 AnnotationAwareOrderComparator 到beanFactory中来          
         beanFactory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);
      }
// get方法获取出来的是SimpleAutowireCandidateResolver,       
      if (!(beanFactory.getAutowireCandidateResolver() instanceof ContextAnnotationAutowireCandidateResolver)) {
// 设置ContextAnnotationAutowireCandidateResolver到beanFactory中来.          
         beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
      }
   }

   Set<BeanDefinitionHolder> beanDefs = new LinkedHashSet<>(8);

// 可以看到每个都有 internal 来特意表明内部的意思.    
// org.springframework.context.annotation.internalConfigurationAnnotationProcessor --->  ConfigurationClassPostProcessor
// org.springframework.context.annotation.internalAutowiredAnnotationProcessor  --> AutowiredAnnotationBeanPostProcessor
// org.springframework.context.annotation.internalCommonAnnotationProcessor   ---> CommonAnnotationBeanPostProcessor 
// org.springframework.context.annotation.internalPersistenceAnnotationProcessor  ---> PersistenceAnnotationBeanPostProcessor
// org.springframework.context.event.internalEventListenerProcessor   ---> EventListenerMethodProcessor
// org.springframework.context.event.internalEventListenerFactory  --- > DefaultEventListenerFactory
   if (!registry.containsBeanDefinition(CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(ConfigurationClassPostProcessor.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME));
   }

   if (!registry.containsBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
   }

   // Check for JSR-250 support, and if present add the CommonAnnotationBeanPostProcessor.
   if (jsr250Present && !registry.containsBeanDefinition(COMMON_ANNOTATION_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
   }

   // Check for JPA support, and if present add the PersistenceAnnotationBeanPostProcessor.
   if (jpaPresent && !registry.containsBeanDefinition(PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition();
      try {
         def.setBeanClass(ClassUtils.forName(PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME,
               AnnotationConfigUtils.class.getClassLoader()));
      }
      catch (ClassNotFoundException ex) {
         throw new IllegalStateException(
               "Cannot load optional framework class: " + PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME, ex);
      }
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME));
   }

   if (!registry.containsBeanDefinition(EVENT_LISTENER_PROCESSOR_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(EventListenerMethodProcessor.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_PROCESSOR_BEAN_NAME));
   }

   if (!registry.containsBeanDefinition(EVENT_LISTENER_FACTORY_BEAN_NAME)) {
      RootBeanDefinition def = new RootBeanDefinition(DefaultEventListenerFactory.class);
      def.setSource(source);
      beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_FACTORY_BEAN_NAME));
   }

   return beanDefs;
}
```

这里都是先判断这些内部的bean,是不是已经在 registry 中已经存在了,如果没有存在的话，就会利用类信息来构造出一个RootBeanDefinition来,接着就是调用 registerPostProcessor 方法给注册到 registry  中来.

最后返回一个注册过的 bean 的 Set 集合回去.

总结下这里就是为了给spring容器中注册一些内部的 bean 进去. 这些注册进去的bean,都是在后面初始化bean&解析bean等情况有使用到的.



##### new ClassPathBeanDefinitionScanner() 方法



```java
public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
   this(registry, useDefaultFilters, getOrCreateEnvironment(registry));
}

-----------------------------------------------------
// 最后走到 org.springframework.context.annotation.ClassPathBeanDefinitionScanner#ClassPathBeanDefinitionScanner(org.springframework.beans.factory.support.BeanDefinitionRegistry, boolean, org.springframework.core.env.Environment, org.springframework.core.io.ResourceLoader) 构造函数来.    
public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters,
			Environment environment, @Nullable ResourceLoader resourceLoader) {

		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
// 赋值 registry 来.    
		this.registry = registry;

		if (useDefaultFilters) {
// 添加 filter 到 includeFilters 中来.
// AnnotationTypeFilter(Component.class)
// AnnotationTypeFilter(((Class<? extends Annotation>) ClassUtils.forName("javax.annotation.ManagedBean", cl)     
// 等信息进来      
			registerDefaultFilters();
		}
// org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider#setEnvironment
// 设置 enviornment到父类中来.    
		setEnvironment(environment);
// 这里也是这是到父类来了.
// 返回的resourcePatternResolver是AnnotationConfigApplicationContext.
// metadataReaderFactory 是 CachingMetadataReaderFactory 对象来.
// componentsIndex 是 null.    
		setResourceLoader(resourceLoader);
}    

```

该方法可以看到,添加了三个 filter 到 includeFilters 中来.

设置environment / resource 到 其父类org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider 中来.

也就是setXXX方法是调用的父类.





#### register(componentClasses)  方法

  

```java
@Override
public void register(Class<?>... componentClasses) {
   //  检验传入进来的 comonpentClasses是一定要有值的. 
   Assert.notEmpty(componentClasses, "At least one component class must be specified");
   this.reader.register(componentClasses);
}

-------------------------
// 这里从名字上就可以很容易看出是注册 bean 的    
public void register(Class<?>... componentClasses) {
		for (Class<?> componentClass : componentClasses) {
			registerBean(componentClass);
		}
	}

-----------------------------
private <T> void doRegisterBean(Class<T> beanClass, @Nullable String name,
			@Nullable Class<? extends Annotation>[] qualifiers, @Nullable Supplier<T> supplier,
			@Nullable BeanDefinitionCustomizer[] customizers) {
// new 一个 bd 出来.
		AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(beanClass);
// 这里没有 @Conditional 注解和 metadata 是 null 就会直接返回 false 来.    
		if (this.conditionEvaluator.shouldSkip(abd.getMetadata())) {
			return;
		}

		abd.setInstanceSupplier(supplier);
 
// 在对象上获取 @Scope 注解,这里没有,所以就不会往下走.
// 这里返回的 ScopeMetadata应该是默认的,scopeName是singleton,scopedProxyMode是No/1    
		ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
		abd.setScope(scopeMetadata.getScopeName());
// 获取 beanName 来    
		String beanName = (name != null ? name : this.beanNameGenerator.generateBeanName(abd, this.registry));

// 对一些注解的处理.
// @Lazy , @Primary , @DependsOn , @Role , @Description 如果有这些注解的话,就会进行处理.
// 根据注解的名字,来调用相应的set方法.    
		AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
		if (qualifiers != null) {
			for (Class<? extends Annotation> qualifier : qualifiers) {
				if (Primary.class == qualifier) {
					abd.setPrimary(true);
				}
				else if (Lazy.class == qualifier) {
					abd.setLazyInit(true);
				}
				else {
					abd.addQualifier(new AutowireCandidateQualifier(qualifier));
				}
			}
		}
		if (customizers != null) {
			for (BeanDefinitionCustomizer customizer : customizers) {
				customizer.customize(abd);
			}
		}

		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
		definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
	}    

```

