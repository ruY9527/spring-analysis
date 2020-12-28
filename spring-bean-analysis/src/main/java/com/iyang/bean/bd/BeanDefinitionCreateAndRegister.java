package com.iyang.bean.bd;

import com.iyang.bean.pojo.Person;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Yang
 * 当前服务 : spring-bean-analysis
 * @date 2020/12/25 / 10:07
 *
 *  通过手动创建 bd , 调用 set 方法给注入到 Spring 容器中.
 */
public class BeanDefinitionCreateAndRegister {

    public static void main(String[] args) {

        // 1 : 通过 BeanDefinitionBuilder 来创建 bd
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(Person.class);
        beanDefinitionBuilder.addPropertyValue("id",9527).addPropertyValue("name","GavinYang");
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();

        // 2 : 通过 new GenericBeanDefinition 来创建 bd.
        GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
        genericBeanDefinition.setBeanClass(Person.class);
        MutablePropertyValues mutablePropertyValues = new MutablePropertyValues();
        mutablePropertyValues.add("id",1).add("name","Peterwong");
        genericBeanDefinition.setPropertyValues(mutablePropertyValues);

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        // 这里是给 bd 给注册到 Spring 容器里面来.
        // context.registerBeanDefinition("person",beanDefinition);
        context.registerBeanDefinition("peterwong",genericBeanDefinition);

        // 如果这里不调用 refresh 是会有错误的.
        context.refresh();

        Person person = context.getBean(Person.class);
        person.say();
        System.out.println(person.toString());

    }

}
