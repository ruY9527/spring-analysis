package com.iyang.spring.inject.service;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

/**
 * @author Yang
 * 当前服务 : spring-inject-analysis
 * @date 2020/12/29 / 10:35
 */


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
