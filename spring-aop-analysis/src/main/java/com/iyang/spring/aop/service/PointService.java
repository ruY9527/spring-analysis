package com.iyang.spring.aop.service;

import org.springframework.stereotype.Service;

/**
 * @author Yang
 * 当前服务 : spring-aop-analysis
 * @date 2020/12/30 / 15:41
 */

@Service
public class PointService {

    public String say(){
        System.out.println("调用 PointService 的 say 方法");

        return "1";
    }


}
