package com.iyang.spring.inject.pojo;

/**
 * @author Yang
 * 当前服务 : spring-inject-analysis
 * @date 2020/12/29 / 10:35
 */
public class Student {

    private Integer id;
    private String name;

    public Student(){
        System.out.println("Student 无参数构造初始化");
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
}
