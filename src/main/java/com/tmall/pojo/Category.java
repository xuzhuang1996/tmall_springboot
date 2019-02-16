package com.tmall.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;


// 前后端交互时，Category 对象就会被转换为 json 数据。在 jpa 工作过程中，就会创造代理类来继承 Category ，
// 并添加 handler 和 hibernateLazyInitializer 这两个无须json化的属性，所以这里需要用JsonIgnoreProperties把这两个属性忽略掉。
@Entity
@Table(name = "category")//对应的表名是 category
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//表明自增长方式
    @Column(name = "id")//表明对应的数据库字段名。这里是从数据库中取值进行的增长
    int id;

    String name;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
