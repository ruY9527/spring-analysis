## 					Spring中AOP的知识阅读分析.



#### 题记

   Spring Aop  是一个我们在使用 Spring 过程中必须掌握的知识. Aop 是一种编程,并不是 Spring 特有的，在其他的语言中也是有的.  但是 阅读 Spring Aop 的源码是很有必要的. 

   宁外如果有错误的地方,还请大家指出修正&改正.



#### 运行结果

  对比于之前，这里我们引入上 aop 和 aspectjweaver 的依赖即可.

```java
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aop</artifactId>
    <version>5.2.0.RELEASE</version>
</dependency>

<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.8.9</version>
</dependency>
```





这里看下，一个是使用了 Aop 的 Service 和 一个没有使用 Aop 的 Service ， 打印出 class 地址信息来看看,就会比较清晰明白.  从结果可以看， PointService是经过了代理的, NoAopService 是没有使用代理的. 而且还可以明显的看到是使用了 Cglib 代理.

所以这里是可以看到，没有什么拦截之类的操作，很多同学可能会误以为有什么拦截操作，其实是没有的. 是使用的cglib来增强代理类的.

```java
public class AopApplicationMain {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ClassConfig.class);

        PointService pointService = context.getBean(PointService.class);
        System.out.println(pointService.getClass());

        NoAopService aopService = context.getBean(NoAopService.class);
        System.out.println(aopService.getClass());
    }        
}        
```

class com.iyang.spring.aop.service.PointService$$EnhancerBySpringCGLIB$$f87607a7
class com.iyang.spring.aop.NoAopService



#### 分析

​	上面已经看到了结果，那就从结果处来进行分析.  Spring 是在什么时候来对 PointService 来进行增强的？是利用cglib来增强的.  

​      com.iyang.spring.aop.config.ClassConfig , 我们可以看到 该类上面有一个注解 :  @EnableAspectJAutoProxy(exposeProxy = true) , 从这个注解开始分析.

​    org.springframework.context.annotation.EnableAspectJAutoProxy  从该注解上可以看到 : @Import(AspectJAutoProxyRegistrar.class) , 又使用了 @Import 来导入进来了一个 bean.

  

该类实现了 ImportBeanDefinitionRegistrar 接口，也就是重写了registerBeanDefinitions方法，注册bd进来.

其实这里注册进来的 AnnotationAwareAspectJAutoProxyCreator 信息, 可以往上面看，该类是有实现SmartInstantiationAwareBeanPostProcessor接口的，也就是说，这里是注册了一个 BeanPostProceesor后置处理器进来了.

关于 BeanPostProcessor 后置处理器的，可以参看之前的文章，是有对其进行讲到说明的. [参考地址](http://www.lwfby.cn/2020/12/29/spring/spring-refresh-work-flow/)

```java
class AspectJAutoProxyRegistrar implements ImportBeanDefinitionRegistrar {

   /**
    * Register, escalate, and configure the AspectJ auto proxy creator based on the value
    * of the @{@link EnableAspectJAutoProxy#proxyTargetClass()} attribute on the importing
    * {@code @Configuration} class.
    */
   @Override
   public void registerBeanDefinitions(
         AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

//  这里注册进来的就是 AnnotationAwareAspectJAutoProxyCreator.class       
      AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);

//这里获取出 @EnableAspectJAutoProxy 注解的信息.       
      AnnotationAttributes enableAspectJAutoProxy =
            AnnotationConfigUtils.attributesFor(importingClassMetadata, EnableAspectJAutoProxy.class);
      if (enableAspectJAutoProxy != null) {
// 分别获取出注解对应的属性  proxyTargetClass / exposeProxy ,
// 如果是 true 的话,就会调用想用的方法.
         if (enableAspectJAutoProxy.getBoolean("proxyTargetClass")) {
            AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
         }
         if (enableAspectJAutoProxy.getBoolean("exposeProxy")) {
            AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
         }
      }
   }

}
```





org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#getAdvicesAndAdvisorsForBean 这里我们在这个方法上打上断点, 直到 beanName 是 pointService ，就可以看到我们想要的work flow流程啦.

org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#initializeBean(java.lang.String, java.lang.Object, org.springframework.beans.factory.support.RootBeanDefinition)     ---->  	org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#applyBeanPostProcessorsAfterInitialization     --->  org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#postProcessAfterInitialization  ----->  org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#getAdvicesAndAdvisorsForBean ,  可以看到进入到  getAdvicesAndAdvisorsForBean 方法所经历的堆栈信息.



```java
/**
 * Wrap the given bean if necessary, i.e. if it is eligible for being proxied.
 * @param bean the raw bean instance
 * @param beanName the name of the bean
 * @param cacheKey the cache key for metadata access
 * @return a proxy wrapping the bean, or the raw bean instance as-is
 */
protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
// 集合 targetSourcedBeans 中已经包含了 beanName的话,那直接返回.    
   if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
      return bean;
   }
// 如果 advisedBeans 中已经包含了 beanName 并且是False的话,就说明是已经处理过了的,并没有走代理的.  
   if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
      return bean;
   }
// bean 是接口 或者 判断出是元对象的话,这里就直接put一个false进去，也返回bean回去.    
   if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
      this.advisedBeans.put(cacheKey, Boolean.FALSE);
      return bean;
   }

   // Create proxy if we have advice.
// 对 bean 来与 Aop 中定义的 pointCut 来集合判断,如果返回的是 DO_NOT_PROXY 的话,就说明是没有代理的.如果返回的不是 DO_NOT_PROXY 的话,那就是说明是要有代理的.    
   Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
   if (specificInterceptors != DO_NOT_PROXY) {
  // 这里如果是走的代理的话,那么存入到 advisedBeans 中的信息就会是 true.     
      this.advisedBeans.put(cacheKey, Boolean.TRUE);
       
// 调用 createProxy 方法.
// 借助 ProxyFactory等来生成代理对象,这里如果你是开启的debug模式的话,你就可以很明显的看到,proxy对用的class类信息的话,是很明显的you EnhancerBySpringCGLIB$$ 等信息的.       
      Object proxy = createProxy(
            bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
// 存入到 proxyTypes 中来.       
      this.proxyTypes.put(cacheKey, proxy.getClass());
// 返回       
      return proxy;
   }

   this.advisedBeans.put(cacheKey, Boolean.FALSE);
   return bean;
}
```

 所以这个方法是可以看到 createProxy 方法的，也就是该方法来对我们的目标类来进行 Cglib 代理的.





该方法可以理解为是对 beanClass 与 Aop 中定义的 @PointCut配置的切点信息是否满足条件，如果是满足条件的话，就说明这个类是要生成代理对象的. 如果是没有满足条件的话，那么就说明这个类是不需要生成代理对象的.

```java
@Override
@Nullable
protected Object[] getAdvicesAndAdvisorsForBean(
      Class<?> beanClass, String beanName, @Nullable TargetSource targetSource) {
//先是将我们在 Aop 定义的 @PointCut 和 execution 给封装成为 org.springframework.aop.aspectj.annotation.InstantiationModelAwarePointcutAdvisorImpl,这里是有封装成为三个的.
// 利用 ThreadLocal + beanName 来标记当前创建的代理对象.
// 借助 org.springframework.aop.support.AopUtils#findAdvisorsThatCanApply , 也就是借助AopUtis来匹配当前传入进来的 bean 信息 与 Aop 中的切点信息是否相匹配.
// 最后排序下 List<Advisor>    
   List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
// 如果翻回来的 advisors 是空的话,那么就说明是不需要代理的.    
   if (advisors.isEmpty()) {
      return DO_NOT_PROXY;
   }
// 否则就转化为数组.    
   return advisors.toArray();
}
```





#### 总结

   所以可以看到，Spring-Aop 是做了什么事情呢 ? 当我们初始化我们的 bean 的时候，Spring 利用注册进来的AnnotationAwareAspectJAutoProxyCreator后置处理器, 然后在这个后置处理器中,我们来对与 aop 可以匹配上的 bean 来进行增强处理，也就是走 代理. 