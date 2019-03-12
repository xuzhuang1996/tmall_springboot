package com.tmall.service;

import com.tmall.pojo.Coupon;
import com.tmall.pojo.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserService userService;
    @Autowired
    CouponService couponService;

    @Test
    public void addCoupon() {
//        User user = userService.getByName("xx");
//        Coupon coupon = couponService.get(1);
//        userService.AddCoupon(user,coupon);

//        User user = userService.getByName("xx");//这样他自动就拿到了。
//        Coupon coupon = couponService.get(1);
//          userService.AddCoupon(2,1);
//        userService.AddCoupon(2,1);
//        String s = "users::users-2:coupon-1";
//        //int first = s.indexOf(":");
//        int last = s.lastIndexOf(":");
//        String sb2 = s.substring(last);
//        String sb1 = s.substring(0,last);
//        sb1= sb1.replaceAll("[^(0-9)]","");
//        sb2=sb2.replaceAll("[^(0-9)]","");
//        System.out.println(sb1+"-"+sb2);
    }
}