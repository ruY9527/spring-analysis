package com.iyang.spring.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Description;
import org.springframework.core.annotation.AliasFor;

/**
 * Created by Yang on 2020/12/23 0:10
 */

// 这里配置需要扫描bean的路径配置.
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
