package com.iyang.spring.inject.service.mutual;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * @author Yang
 * 当前服务 : spring-inject-analysis
 * @date 2020/12/29 / 10:42
 */

@Order(value = 0)
@Service
public class TwoService {

    @Autowired
    private OneService oneService;

    public TwoService(){
        System.out.println("TwoService 无参数构造函数");
    }

    public void say(){
        System.out.println("TwoService 中的 say 方法");

        oneService.say();
    }

}
