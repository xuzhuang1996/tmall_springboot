/**
* 模仿天猫整站 springboot 教程 为 how2j.cn 版权所有
* 本教程仅用于学习使用，切勿用于非法用途，由此引起一切后果与本站无关
* 供购买者学习，请勿私自传播，否则自行承担相关法律责任
*/	

package com.tmall.service;


import com.tmall.dao.UserDAO;
import com.tmall.pojo.Coupon;
import com.tmall.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@CacheConfig(cacheNames="users")
public class UserService {
	
	@Autowired
	UserDAO userDAO;
	@Autowired
	CouponService couponService;

	//返回所有用户
	public List<User> list() {
		//首先创建一个 Sort 对象，表示通过 id 倒排序， 然后通过 categoryDAO进行查询
		Sort sort = new Sort(Sort.Direction.DESC, "id");
		return userDAO.findAll(sort);
	}


	public boolean isExist(String name) {
		//UserService userService = SpringContextUtil.getBean(UserService.class);
		User user = getByName(name);
		return null!=user;
	}

	//@Cacheable(key="'users-one-name-'+ #p0")
	public User getByName(String name) {
		return userDAO.findByName(name);
	}
	
	

	//@Cacheable(key="'users-one-name-'+ #p0 +'-password-'+ #p1")
	public User get(String name, String password) {
		return userDAO.getByNameAndPassword(name,password);
	}

	//@Cacheable(key="'users-page-'+#p0+ '-' + #p1")
	public Page<User> list(int start, int size) {
		Sort sort = new Sort(Sort.Direction.DESC, "id");
		Pageable pageable = PageRequest.of(start, size, sort);
		return userDAO.findAll(pageable);
	}

	//@CacheEvict(allEntries=true)
	@Transactional
	public void add(User user) {
		userDAO.save(user);
		//int a = 10/0;//加事务后，模拟异常后回滚。

	}

	public User get(int id) {
		Optional<User> UserInfoOptional = userDAO.findById(id);
		if (!UserInfoOptional.isPresent()) {
			return null;
		}
		return UserInfoOptional.get();
	}

	public void Add(User user){
		add(user);
	}

	@Cacheable(key=" 'users-'+ #p0 + ':coupon-'+ #p1 ",value = "UserCouponInfo" )//这里就是头空间，而不是user为头空间
	public void AddCoupon(int userId,int couponId){
		User user = get(userId);
		Coupon coupon = couponService.get(couponId);
		List<Coupon> coupons = user.getCoupons();
		if(coupons==null){
			user.setCoupons(new ArrayList<>());
		}

		for (int i = 0; i < coupons.size(); i++) {
			if(coupons.get(i).getId()==couponId)
				return;
		}
		coupons.add(coupon);
		userDAO.save(user);//如果过期的时候，没有删掉，那么再添加进去就不好用了。因为很难判断是否拥有同一张优惠券,其实可以重写equles
	}

	public void DeleteCoupon(int userId,int couponId){
		User user = get(userId);
		Coupon coupon = couponService.get(couponId);

		List<Coupon> coupons = user.getCoupons();
		if(coupons==null){
			return;
		}
		int coupon_id=0;
		for (int i = 0; i < coupons.size(); i++) {
			if(coupons.get(i).getId()==coupon.getId())
				coupon_id = i;
		}
		coupons.remove(coupon_id);
		userDAO.save(user);
	}

}


