package com.iyang.spring.inject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @author Yang
 * 当前服务 : spring-inject-analysis
 * @date 2020/12/29 / 10:36
 */

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
