package com.iyang.spring;

import com.iyang.spring.config.YangBeanScannerConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by Yang on 2020/12/23 0:10
 */

public class InitWorkFlowSpring {


    public static void main(String[] args) {

        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(YangBeanScannerConfig.class);
        YangBeanScannerConfig yangBeanScannerConfig = context.getBean(YangBeanScannerConfig.class);
        yangBeanScannerConfig.say();
    }

}
