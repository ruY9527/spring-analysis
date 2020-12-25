package com.iyang.bean.bd;

import com.iyang.bean.pojo.Person;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Yang
 * 当前服务 : spring-bean-analysis
 * @date 2020/12/25 / 10:33
 */

@Order
@Import(ImportBeanConfigMain.ImportConfig.class)
@ComponentScan(basePackages = "com.iyang.bean.bd")
public class ImportBeanConfigMain {

    public ImportBeanConfigMain(){
        System.out.println("ImportBeanConfigMain 无参数构造函数");
    }

    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(ImportBeanConfigMain.class);
        context.refresh();

        ImportConfig importConfig = context.getBean(ImportConfig.class);
        Person person = context.getBean(Person.class);
        AnnotConfig annotConfig = context.getBean(AnnotConfig.class);
        ExternalConfig externalConfig = context.getBean(ExternalConfig.class);

        System.out.println(importConfig);
        System.out.println(person);
        System.out.println(annotConfig);
        System.out.println(externalConfig);

    }

    /**
     * 通过 @Import 导入进来.
     */
    public class ImportConfig{

        public void importMe(){
            System.out.println("这是导入自己的方法");
        }

        @Override
        public String toString() {
            return "ImportConfig 的 toString 方法";
        }

        public ImportConfig(){
            System.out.println("ImportConfig无参数构造函数");
        }
        /**
         * 使用 @Bean 注解 注入 Bean 进来.
         * @return
         */
        @Bean
        public Person importPerson(){
            return new Person(9527,"GavinYang");
        }

    }

    @Component
    public static class AnnotConfig{


        public AnnotConfig(){
            System.out.println("AnnotConfig无参数构造函数");
        }

        @Override
        public String toString() {
            return "使用注解来注入bean进来.";
        }
    }

}

@Component
class ExternalConfig {

    public ExternalConfig(){
        System.out.println("externalConfig 无参构造函数");
    }

    @Override
    public String toString() {
        return "externalConfig 打印 toString() 方法";
    }
}
