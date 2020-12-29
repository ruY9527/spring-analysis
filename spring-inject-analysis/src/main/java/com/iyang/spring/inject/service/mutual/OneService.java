package com.iyang.spring.inject.service.mutual;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * @author Yang
 * 当前服务 : spring-inject-analysis
 * @date 2020/12/29 / 10:42
 */

@Order(value = 1)
@Service
public class OneService {

    @Autowired
    private TwoService twoService;

    public OneService(){
        System.out.println("OneService 无参数构造方法");
    }

    public void say(){
        System.out.println("OneService 中 say 方法");

        twoService.say();
    }

}
