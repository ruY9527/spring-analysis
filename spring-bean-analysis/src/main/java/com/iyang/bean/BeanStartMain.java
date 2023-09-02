package com.iyang.bean;

import com.iyang.bean.config.YangScanConfig;
import com.iyang.bean.config.service.PersonServiceImpl;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by Yang on 2020/12/25 0:43
 */

public class BeanStartMain {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext ac =
                new AnnotationConfigApplicationContext(YangScanConfig.class);


        PersonServiceImpl personService = ac.getBean(PersonServiceImpl.class);
        personService.insertPerson();

    }

}
