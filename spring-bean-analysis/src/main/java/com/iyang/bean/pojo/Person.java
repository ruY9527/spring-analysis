package com.iyang.bean.pojo;

import org.springframework.context.annotation.Scope;

/**
 * @author Yang
 * 当前服务 : spring-bean-analysis
 * @date 2020/12/25 / 10:07
 */

public class Person {

    private Integer id;
    private String name;

    public void say(){
        System.out.println("调用我说方法");
    }

    public Person(){
        System.out.println("person 调用无参数构造函数");
    }

    public Person(Integer id, String name) {
        System.out.println("person 有参数构造函数");

        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
