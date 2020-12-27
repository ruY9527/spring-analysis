package com.iyang.spring.extend.bd;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Created by Yang on 2020/12/27 23:54
 */

public class ObjectProviderMain {

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(ObjectProviderMain.class);
        context.refresh();

        printStringInfo(context);
        getObjectProviderInfo(context);

        context.close();

    }

    /**
     * This is beanOne & is primary
     * This is beanTwo
     *
     * @param context
     */
    public static void printStringInfo(ApplicationContext context){

        ObjectProvider<String> beanProvider = context.getBeanProvider(String.class);
        beanProvider.stream().forEach(System.out::println);

    }

    /**
     * getObjectProviderInfo 方法中打印的值 : This is beanOne & is primary
     * @param context
     */
    public static void getObjectProviderInfo(ApplicationContext context){
        ObjectProvider<String> beanProvider = context.getBeanProvider(String.class);
        System.out.println("getObjectProviderInfo 方法中打印的值 : " + beanProvider.getObject());
    }

    @Bean
    @Primary
    public String beanOne(){
        return "This is beanOne & is primary";
    }

    @Bean
    public String beanTwo(){
        return "This is beanTwo";
    }

}
