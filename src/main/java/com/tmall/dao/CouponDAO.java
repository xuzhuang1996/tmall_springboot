package com.tmall.dao;

import com.tmall.pojo.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponDAO extends JpaRepository<Coupon,Integer> {
}
