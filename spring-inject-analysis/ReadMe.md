##         Spring 注入 bean 分析





#### 前言

​    这里的 Spring 注入分析，和之前的 @Componet/@Import不一样，这里主要是分析我们在 XXXXService 中使用@Autowired 这种类型的注解进行分析, 已经相互之间循环依赖引用给Spring是怎么处理的？Order的顺序初始化又是怎么回是之类的等的分析.



#### 分析

​    这里看到写的二个类 : 分别是

​    com.iyang.spring.inject.service.CommonService

```java
@Service
@DependsOn(value = {"injectService"})
public class CommonService {

    public CommonService(){
        System.out.println("CommonService 无参数构造函数初始化");
    }

    public void say(){
        System.out.println("调用CommonService的say()方法");
    }

}
```



com.iyang.spring.inject.service.InjectService

```java
@Service
public class InjectService {

    @Autowired
    private CommonService commonService;

    public InjectService(){
        System.out.println("InjectService 构造函数初始化");
    }

    public void say(){
        System.out.println("调用InjectService的say()方法");
    }

}
```



可以看到我们在 InjectService 中注入了 CommonService , 但是在 CommonService 中使用@DependsOn注解来依赖injectService(注意,这里是需要写bean的名字的,否则是会有错误的).

打印结果 :  InjectService 构造函数初始化
CommonService 无参数构造函数初始化

可以从打印结果来看,  Spring 是先实例化 InjectService , 在实例化 CommonService 的. 于是我们 debug 来分析下.

这里我们把断点打在 :  org.springframework.context.support.AbstractApplicationContext#finishBeanFactoryInitialization 的 beanFactory.preInstantiateSingletons() 方法上面.

![Spring迭代创建bean](https://raw.githubusercontent.com/baoyang23/images_repository/master/spring/inject/spring_bean_inject_foreach_1.png)



从这里可以看出来 ，按照这个遍历顺序的话,那应该是先初始化 commonService 接着再是injectService ,但是从我们打印出来的log来看,这刚刚是相反的.

那是怎么回是呢？

往下分析看.



#### @Autowired &  @DependsOn 注解分析

  进到我们上图的 debug 来，当迭代到 beanName 是 commonService 的时候，我们就debug跟进往下层看,看看做了上面事情.

  跟进到 org.springframework.beans.factory.support.AbstractBeanFactory#doGetBean 方法来 :

可以看到这里是有一段获取注解的代码,



##### @DependsOn  注解

```java
// Guarantee initialization of beans that the current bean depends on.
// 这里获取出来的值就是 : injectService , 也就是我们在注解里面定义的值.
String[] dependsOn = mbd.getDependsOn();
if (dependsOn != null) {
 // 迭代.   
   for (String dep : dependsOn) {
// org.springframework.beans.factory.support.DefaultSingletonBeanRegistry#dependentBeanMap
// org.springframework.beans.factory.support.DefaultSingletonBeanRegistry#dependenciesForBeanMap
// org.springframework.beans.factory.support.DefaultSingletonBeanRegistry#containedBeanMap
// 这里是有注解三个Map来存储信息,
// 如果判断出来是包含的话,就会抛出异常来.       
      if (isDependent(beanName, dep)) {
         throw new BeanCreationException(mbd.getResourceDescription(), beanName,
               "Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
      }
// 这步就讲我们的 beanName 分别是 : injectSerivce/commonService 分别给添加到二个集合中去.       
      registerDependentBean(dep, beanName);
      try {
 // 传入进去 injectSerivce 来走 getBean() 方法,注意我们走的这步,也是在getBean()方法的,也就说这里是去调用自己了.         
         getBean(dep);
      }
      catch (NoSuchBeanDefinitionException ex) {
         throw new BeanCreationException(mbd.getResourceDescription(), beanName,
               "'" + beanName + "' depends on missing bean '" + dep + "'", ex);
      }
   }
}
```

  这里是可以看到对于 @DependsOn 注解的处理，还是比较容易理解,难度并不是那么大的.



##### @Autowired  注解分析

  这里如果是对 @Autowired   不是很清楚的话,那么我们可以这样干,在我们的 com.iyang.spring.inject.service.CommonService#CommonService无参数构造函数上打上断点,然后跟踪堆栈信息来寻找入口是在哪里.

![Spring_Autowired分析](https://raw.githubusercontent.com/baoyang23/images_repository/master/spring/inject/spring_bean_inject_autwiord_1.png)



比如我这里就根据堆栈跟踪到, 当injectService new完了后,走到这个方法的时候,populateBean走完, 我们的 CommonService 的无参构造方法也就走完了,同时我们的输出语句也跟着打印出来了.



org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#populateBean

所以我们接下来就重点看这个方法，是做了个什么事情呢？

```java
/**
 * Populate the bean instance in the given BeanWrapper with the property values
 * from the bean definition.
 * @param beanName the name of the bean
 * @param mbd the bean definition for the bean
 * @param bw the BeanWrapper with bean instance
 */
@SuppressWarnings("deprecation")  // for postProcessPropertyValues
protected void populateBean(String beanName, RootBeanDefinition mbd, @Nullable BeanWrapper bw) {
    
// 这里先是对 bw 进行非空的判断.    
   if (bw == null) {
      if (mbd.hasPropertyValues()) {
         throw new BeanCreationException(
               mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
      }
      else {
         // Skip property population phase for null instance.
         return;
      }
   }

   // Give any InstantiationAwareBeanPostProcessors the opportunity to modify the
   // state of the bean before properties are set. This can be used, for example,
   // to support styles of field injection.
   boolean continueWithPropertyPopulation = true;

 // 这里是走InstantiationAwareBeanPostProcessor后置处理器的 postProcessAfterInstantiation 方法.  这里并不是对 @Autowired 进行处理的操作.  
   if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
      for (BeanPostProcessor bp : getBeanPostProcessors()) {
         if (bp instanceof InstantiationAwareBeanPostProcessor) {
            InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
            if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
               continueWithPropertyPopulation = false;
               break;
            }
         }
      }
   }

   if (!continueWithPropertyPopulation) {
      return;
   }

   PropertyValues pvs = (mbd.hasPropertyValues() ? mbd.getPropertyValues() : null);

   int resolvedAutowireMode = mbd.getResolvedAutowireMode();
   if (resolvedAutowireMode == AUTOWIRE_BY_NAME || resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
      MutablePropertyValues newPvs = new MutablePropertyValues(pvs);
      // Add property values based on autowire by name if applicable.
      if (resolvedAutowireMode == AUTOWIRE_BY_NAME) {
         autowireByName(beanName, mbd, bw, newPvs);
      }
      // Add property values based on autowire by type if applicable.
      if (resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
         autowireByType(beanName, mbd, bw, newPvs);
      }
      pvs = newPvs;
   }

   boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();
   boolean needsDepCheck = (mbd.getDependencyCheck() != AbstractBeanDefinition.DEPENDENCY_CHECK_NONE);

   PropertyDescriptor[] filteredPds = null;
   if (hasInstAwareBpps) {
      if (pvs == null) {
         pvs = mbd.getPropertyValues();
      }
       
// 这里才是对 @Autowired 注解进行处理的后置处理器.       
// org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor,也就是这个后置处理器的 postProcessProperties 方法. 那么这里可以看看,这个后置处理器的 postProcessProperties方法是做了什么事情.        
      for (BeanPostProcessor bp : getBeanPostProcessors()) {
         if (bp instanceof InstantiationAwareBeanPostProcessor) {
            InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
            PropertyValues pvsToUse = ibp.postProcessProperties(pvs, bw.getWrappedInstance(), beanName);
            if (pvsToUse == null) {
               if (filteredPds == null) {
                  filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
               }
               pvsToUse = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
               if (pvsToUse == null) {
                  return;
               }
            }
            pvs = pvsToUse;
         }
      }
   }
   if (needsDepCheck) {
      if (filteredPds == null) {
         filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
      }
      checkDependencies(beanName, mbd, filteredPds, pvs);
   }

   if (pvs != null) {
      applyPropertyValues(beanName, mbd, bw, pvs);
   }
}
```

可以看到走的 populateBean()  方法， 然后再走AutowiredAnnotationBeanPostProcessor这个后置处理器,这个后置处理器就是对@Autowired注解进行处理的操作.   这里是不是可以扩展下? 如果我自己要写一个类似 @Autowired 的注解的话，那我是不是像 AutowiredAnnotationBeanPostProcessor 这个后置处理器一样,模仿这自己写一个然后调用 beanFactory的getBean 方法即可呢 ?



###### org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#postProcessProperties 后置处理器方法分析

这里可以看到是借助 InjectionMetadata的inject方法.

```java
@Override
public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
   InjectionMetadata metadata = findAutowiringMetadata(beanName, bean.getClass(), pvs);
   try {
      metadata.inject(bean, beanName, pvs);
   }
   catch (BeanCreationException ex) {
      throw ex;
   }
   catch (Throwable ex) {
      throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", ex);
   }
   return pvs;
}
```





###### org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor.AutowiredFieldElement#inject 方法

上步借助 metadata.inject() 方法是走到这里来.

```java
    @Override
   protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
 // 字段信息
      Field field = (Field) this.member;
      Object value;
 // 是否缓存.      
      if (this.cached) {
         value = resolvedCachedArgument(beanName, this.cachedFieldValue);
      }
      else {
// 创建出 DependencyDescriptor 对象来.          
         DependencyDescriptor desc = new DependencyDescriptor(field, this.required);
// 给对象设置 containingClass 属性.          
         desc.setContainingClass(bean.getClass());
         Set<String> autowiredBeanNames = new LinkedHashSet<>(1);
         Assert.state(beanFactory != null, "No BeanFactory available");
// 从beanFactory中获取出TypeConverter,如果没有的话,默认获取出来的就是SimpleTypeConverter
         TypeConverter typeConverter = beanFactory.getTypeConverter();
         try {
// org.springframework.beans.factory.support.DefaultListableBeanFactory#resolveDependency
// org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency, //这里主要看走到了 doResolveDependency 中来.            
// 借助 beanFactory的方法,             
            value = beanFactory.resolveDependency(desc, beanName, autowiredBeanNames, typeConverter);
         }
         catch (BeansException ex) {
            throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(field), ex);
         }
         synchronized (this) {
            if (!this.cached) {
               if (value != null || this.required) {
                  this.cachedFieldValue = desc;
                  registerDependentBeans(beanName, autowiredBeanNames);
                  if (autowiredBeanNames.size() == 1) {
                     String autowiredBeanName = autowiredBeanNames.iterator().next();
                     if (beanFactory.containsBean(autowiredBeanName) &&
                           beanFactory.isTypeMatch(autowiredBeanName, field.getType())) {
                        this.cachedFieldValue = new ShortcutDependencyDescriptor(
                              desc, autowiredBeanName, field.getType());
                     }
                  }
               }
               else {
                  this.cachedFieldValue = null;
               }
               this.cached = true;
            }
         }
      }
      if (value != null) {
         ReflectionUtils.makeAccessible(field);
         field.set(bean, value);
      }
   }
}
```





###### org.springframework.beans.factory.support.DefaultListableBeanFactory#doResolveDependency 方法

该方法可以看到 ,instanceCandidate = descriptor.resolveCandidate(autowiredBeanName, type, this); 这行代码是又去调用 getBean()  方法. 对于getBean 这个方法就不需要多说啦,如果你不深入理解的话,你认为是实例化我们的bean就可以啦.

最后还是走的 org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#doCreateBean 中的instanceWrapper = createBeanInstance(beanName, mbd, args); 这行代码来实例化我们的bean对象.

其实在再次调用doGetBean方法的时候,不清楚大家有没有一个问题 ，那就是@DependsOn 注解的问题，貌似这个是没有用什么集合来存储记录是不是有解析过了一回, 是不是会有一个这样的疑问 ? 

如果是第二次 commonService 进入到 getBean 方法的话, org.springframework.beans.factory.support.DefaultSingletonBeanRegistry#getSingleton(java.lang.String, boolean) , 走到这里会获取出来 instance来,也就是用这个方法就控制住了.

```java
@Nullable
public Object doResolveDependency(DependencyDescriptor descriptor, @Nullable String beanName,
      @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {
// 获取出来是 null
   InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
   try {
// 这里的shortcut获取出来的也是null       
      Object shortcut = descriptor.resolveShortcut(this);
      if (shortcut != null) {
         return shortcut;
      }
// type 就是对应我们注入进来的 commonService.
      Class<?> type = descriptor.getDependencyType();
// 获取出来的value是null.       
      Object value = getAutowireCandidateResolver().getSuggestedValue(descriptor);
      if (value != null) {
         if (value instanceof String) {
            String strVal = resolveEmbeddedValue((String) value);
            BeanDefinition bd = (beanName != null && containsBean(beanName) ?
                  getMergedBeanDefinition(beanName) : null);
            value = evaluateBeanDefinitionString(strVal, bd);
         }
         TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
         try {
            return converter.convertIfNecessary(value, type, descriptor.getTypeDescriptor());
         }
         catch (UnsupportedOperationException ex) {
            // A custom TypeConverter which does not support TypeDescriptor resolution...
            return (descriptor.getField() != null ?
                  converter.convertIfNecessary(value, type, descriptor.getField()) :
                  converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
         }
      }

// 判断是否有多个bean,这里获取出来的也是null.       
      Object multipleBeans = resolveMultipleBeans(descriptor, beanName, autowiredBeanNames, typeConverter);
      if (multipleBeans != null) {
         return multipleBeans;
      }
// 先是获取出被注入进去的bean的名字, 接着判断是不是自己引用自己&判断是不是@Autowire.
// 最后返回的Map的,key 是 beanName , value 就是对应这个 Object 的 class.       
      Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
      if (matchingBeans.isEmpty()) {
         if (isRequired(descriptor)) {
            raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
         }
         return null;
      }

      String autowiredBeanName;
      Object instanceCandidate;

// 长度大于1.       
      if (matchingBeans.size() > 1) {
         autowiredBeanName = determineAutowireCandidate(matchingBeans, descriptor);
         if (autowiredBeanName == null) {
            if (isRequired(descriptor) || !indicatesMultipleBeans(type)) {
               return descriptor.resolveNotUnique(descriptor.getResolvableType(), matchingBeans);
            }
            else {
               // In case of an optional Collection/Map, silently ignore a non-unique case:
               // possibly it was meant to be an empty collection of multiple regular beans
               // (before 4.3 in particular when we didn't even look for collection beans).
               return null;
            }
         }
         instanceCandidate = matchingBeans.get(autowiredBeanName);
      }
      else {
// 长度不是大于1,也就是说0/1的情况.          
         // We have exactly one match.
         Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
// commonService          
         autowiredBeanName = entry.getKey();
// commonService 对应的 CommonService的类信息.          
         instanceCandidate = entry.getValue();
      }
//添加到autowiredBeanNames 集合中来. 
      if (autowiredBeanNames != null) {
         autowiredBeanNames.add(autowiredBeanName);
      }
      if (instanceCandidate instanceof Class) {
// org.springframework.beans.factory.config.DependencyDescriptor#resolveCandidate,这里传入的this是一个beanFactory工厂,该方法内部直接走的 beanFactory.getBean(beanName); 也就是直接调用了的getBean()方法. 可以看到该方法走完,我们的CommonService的无参构造函数中的输出语句就打印出来了.
         instanceCandidate = descriptor.resolveCandidate(autowiredBeanName, type, this);
      }
// 将返回回来的 bean 实例给赋值给 result.       
      Object result = instanceCandidate;
// 判断 result 是不是 NullBean 类型.       
      if (result instanceof NullBean) {
         if (isRequired(descriptor)) {
            raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
         }
         result = null;
      }
      if (!ClassUtils.isAssignableValue(type, result)) {
         throw new BeanNotOfRequiredTypeException(autowiredBeanName, type, instanceCandidate.getClass());
      }
      return result;
   }
   finally {
// 最后清除 org.springframework.beans.factory.support.ConstructorResolver#currentInjectionPoint,currentInjectionPoint也是一个ThreadLocal,只不过Spring自己包装了一下.       
      ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
   }
}
```

这里是可以看到在该方法中,这行代码beanFactory.resolveDependency,还是去调用了beanFactory的getBean方法,调用了这个方法就是简单的理解为实例化我们的bean对象,当然,也不仅仅是实例化bean这么简单,在实例化前后都是有相应的处理操作的.



#### @Autowired 相互依赖

​		早些时候,Spring之间的相互依赖是个很有名气的问题,因为最初的时候好像还是一个bug。从第一张图来看的话,显示实例化 oneService , 再是实例化 twoService , 但是在 oneService 实例化完了后, 在走populateBean方法的时候，会将twoService给注入进来, 然后twoService又会去调用getBean()方法，发现又需要注入oneService进来，又去根据oneService来调用getBean方法,那么就陷入了一个死循环了,这肯定是不行的，那么我们看看Spring是怎么处理的, 当然啦,现在肯定早就没这个问题了.

​     其实按照上面的理解的话,最后是不是都要走到 getBean() 方法中来, 所以文章都是在 getBean() 方法里面.

​	org.springframework.beans.factory.support.AbstractBeanFactory#doGetBean  看到这个方法.

```
protected <T> T doGetBean(final String name, @Nullable final Class<T> requiredType,
      @Nullable final Object[] args, boolean typeCheckOnly) throws BeansException {

   final String beanName = transformedBeanName(name);
   Object bean;

   // Eagerly check singleton cache for manually registered singletons.
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
      bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
   }
```



这里贴上二张debug的图片,大家可能会比较容易理解啦.



![Spring_mutual_one](https://raw.githubusercontent.com/baoyang23/images_repository/master/spring/inject/Spring_mutual_one.png)



这张是 oneService 第一次进 doGetBean 方法.



![Spring_mutual_two](https://raw.githubusercontent.com/baoyang23/images_repository/master/spring/inject/Spring_mutual_two.png)

 这张是 oneService 第二次进 doGetBean 方法. 



可以从这二张图来看,在 Object sharedInstance = getSingleton(beanName); 返回返回的sharedInstance的对象,第一次是没有值的,就会走createBean() 方法,  第二次因为有值, 就不会再走createBean() 方法，所以就不会陷入我们上面所提到的一直陷入死循环了.  



那么我们就看下这个 getSingleton 方法里面走了什么事情.

这里直接定位到我们接下来要走的方法中来.

```java
/**
 * Return the (raw) singleton object registered under the given name.
 * <p>Checks already instantiated singletons and also allows for an early
 * reference to a currently created singleton (resolving a circular reference).
 * @param beanName the name of the bean to look for
 * @param allowEarlyReference whether early references should be created or not
 * @return the registered singleton object, or {@code null} if none found
 */
@Nullable
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
   Object singletonObject = this.singletonObjects.get(beanName);
// 这里获取出来的 singletonObject 是null.     
// isSingletonCurrentlyInCreation方法是从org.springframework.beans.factory.support.DefaultSingletonBeanRegistry#singletonsCurrentlyInCreation集合中判断是不是正在创建,这里很明显判断到是有,所以就是满足条件的.    
   if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
      synchronized (this.singletonObjects) {
// 再从 earlySingletonObjects 集合中通过 beanName 来确定有没有值.          
         singletonObject = this.earlySingletonObjects.get(beanName);
// earlySingletonObjects 中没有并且 allowEarlyReference 是 true,就会满足条件进入进来.          
         if (singletonObject == null && allowEarlyReference) {
// 获取出来的 ObjectFactory 是 org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.
// org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#getEarlyBeanReference             
            ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
            if (singletonFactory != null) {
 // 这里是获取出 OneService , 也就是我们的 beanName 对用的实例对象.               
               singletonObject = singletonFactory.getObject();
 // 放入到 earlySingletonObjects 中来.               
               this.earlySingletonObjects.put(beanName, singletonObject);
               this.singletonFactories.remove(beanName);
            }
         }
      }
   }
   return singletonObject;
}
```

  

**所以Spring 的 doGetBean() 方法，利用 HashMap等来做了一层bean创建的缓存，当循环依赖的时候，第二次再进入到 doGetBean() 方法的时候，会根据   Object sharedInstance = getSingleton(beanName) 方法返回的 sharedInstance  的是否有值，如果有值的话,就说明已经初始化了,如果没有值的话,就说明是第一次初始化,所以得走createBean方法.**



#### 总结

   这里分析 @DependsOn 这个注解是比较好理解的, 这里是在实例化这个对象之前,先实例化依赖的bean.

   @Autowired 这个注解，是借助了后置处理器，注意，这里是在是实例化当前实例化对象之后.

   可以感受到 Spring 提供的后置处理器, 都是各司其职,我们后面可以定义自己的后置处理器来进行分析操作.