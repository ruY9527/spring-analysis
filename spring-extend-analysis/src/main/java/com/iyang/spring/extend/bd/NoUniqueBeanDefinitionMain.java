package com.iyang.spring.extend.bd;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Created by Yang on 2020/12/27 23:48
 */

public class NoUniqueBeanDefinitionMain {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext();
        context.register(NoUniqueBeanDefinitionMain.class);
        context.refresh();

        // 这里如果是根据类型来获取的话,就是会下面的error错误的.
        // Exception in thread "main" org.springframework.beans.factory.NoUniqueBeanDefinitionException:
        // No qualifying bean of type 'java.lang.String' available:
        // expected single matching bean but found 3: beanOne,beanTwo,beanThree
        // String bean = context.getBean(String.class);

        // 如果是名字来获取的话,是没有任何问题的.
        Object beanOne = context.getBean("beanOne");
        if (beanOne instanceof String) {
            System.out.println(beanOne.toString());
        }

        context.close();

    }

    @Bean
    public String beanOne(){
        return "This is beanOne";
    }

    @Bean
    public String beanTwo(){
        return "This is beanTwo";
    }

    @Bean
    public String beanThree(){
        return "Ths is beanThree";
    }


}
