package com.iyang.spring.aop;

import org.springframework.stereotype.Service;

/**
 * @author Yang
 * 当前服务 : spring-aop-analysis
 * @date 2020/12/31 / 14:31
 */

@Service
public class NoAopService {

    public void say(){
        System.out.println("NoAopService 的 say 方法");
    }


}
