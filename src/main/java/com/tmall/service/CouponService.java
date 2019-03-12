package com.tmall.service;

import com.tmall.dao.CouponDAO;
import com.tmall.pojo.Coupon;
import com.tmall.pojo.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CouponService {
    @Autowired
    CouponDAO couponDAO;

    public Coupon get(int id){
        Optional<Coupon> ProductInfoOptional = couponDAO.findById(id);
        if (!ProductInfoOptional.isPresent()) {
            return null;
        }
        Coupon coupon = ProductInfoOptional.get();
        return coupon;
    }
}
