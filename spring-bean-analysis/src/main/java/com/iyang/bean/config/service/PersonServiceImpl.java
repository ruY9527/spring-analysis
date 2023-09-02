package com.iyang.bean.config.service;

import org.springframework.stereotype.Service;

/***
 * @author: yang_bao
 * @date: 2023/9/3
 * @desc:
 ***/

@Service
public class PersonServiceImpl {

    public PersonServiceImpl(){

        System.out.println("PersonServiceImpl 无参数构造方法");
    }



    public void insertPerson(){

        System.out.println("模拟插入数据");

    }


}
