package com.iyang.spring.aop;

import com.iyang.spring.aop.config.ClassConfig;
import com.iyang.spring.aop.service.PointService;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

/**
 * @author Yang
 * 当前服务 : spring-aop-analysis
 * @date 2020/12/30 / 15:39
 */
public class AopApplicationMain {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ClassConfig.class);

        PointService pointService = context.getBean(PointService.class);
        System.out.println(pointService.getClass());

        NoAopService aopService = context.getBean(NoAopService.class);
        System.out.println(aopService.getClass());

        pointService.say();



        // com.iyang.spring.aop.service.PointService@11fc564b


        // .getBeanPostProcessors()
        AbstractBeanFactory beanFactory = (AbstractBeanFactory) context.getBeanFactory();
        List<BeanPostProcessor> beanPostProcessors = beanFactory.getBeanPostProcessors();

        System.out.println(beanPostProcessors.size() + " : 个数");
        beanPostProcessors.forEach(beanPostProcessor -> {
            System.out.println(beanPostProcessor.getClass().toString());
        });
        System.out.println("  ----    分割线 - ------");


       /* PointAop pointAop = context.getBean(PointAop.class);
        System.out.println(pointAop.toString());*/

    }

}
