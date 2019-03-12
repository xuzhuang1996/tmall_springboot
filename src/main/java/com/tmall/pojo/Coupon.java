package com.tmall.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.awt.print.Book;
import java.util.*;

//多对多关系中一般不设置级联保存、级联删除、级联更新等操作
//可以随意指定一方为关系维护端
//多对多关系的绑定由关系维护端来完成,多对多关系的解除由关系维护端来完成https://blog.csdn.net/johnf_nash/article/details/80642581
@Entity
@Table(name = "coupon")
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//表明自增长方式
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;
    //(mappedBy = "couponList")如果报错试着删这里
//    @ManyToMany(mappedBy = "couponList",cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY,
//            targetEntity = User.class)
//    @JoinTable(
//            name="user_coupon",
//            joinColumns=@JoinColumn(name="coupon_id"),
//            inverseJoinColumns=@JoinColumn(name="user_id"))
//    private List<User> userList;//一张优惠券被多个用户使用

    @ManyToMany(mappedBy = "coupons")
    private List<User> users = new ArrayList<>();

    @Column(name = "expire_Date")
    private Date expire_Date;

    @Column(name = "promoteMoney")
    private float promoteMoney;

    public float getPromoteMoney() {
        return promoteMoney;
    }

    public void setPromoteMoney(float promoteMoney) {
        this.promoteMoney = promoteMoney;
    }

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

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public Date getExpire_Date() {
        return expire_Date;
    }

    public void setExpire_Date(Date expire_Date) {
        this.expire_Date = expire_Date;
    }
}
