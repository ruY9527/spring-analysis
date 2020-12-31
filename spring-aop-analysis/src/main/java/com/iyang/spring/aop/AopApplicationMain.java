package com.iyang.spring.aop;

import com.iyang.spring.aop.aop.PointAop;
import com.iyang.spring.aop.config.ClassConfig;
import com.iyang.spring.aop.service.PointService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Yang
 * 当前服务 : spring-aop-analysis
 * @date 2020/12/30 / 15:39
 */
public class AopApplicationMain {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ClassConfig.class);

        PointService pointService = context.getBean(PointService.class);
        pointService.say();

        NoAopService aopService = context.getBean(NoAopService.class);
        System.out.println(aopService.getClass());

        // com.iyang.spring.aop.service.PointService@11fc564b
        System.out.println(pointService.getClass());



       /* PointAop pointAop = context.getBean(PointAop.class);
        System.out.println(pointAop.toString());*/

    }

}
