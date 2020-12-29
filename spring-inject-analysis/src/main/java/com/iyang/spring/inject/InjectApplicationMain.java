package com.iyang.spring.inject;

import com.iyang.spring.inject.config.SpringScannerConfig;
import com.iyang.spring.inject.service.InjectService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Yang
 * 当前服务 : spring-inject-analysis
 * @date 2020/12/29 / 10:31
 */
public class InjectApplicationMain {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringScannerConfig.class);
        /*InjectService injectService = context.getBean(InjectService.class);
        injectService.say();*/


    }

}
