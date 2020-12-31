package com.iyang.spring.aop.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author Yang
 * 当前服务 : spring-aop-analysis
 * @date 2020/12/30 / 15:41
 */

@Component
@Aspect
public class PointAop {

    public PointAop(){


        System.out.println("PointAop 无参数构造函数");
    }

    /**
     * 定义切点.
     */
    @Pointcut("execution(* com.iyang.spring.aop.service..*.*(..))")
    public void pointCut(){}

    @Before(value = "execution(public * com.iyang.spring.aop.service..*.*(..))")
    public void callBefore(JoinPoint joinPoint){
        System.out.println("调用aop前置处理器");
    }

    @After("pointCut()")
    public void doAfter(){
        System.out.println("调用doAfter方法");
    }

    //@Around(value = "execution(* com.iyang.spring.aop.service..*.*(..))")
    @Around("pointCut()")
    public void callAround(ProceedingJoinPoint pjp){
        System.out.println("调用callAround方法");
        try{
            pjp.proceed();
        }catch (Throwable e){

        }

    }

}
