## 		             Spring_Bean 分析



#### 前提

   我们在创建 Spring Bean 的时候，是可以通过很多种方式来创建的. 但是这么多种方式,又是怎么加载的？是不是又有顺序呢？ 所以对 Spring 的  Bean 创建还是很有必要的.



#### 创建方式



我们可以通过自己创建 bd  , 然后调用 registerBeanDefinition 方法给注册到 Spring 中来.

那么创建bd的怎么创建的呢？可以看到下面的二种创建方式.

这是通过 bd 来的.

```java
public class BeanDefinitionCreateAndRegister {


    public static void main(String[] args) {

        // 1 : 通过 BeanDefinitionBuilder 来创建 bd
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(Person.class);
        beanDefinitionBuilder.addPropertyValue("id",9527).addPropertyValue("name","GavinYang");
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();

        // 2 : 通过 new GenericBeanDefinition 来创建 bd.
        GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
        genericBeanDefinition.setBeanClass(Person.class);
        MutablePropertyValues mutablePropertyValues = new MutablePropertyValues();
        mutablePropertyValues.add("id",1).add("name","Peterwong");
        genericBeanDefinition.setPropertyValues(mutablePropertyValues);

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        // 这里是给 bd 给注册到 Spring 容器里面来.
        // context.registerBeanDefinition("person",beanDefinition);
        context.registerBeanDefinition("peterwong",genericBeanDefinition);

        // 如果这里不调用 refresh 是会有错误的.
        context.refresh();

        Person person = context.getBean(Person.class);
        person.say();
        System.out.println(person.toString());

    }

}
```





通过我们常用的注解

这里主要是 @Import/@Bean/@Component+@ComponentScan 方式来注入对象到 Spring 容器中来.

```java
@Import(ImportBeanConfigMain.ImportConfig.class)
@ComponentScan(basePackages = "com.iyang.bean.bd")
public class ImportBeanConfigMain {
    
	public ImportBeanConfigMain(){
        System.out.println("ImportBeanConfigMain 无参数构造函数");
    }

    
    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(ImportBeanConfigMain.class);
        context.refresh();

        ImportConfig importConfig = context.getBean(ImportConfig.class);
        Person person = context.getBean(Person.class);
        AnnotConfig annotConfig = context.getBean(AnnotConfig.class);
        ExternalConfig externalConfig = context.getBean(ExternalConfig.class);

        System.out.println(importConfig);
        System.out.println(person);
        System.out.println(annotConfig);
        System.out.println(externalConfig);

    }

    /**
     * 通过 @Import 导入进来.
     */
    public class ImportConfig{

        public void importMe(){
            System.out.println("这是导入自己的方法");
        }

        @Override
        public String toString() {
            return "ImportConfig 的 toString 方法";
        }

        public ImportConfig(){
            System.out.println("ImportConfig无参数构造函数");
        }
        /**
         * 使用 @Bean 注解 注入 Bean 进来.
         * @return
         */
        @Bean
        public Person importPerson(){
            return new Person(9527,"GavinYang");
        }

    }

    @Component
    public static class AnnotConfig{


        public AnnotConfig(){
            System.out.println("AnnotConfig无参数构造函数");
        }

        @Override
        public String toString() {
            return "使用注解来注入bean进来.";
        }
    }

}

@Component
class ExternalConfig {

    public ExternalConfig(){
        System.out.println("externalConfig 无参构造函数");
    }

    @Override
    public String toString() {
        return "externalConfig 打印 toString() 方法";
    }
}


----------------------------------------------
ImportBeanConfigMain 无参数构造函数    
externalConfig 无参构造函数
AnnotConfig无参数构造函数
ImportConfig无参数构造函数
person 有参数构造函数
ImportConfig 的 toString 方法
Person{id=9527, name='GavinYang'}
使用注解来注入bean进来.
externalConfig 打印 toString() 方法
    
// 这里可以看到new出来的对象打印顺序.    
```



如果是基于创建 bd 的方式的话，是说明下是可以通过这种方式来将我们自己创建的对象给注入到Spring容器中来.我们主要来分析第二种,是做了什么事情.



#### @Import/@Bean/@Component+@ComponentScan 分析



在分析之前，我们看下我们的 beanClass 是怎么先注册到 Spring中来的,也就是在org.springframework.beans.factory.support.DefaultListableBeanFactory#beanDefinitionMap和org.springframework.beans.factory.support.DefaultListableBeanFactory#beanDefinitionNames中,可以看到一个是Map类型的,一个是集合类型的.

我们把断点打在 org.springframework.beans.factory.support.DefaultListableBeanFactory#registerBeanDefinition 进来的方法上就可以看到,然后看堆栈信息,就可以看到怎么一步一步给添加进来的.



##### 注册 Spring 中来走的方法

  这里只用关注我们自己自己定义的，Spring内部的就不需要管了。



  **ImportBeanConfigMain**

  org.springframework.context.annotation.AnnotatedBeanDefinitionReader#registerBean(java.lang.Class<?>)     ---->     org.springframework.beans.factory.support.BeanDefinitionReaderUtils#registerBeanDefinition   ---->   org.springframework.context.support.GenericApplicationContext#registerBeanDefinition



**externalConfig**

org.springframework.context.support.AbstractApplicationContext#invokeBeanFactoryPostProcessors  ---->

org.springframework.context.support.PostProcessorRegistrationDelegate#invokeBeanDefinitionRegistryPostProcessors    ----->   org.springframework.context.annotation.ConfigurationClassPostProcessor#processConfigBeanDefinitions  ---->   org.springframework.context.annotation.ConfigurationClassParser#parse(org.springframework.core.type.AnnotationMetadata, java.lang.String)   --->  org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass   ---->

org.springframework.context.annotation.ClassPathBeanDefinitionScanner#doScan   --->  org.springframework.context.annotation.ClassPathBeanDefinitionScanner#registerBeanDefinition 



**importBeanConfigMain.AnnotConfig**

org.springframework.context.support.AbstractApplicationContext#invokeBeanFactoryPostProcessors ---->org.springframework.context.support.PostProcessorRegistrationDelegate#invokeBeanFactoryPostProcessors(org.springframework.beans.factory.config.ConfigurableListableBeanFactory, java.util.List<org.springframework.beans.factory.config.BeanFactoryPostProcessor>)     ---->   org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry ---->  org.springframework.context.annotation.ConfigurationClassPostProcessor#processConfigBeanDefinitions  --->  org.springframework.context.annotation.ConfigurationClassParser#parse(org.springframework.core.type.AnnotationMetadata, java.lang.String)   ---> org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass  --->  org.springframework.context.annotation.ClassPathBeanDefinitionScanner#doScan   ---> org.springframework.context.annotation.ClassPathBeanDefinitionScanner#registerBeanDefinition



**com.iyang.bean.bd.ImportBeanConfigMain$ImportConfig**

org.springframework.context.support.AbstractApplicationContext#invokeBeanFactoryPostProcessors  --->  org.springframework.context.support.PostProcessorRegistrationDelegate#invokeBeanDefinitionRegistryPostProcessors   ---> org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry  ----> org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitions   ---> org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#registerBeanDefinitionForImportedConfigurationClass 



**importPerson**

org.springframework.context.support.AbstractApplicationContext#invokeBeanFactoryPostProcessors  ---> org.springframework.context.support.PostProcessorRegistrationDelegate#invokeBeanDefinitionRegistryPostProcessors   --->   org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry   ---> org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitions   ---> org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsForBeanMethod  



可以看到除了 ImportBeanConfigMain 在扫描的时候就被注册到 spring 容器里面来，后面的都是走的 AbstractApplicationContext#invokeBeanFactoryPostProcessors 方法给注册到 Spring 容器中来了. 是不是应该详细分析下 invokeBeanFactoryPostProcessors  方法到了做了什么或者说用了什么,将我们定义的对象给注册到 Spring 容器中来了呢？



##### invokeBeanFactoryPostProcessors 方法解析

从上面来看，这个方法并不是我们想象中那么简单的.

org.springframework.context.support.PostProcessorRegistrationDelegate#invokeBeanFactoryPostProcessors(org.springframework.beans.factory.config.ConfigurableListableBeanFactory, java.util.List<org.springframework.beans.factory.config.BeanFactoryPostProcessor>)   委托到这里来进行解析的,所以我们直接深度分析这个方法即可.

上面可以看到都是走的 PostProcessorRegistrationDelegate 这个类,但是我们并没有在这个方法中找到这个类.

```java
public static void invokeBeanFactoryPostProcessors(
      ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

   // Invoke BeanDefinitionRegistryPostProcessors first, if any.
   Set<String> processedBeans = new HashSet<>();

   if (beanFactory instanceof BeanDefinitionRegistry) {
      BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
      List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
      List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

      for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
         if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
            BeanDefinitionRegistryPostProcessor registryProcessor =
                  (BeanDefinitionRegistryPostProcessor) postProcessor;
            registryProcessor.postProcessBeanDefinitionRegistry(registry);
            registryProcessors.add(registryProcessor);
         }
         else {
            regularPostProcessors.add(postProcessor);
         }
      }

      // Do not initialize FactoryBeans here: We need to leave all regular beans
      // uninitialized to let the bean factory post-processors apply to them!
      // Separate between BeanDefinitionRegistryPostProcessors that implement
      // PriorityOrdered, Ordered, and the rest.
      List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

      // First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
      String[] postProcessorNames =
            beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
      for (String ppName : postProcessorNames) {
         if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
            currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
            processedBeans.add(ppName);
         }
      }
      sortPostProcessors(currentRegistryProcessors, beanFactory);
      registryProcessors.addAll(currentRegistryProcessors);
// Note : 我们根据 debug 是可以跟进到这里的, 我们直接在这里打上断点,再来仔细看看这个方法做了什么事情.   // currentRegistryProcessors : org.springframework.context.annotation.ConfigurationClassPostProcessor      
// registry :  DefaultableListFactory 
// 走完这个方法,我们的bean信息都注册到 Spring 的 DefaultLitableFactory中来了.      
      invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
      currentRegistryProcessors.clear();

      // Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
      postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
      for (String ppName : postProcessorNames) {
         if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
            currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
            processedBeans.add(ppName);
         }
      }
      sortPostProcessors(currentRegistryProcessors, beanFactory);
      registryProcessors.addAll(currentRegistryProcessors);
      invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
      currentRegistryProcessors.clear();

      // Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
      boolean reiterate = true;
      while (reiterate) {
         reiterate = false;
         postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
         for (String ppName : postProcessorNames) {
            if (!processedBeans.contains(ppName)) {
               currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
               processedBeans.add(ppName);
               reiterate = true;
            }
         }
         sortPostProcessors(currentRegistryProcessors, beanFactory);
         registryProcessors.addAll(currentRegistryProcessors);
         invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
         currentRegistryProcessors.clear();
      }

      // Now, invoke the postProcessBeanFactory callback of all processors handled so far.
      invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
      invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
   }

   else {
      // Invoke factory processors registered with the context instance.
      invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
   }

   // Do not initialize FactoryBeans here: We need to leave all regular beans
   // uninitialized to let the bean factory post-processors apply to them!
   String[] postProcessorNames =
         beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

   // Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
   // Ordered, and the rest.
   List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
   List<String> orderedPostProcessorNames = new ArrayList<>();
   List<String> nonOrderedPostProcessorNames = new ArrayList<>();
   for (String ppName : postProcessorNames) {
      if (processedBeans.contains(ppName)) {
         // skip - already processed in first phase above
      }
      else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
         priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
      }
      else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
         orderedPostProcessorNames.add(ppName);
      }
      else {
         nonOrderedPostProcessorNames.add(ppName);
      }
   }

   // First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
   sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
   invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

   // Next, invoke the BeanFactoryPostProcessors that implement Ordered.
   List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
   for (String postProcessorName : orderedPostProcessorNames) {
      orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
   }
   sortPostProcessors(orderedPostProcessors, beanFactory);
   invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

   // Finally, invoke all other BeanFactoryPostProcessors.
   List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
   for (String postProcessorName : nonOrderedPostProcessorNames) {
      nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
   }
   invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

   // Clear cached merged bean definitions since the post-processors might have
   // modified the original metadata, e.g. replacing placeholders in values...
   beanFactory.clearMetadataCache();
}
```



###### org.springframework.context.annotation.ConfigurationClassPostProcessor#processConfigBeanDefinitions 方法

```java
/**
 * Build and validate a configuration model based on the registry of
 * {@link Configuration} classes.
 */
public void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {
   List<BeanDefinitionHolder> configCandidates = new ArrayList<>();
   String[] candidateNames = registry.getBeanDefinitionNames();

   for (String beanName : candidateNames) {
      BeanDefinition beanDef = registry.getBeanDefinition(beanName);
      if (beanDef.getAttribute(ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE) != null) {
         if (logger.isDebugEnabled()) {
            logger.debug("Bean definition has already been processed as a configuration class: " + beanDef);
         }
      }
// 对是否满足配置类进行检查, 这里我们的bean是importBeanConfigMain,满足条件的,具体可以看下面该方法的分析.然后会构建一个 bdHolder,添加到集合中来.
      else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef, this.metadataReaderFactory)) {
         configCandidates.add(new BeanDefinitionHolder(beanDef, beanName));
      }
   }

   // Return immediately if no @Configuration classes were found
   if (configCandidates.isEmpty()) {
      return;
   }

   // Sort by previously determined @Order value, if applicable
// 这里会根据 @Order 来进行排序下.
// 从 Integer.compare(i1, i2) 来分析，应该是从小到大的排序,也就是说,越小的话,优先级就约高. 
   configCandidates.sort((bd1, bd2) -> {
      int i1 = ConfigurationClassUtils.getOrder(bd1.getBeanDefinition());
      int i2 = ConfigurationClassUtils.getOrder(bd2.getBeanDefinition());
      return Integer.compare(i1, i2);
   });

   // Detect any custom bean name generation strategy supplied through the enclosing application context
   SingletonBeanRegistry sbr = null;
   if (registry instanceof SingletonBeanRegistry) {
// 满足类型条件强转下.       
      sbr = (SingletonBeanRegistry) registry;
      if (!this.localBeanNameGeneratorSet) {
// 这里不包含,所以返回的就是null.
//org.springframework.beans.factory.support.DefaultSingletonBeanRegistry#getSingleton(java.lang.String, boolean)          
         BeanNameGenerator generator = (BeanNameGenerator) sbr.getSingleton(
               AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR);
         if (generator != null) {
            this.componentScanBeanNameGenerator = generator;
            this.importBeanNameGenerator = generator;
         }
      }
   }
// 确保environment不是null.
   if (this.environment == null) {
      this.environment = new StandardEnvironment();
   }

   // Parse each @Configuration class
// 创建一个解析 @Configuration 的对象.
// 在创建ConfigurationClassParser的这个有参构造函数里面,是可以看到又new了二个对象的,一个是ComponentScanAnnotationParser,一个是ConditionEvaluator.
// ComponentScanAnnotationParser 这个从名字上看,可以理解为@ComponentScan注解的解析.  
   ConfigurationClassParser parser = new ConfigurationClassParser(
         this.metadataReaderFactory, this.problemReporter, this.environment,
         this.resourceLoader, this.componentScanBeanNameGenerator, registry);

   Set<BeanDefinitionHolder> candidates = new LinkedHashSet<>(configCandidates);
   Set<ConfigurationClass> alreadyParsed = new HashSet<>(configCandidates.size());
   do {
// org.springframework.context.annotation.ConfigurationClassParser#processConfigurationClass这里走到这里,主要看这个方法中的doProcessConfigurationClass方法.       
      parser.parse(candidates);
// 这里对我们上面解析出来的bean进行valiate,如果validate失败的话,那么最后是会抛出一个异常来的.	       
      parser.validate();

// 装有我们解析出来的bean信息       
      Set<ConfigurationClass> configClasses = new LinkedHashSet<>(parser.getConfigurationClasses());
// 移除已经解析过了的.       
      configClasses.removeAll(alreadyParsed);

      // Read the model and create bean definitions based on its content
//如果this.reader是null的话,就会new一个ConfigurationClassBeanDefinitionReader出来.       
      if (this.reader == null) {
         this.reader = new ConfigurationClassBeanDefinitionReader(
               registry, this.sourceExtractor, this.resourceLoader, this.environment,
               this.importBeanNameGenerator, parser.getImportRegistry());
      }
 // 这里对我们获取的 bean 再进行一个 load.      
      this.reader.loadBeanDefinitions(configClasses);
// 解析过了的bean放入到 alreadyParsed 中来.       
      alreadyParsed.addAll(configClasses);

      candidates.clear();
// 扫描获取出来的bean个数大于 初始化传入进来的个数.       
      if (registry.getBeanDefinitionCount() > candidateNames.length) {
        // 获取出新扫描的bean信息.  
         String[] newCandidateNames = registry.getBeanDefinitionNames();
        // 旧的bean信息  
         Set<String> oldCandidateNames = new HashSet<>(Arrays.asList(candidateNames));
        // 表示已经注册过了的  
         Set<String> alreadyParsedClasses = new HashSet<>();
       // 将外面的 alreadyParsed 中的元素的 metadata的className给放入到alreadyParsedClasses集合中来.    
         for (ConfigurationClass configurationClass : alreadyParsed) {
            alreadyParsedClasses.add(configurationClass.getMetadata().getClassName());
         }
 // 对new的集合中元素进行迭代         
         for (String candidateName : newCandidateNames) {
       // 老的集合中不包含      
            if (!oldCandidateNames.contains(candidateName)) {
               BeanDefinition bd = registry.getBeanDefinition(candidateName);
                
       // alreadyParsedClasses 中不包含并且检验出需要配置的,比如有一些@Configuration等特殊注解，这个方法在之前是有提到的.         
               if (ConfigurationClassUtils.checkConfigurationClassCandidate(bd, this.metadataReaderFactory) &&
                     !alreadyParsedClasses.contains(bd.getBeanClassName())) {
           // 满足上面这些条件就会放入到candidates集合中来.         
                  candidates.add(new BeanDefinitionHolder(bd, candidateName));
               }
            }
         }
         candidateNames = newCandidateNames;
      }
   }
// candidates 是 empty 就跳出while循环,否则就认为还有bean需要解析.    
   while (!candidates.isEmpty());

   // Register the ImportRegistry as a bean in order to support ImportAware @Configuration classes
// org.springframework.context.annotation.ConfigurationClassPostProcessor.importRegistry sbr不包含importRegistry的话,就会注册一个进去.   
   if (sbr != null && !sbr.containsSingleton(IMPORT_REGISTRY_BEAN_NAME)) {
      sbr.registerSingleton(IMPORT_REGISTRY_BEAN_NAME, parser.getImportRegistry());
   }

   if (this.metadataReaderFactory instanceof CachingMetadataReaderFactory) {
      // Clear cache in externally provided MetadataReaderFactory; this is a no-op
      // for a shared cache since it'll be cleared by the ApplicationContext.
 // 这里是清除缓存,也是清除一些集合.      
      ((CachingMetadataReaderFactory) this.metadataReaderFactory).clearCache();
   }
}
```



**走完这个方法,如果是debug模式的话,就可以在 registry(也就是DefaultListableBeanFactory)的 beanDefintionMap和beanDefinitionNames这二个集合中是可以看到我们的bean名字已经bean对应的class信息的.**



###### org.springframework.context.annotation.ConfigurationClassParser#doProcessConfigurationClass方法

可以看到这个方法就是对 configuration 类进行处理的.

```java
/**
 * Apply processing and build a complete {@link ConfigurationClass} by reading the
 * annotations, members and methods from the source class. This method can be called
 * multiple times as relevant sources are discovered.
 * @param configClass the configuration class being build
 * @param sourceClass a source class
 * @return the superclass, or {@code null} if none found or previously processed
 */
@Nullable
protected final SourceClass doProcessConfigurationClass(ConfigurationClass configClass, SourceClass sourceClass)
      throws IOException {

// 判断是不是有 @Component 注解.  
   if (configClass.getMetadata().isAnnotated(Component.class.getName())) {
      // Recursively process any member (nested) classes first
      processMemberClasses(configClass, sourceClass);
   }

   // Process any @PropertySource annotations
// 接着再处理 @PropertySources 注解. 可以看到这个注解貌似是和 Environment 有关系.   
   for (AnnotationAttributes propertySource : AnnotationConfigUtils.attributesForRepeatable(
         sourceClass.getMetadata(), PropertySources.class,
         org.springframework.context.annotation.PropertySource.class)) {
      if (this.environment instanceof ConfigurableEnvironment) {
         processPropertySource(propertySource);
      }
      else {
         logger.info("Ignoring @PropertySource annotation on [" + sourceClass.getMetadata().getClassName() +
               "]. Reason: Environment must implement ConfigurableEnvironment");
      }
   }

   // Process any @ComponentScan annotations
// 获取@ComponentScan 注解,我们这里是有的.    
   Set<AnnotationAttributes> componentScans = AnnotationConfigUtils.attributesForRepeatable(
         sourceClass.getMetadata(), ComponentScans.class, ComponentScan.class);
   if (!componentScans.isEmpty() &&
         !this.conditionEvaluator.shouldSkip(sourceClass.getMetadata(), ConfigurationPhase.REGISTER_BEAN)) {
      for (AnnotationAttributes componentScan : componentScans) {
         // The config class is annotated with @ComponentScan -> perform the scan immediately
          
// org.springframework.context.annotation.ComponentScanAnnotationParser#parse
// parse 方法内部是使用 ClassPathBeanDefinitionScanner 扫描器的,对resourcePattern/includeFilters/excludeFilters/lazyInit 是否有进行处理.
// 获取注解上的属性 basePackages/basePackageClasses的值,添加一个AbstractTypeHierarchyTraversingFilter,这个是ExcludeFilter
//最后来org.springframework.context.annotation.ClassPathBeanDefinitionScanner#doScan做扫描操作.
//doScan做了什么事情呢? 显示通过传入进来的包,调用findCandidateComponents获取出bd的集合来,ScopeMetadata设置也是默认的,用beanNameGenerator生成bean对应的beanName
//如果bd是AbstractBeanDefinition,再走一下postProcessBeanDefinition方法
//如果bd是AnnotatedBeanDefinition,会走AnnotationConfigUtils.processCommonDefinitionAnnotations()方法,也是对一些注解的属性进行设置值操作. 走个checkCandidat检查方法,确保bd再registry中不存在的,如果存在的话,那就说明是已经注册过了的.     //如果是不存在的话,就会new一个BeanDefinitionHolder来,然后走registerBeanDefinition给注册到Spring容器中来. 最后返回扫描获取到的bdHolder集合来.     
         Set<BeanDefinitionHolder> scannedBeanDefinitions =
               this.componentScanParser.parse(componentScan, sourceClass.getMetadata().getClassName());
         // Check the set of scanned definitions for any further config classes and parse recursively if needed
         for (BeanDefinitionHolder holder : scannedBeanDefinitions) {
            BeanDefinition bdCand = holder.getBeanDefinition().getOriginatingBeanDefinition();
            if (bdCand == null) {
               bdCand = holder.getBeanDefinition();
            }
// 可以看到这里, 我们在最初进入到processConfigBeanDefinitions来的时候,其实就已经是调用了这个方法,那么我们这里扫描获取的bean在此调用这个方法. 也就是确保,扫描获取的bean,也是有一些配置的注解并且也是需要解析的.           
            if (ConfigurationClassUtils.checkConfigurationClassCandidate(bdCand, this.metadataReaderFactory)) {
// org.springframework.context.annotation.ConfigurationClassParser#processConfigurationClass这里最后也是走到这里了.
// 最初我们是从parse.parse() 进来的,也是走的ConfigurationClassParser#processConfigurationClas,这里又走到了该方法.
// 也就说我们是调用这个方法,只要满足条件的话,就会一直调用这个方法,直到不满足条件为止.                
               parse(bdCand.getBeanClassName(), holder.getBeanName());
            }
         }
      }
   }

   // Process any @Import annotations
// 这里是对 @Import 注解进行处理. 该方法是有利用 importStack 来控制,
// 其内部又分为 @ImportSelector/@ImportBeanDefinitionRegistrar/无注解这三种情况.
// 获取完 bean 信息后,就又走到了org.springframework.context.annotation.ConfigurationClassParser#processConfigurationClass方法来.
// 最后importStack 调用 pop 给数据给弹出来.    
   processImports(configClass, sourceClass, getImports(sourceClass), true);

   // Process any @ImportResource annotations
// 对@ImportResource是否有进行判断.    
   AnnotationAttributes importResource =
         AnnotationConfigUtils.attributesFor(sourceClass.getMetadata(), ImportResource.class);
   if (importResource != null) {
      String[] resources = importResource.getStringArray("locations");
      Class<? extends BeanDefinitionReader> readerClass = importResource.getClass("reader");
      for (String resource : resources) {
         String resolvedResource = this.environment.resolveRequiredPlaceholders(resource);
         configClass.addImportedResource(resolvedResource, readerClass);
      }
   }

   // Process individual @Bean methods
// @Bean 注解处理.
//org.springframework.context.annotation.ConfigurationClassParser#retrieveBeanMethodMetadata , 
// 这里对于主入口类进来,是没有这个配置的.    
   Set<MethodMetadata> beanMethods = retrieveBeanMethodMetadata(sourceClass);
   for (MethodMetadata methodMetadata : beanMethods) {
      configClass.addBeanMethod(new BeanMethod(methodMetadata, configClass));
   }

   // Process default methods on interfaces
// 对接口的进行处理. 这里目前也是没有的.    
   processInterfaces(configClass, sourceClass);

   // Process superclass, if any
// 先是判断是不是有父类.    
   if (sourceClass.getMetadata().hasSuperClass()) {
// 获取出父类信息       
      String superclass = sourceClass.getMetadata().getSuperClassName();
// 父类不是null,不是java开头并且knownSuperclasses中不存在,就满满足条件.       
      if (superclass != null && !superclass.startsWith("java") &&
            !this.knownSuperclasses.containsKey(superclass)) {
         this.knownSuperclasses.put(superclass, configClass);
         // Superclass found, return its annotation metadata and recurse
         return sourceClass.getSuperClass();
      }
   }

   // No superclass -> processing is complete
   return null;
}
```

**这里可以看到 doProcessConfigurationClass方法,是传入进来主类入口进行解析, 然后没满足一个条件的bean,都会在走一遍解析的方法,直到都走到没满足条件的.**



###### org.springframework.context.annotation.ConfigurationClassUtils#checkConfigurationClassCandidate方法

```java
/**
 * Check whether the given bean definition is a candidate for a configuration class
 * (or a nested component class declared within a configuration/component class,
 * to be auto-registered as well), and mark it accordingly.
 * @param beanDef the bean definition to check
 * @param metadataReaderFactory the current factory in use by the caller
 * @return whether the candidate qualifies as (any kind of) configuration class
 */
public static boolean checkConfigurationClassCandidate(
      BeanDefinition beanDef, MetadataReaderFactory metadataReaderFactory) {
// 先获取 beanName 出来
   String className = beanDef.getBeanClassName();
   if (className == null || beanDef.getFactoryMethodName() != null) {
      return false;
   }

   AnnotationMetadata metadata;
// 判断 bd 是不是AnnotatedBeanDefinition 并且 确认 beanName是不是与前面获取出来的classsName是一样的.    
   if (beanDef instanceof AnnotatedBeanDefinition &&
         className.equals(((AnnotatedBeanDefinition) beanDef).getMetadata().getClassName())) {
      // Can reuse the pre-parsed metadata from the given BeanDefinition...
// 获取类上的注解.我们这里获取出来的是 @Import 和 @ComponentScan       
      metadata = ((AnnotatedBeanDefinition) beanDef).getMetadata();
   }
   else if (beanDef instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) beanDef).hasBeanClass()) {
      // Check already loaded Class if present...
      // since we possibly can't even load the class file for this Class.
      Class<?> beanClass = ((AbstractBeanDefinition) beanDef).getBeanClass();
      if (BeanFactoryPostProcessor.class.isAssignableFrom(beanClass) ||
            BeanPostProcessor.class.isAssignableFrom(beanClass) ||
            AopInfrastructureBean.class.isAssignableFrom(beanClass) ||
            EventListenerFactory.class.isAssignableFrom(beanClass)) {
         return false;
      }
      metadata = AnnotationMetadata.introspect(beanClass);
   }
   else {
      try {
         MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(className);
         metadata = metadataReader.getAnnotationMetadata();
      }
      catch (IOException ex) {
         if (logger.isDebugEnabled()) {
            logger.debug("Could not find class file for introspecting configuration annotations: " +
                  className, ex);
         }
         return false;
      }
   }

 // 获取@Configuration,我们这里没有,所以获取出来的null.   
   Map<String, Object> config = metadata.getAnnotationAttributes(Configuration.class.getName());
   if (config != null && !Boolean.FALSE.equals(config.get("proxyBeanMethods"))) {
      beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_FULL);
   }
// 注意这里的 isConfigurationCandidate方法,org.springframework.context.annotation.ConfigurationClassUtils#isConfigurationCandidate
// @Component/@ComponentScan/@Import/@ImportResource,只要有其中的一种的话，那么返回的就是true. 
   else if (config != null || isConfigurationCandidate(metadata)) {
// CONFIGURATION_CLASS_ATTRIBUTE 对应的值是org.springframework.context.annotation.ConfigurationClassPostProcessor.configurationClass       
      beanDef.setAttribute(CONFIGURATION_CLASS_ATTRIBUTE, CONFIGURATION_CLASS_LITE);
   }
   else {
      return false;
   }

   // It's a full or lite configuration candidate... Let's determine the order value, if any.
// 获取 order,如果有的话,就会set进去.    
   Integer order = getOrder(metadata);
   if (order != null) {
      beanDef.setAttribute(ORDER_ATTRIBUTE, order);
   }

   return true;
}
```

**可以看到这个方法最主的就是对一些类上是否有注解进行判断, 如果满足 @Configuration/@Component/@ComponentScan/@Import/@ImportResource,那么返回的就是会true,同时也会set一个CONFIGURATION_CLASS_ATTRIBUTE属性到bd里面来.**





####  getBean方法分析

​    getBean 不仅仅是获取bean的效果,更是创建bean的，可以看到getBean最后走到了createBean方法来.

​    org.springframework.beans.factory.support.DefaultListableBeanFactory#preInstantiateSingletons : 这里我们直接定位到这个方法,来看下是怎么调用的,调用之前/实例化bean等过程,又做了什么事情？



#####   preInstantiateSingletons 方法

```java
@Override
public void preInstantiateSingletons() throws BeansException {
   if (logger.isTraceEnabled()) {
      logger.trace("Pre-instantiating singletons in " + this);
   }

   // Iterate over a copy to allow for init methods which in turn register new bean definitions.
   // While this may not be part of the regular factory bootstrap, it does otherwise work fine.
// 从 beanDefinitionNames 中获取出 beanName的集合.
// 这里获取出来的 beanNameList 不仅仅有Spring内部的,还有我们自己的.    
   List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

   // Trigger initialization of all non-lazy singleton beans...
   for (String beanName : beanNames) {
      RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
// bd不是抽象的,是单列的,不是赖加载的,就进入到这里来.       
      if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
    // 判断是不是 FactoryBean      
         if (isFactoryBean(beanName)) {
            Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
            if (bean instanceof FactoryBean) {
               final FactoryBean<?> factory = (FactoryBean<?>) bean;
               boolean isEagerInit;
               if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
                  isEagerInit = AccessController.doPrivileged((PrivilegedAction<Boolean>)
                              ((SmartFactoryBean<?>) factory)::isEagerInit,
                        getAccessControlContext());
               }
               else {
                  isEagerInit = (factory instanceof SmartFactoryBean &&
                        ((SmartFactoryBean<?>) factory).isEagerInit());
               }
               if (isEagerInit) {
                  getBean(beanName);
               }
            }
         }
         else {
      // 如果不是 FactroyBean的话,就直接走 getBean方法.       
            getBean(beanName);
         }
      }
   }

   // Trigger post-initialization callback for all applicable beans...
   for (String beanName : beanNames) {
      Object singletonInstance = getSingleton(beanName);
      if (singletonInstance instanceof SmartInitializingSingleton) {
         final SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
         if (System.getSecurityManager() != null) {
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
               smartSingleton.afterSingletonsInstantiated();
               return null;
            }, getAccessControlContext());
         }
         else {
            smartSingleton.afterSingletonsInstantiated();
         }
      }
   }
}
```



###### doGetBean() 方法

```java
/**
 * Return an instance, which may be shared or independent, of the specified bean.
 * @param name the name of the bean to retrieve
 * @param requiredType the required type of the bean to retrieve
 * @param args arguments to use when creating a bean instance using explicit arguments
 * (only applied when creating a new instance as opposed to retrieving an existing one)
 * @param typeCheckOnly whether the instance is obtained for a type check,
 * not for actual use
 * @return an instance of the bean
 * @throws BeansException if the bean could not be created
 */
@SuppressWarnings("unchecked")
protected <T> T doGetBean(final String name, @Nullable final Class<T> requiredType,
      @Nullable final Object[] args, boolean typeCheckOnly) throws BeansException {
// 获取beanName
   final String beanName = transformedBeanName(name);
   Object bean;

   // Eagerly check singleton cache for manually registered singletons.
 //   
   Object sharedInstance = getSingleton(beanName);
   if (sharedInstance != null && args == null) {
      if (logger.isTraceEnabled()) {
         if (isSingletonCurrentlyInCreation(beanName)) {
            logger.trace("Returning eagerly cached instance of singleton bean '" + beanName +
                  "' that is not fully initialized yet - a consequence of a circular reference");
         }
         else {
            logger.trace("Returning cached instance of singleton bean '" + beanName + "'");
         }
      }
//        
      bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
   }

   else {
      // Fail if we're already creating this bean instance:
      // We're assumably within a circular reference.
  // 判断这个bean当前是不是已经在注册了,如果是的话,就会抛出异常来.     
      if (isPrototypeCurrentlyInCreation(beanName)) {
         throw new BeanCurrentlyInCreationException(beanName);
      }

      // Check if bean definition exists in this factory.
 //       
      BeanFactory parentBeanFactory = getParentBeanFactory();
      if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
         // Not found -> check parent.
         String nameToLookup = originalBeanName(name);
         if (parentBeanFactory instanceof AbstractBeanFactory) {
            return ((AbstractBeanFactory) parentBeanFactory).doGetBean(
                  nameToLookup, requiredType, args, typeCheckOnly);
         }
         else if (args != null) {
            // Delegation to parent with explicit args.
            return (T) parentBeanFactory.getBean(nameToLookup, args);
         }
         else if (requiredType != null) {
            // No args -> delegate to standard getBean method.
            return parentBeanFactory.getBean(nameToLookup, requiredType);
         }
         else {
            return (T) parentBeanFactory.getBean(nameToLookup);
         }
      }

      if (!typeCheckOnly) {
         markBeanAsCreated(beanName);
      }

      try {
         final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
         checkMergedBeanDefinition(mbd, beanName, args);

         // Guarantee initialization of beans that the current bean depends on.
         String[] dependsOn = mbd.getDependsOn();
         if (dependsOn != null) {
            for (String dep : dependsOn) {
               if (isDependent(beanName, dep)) {
                  throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                        "Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
               }
               registerDependentBean(dep, beanName);
               try {
                  getBean(dep);
               }
               catch (NoSuchBeanDefinitionException ex) {
                  throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                        "'" + beanName + "' depends on missing bean '" + dep + "'", ex);
               }
            }
         }

         // Create bean instance.
         if (mbd.isSingleton()) {
            sharedInstance = getSingleton(beanName, () -> {
               try {
                  return createBean(beanName, mbd, args);
               }
               catch (BeansException ex) {
                  // Explicitly remove instance from singleton cache: It might have been put there
                  // eagerly by the creation process, to allow for circular reference resolution.
                  // Also remove any beans that received a temporary reference to the bean.
                  destroySingleton(beanName);
                  throw ex;
               }
            });
            bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
         }

         else if (mbd.isPrototype()) {
            // It's a prototype -> create a new instance.
            Object prototypeInstance = null;
            try {
               beforePrototypeCreation(beanName);
               prototypeInstance = createBean(beanName, mbd, args);
            }
            finally {
               afterPrototypeCreation(beanName);
            }
            bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
         }

         else {
            String scopeName = mbd.getScope();
            final Scope scope = this.scopes.get(scopeName);
            if (scope == null) {
               throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
            }
            try {
               Object scopedInstance = scope.get(beanName, () -> {
                  beforePrototypeCreation(beanName);
                  try {
                     return createBean(beanName, mbd, args);
                  }
                  finally {
                     afterPrototypeCreation(beanName);
                  }
               });
               bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
            }
            catch (IllegalStateException ex) {
               throw new BeanCreationException(beanName,
                     "Scope '" + scopeName + "' is not active for the current thread; consider " +
                     "defining a scoped proxy for this bean if you intend to refer to it from a singleton",
                     ex);
            }
         }
      }
      catch (BeansException ex) {
         cleanupAfterBeanCreationFailure(beanName);
         throw ex;
      }
   }

   // Check if required type matches the type of the actual bean instance.
   if (requiredType != null && !requiredType.isInstance(bean)) {
      try {
         T convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
         if (convertedBean == null) {
            throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
         }
         return convertedBean;
      }
      catch (TypeMismatchException ex) {
         if (logger.isTraceEnabled()) {
            logger.trace("Failed to convert bean '" + name + "' to required type '" +
                  ClassUtils.getQualifiedName(requiredType) + "'", ex);
         }
         throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
      }
   }
   return (T) bean;
}
```