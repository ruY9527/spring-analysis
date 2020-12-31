package com.iyang.spring.aop.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author Yang
 * 当前服务 : spring-aop-analysis
 * @date 2020/12/30 / 16:07
 */

@ComponentScan(basePackages = "com.iyang.spring.aop")
// 注意这个注解,如果不开启这个注解的话,那么aop是不会生效的.
// 强烈要求注意，还浪费我十几分钟.
@EnableAspectJAutoProxy(exposeProxy = true)
public class ClassConfig {
}
