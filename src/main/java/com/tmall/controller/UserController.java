/**
* 模仿天猫整站 springboot 教程 为 how2j.cn 版权所有
* 本教程仅用于学习使用，切勿用于非法用途，由此引起一切后果与本站无关
* 供购买者学习，请勿私自传播，否则自行承担相关法律责任
*/	

package com.tmall.controller;

import com.tmall.pojo.User;
import com.tmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class UserController {
	@Autowired
    UserService userService;

    @GetMapping("/users")
    public Page<User> list(@RequestParam(value = "start", defaultValue = "0") int start, @RequestParam(value = "size", defaultValue = "10") int size) throws Exception {
    	start = start<0?0:start;
    	Page<User> page = userService.list(start,size);
        return page;
    }

    @GetMapping("/users_all")
    public List<User> list_all() throws Exception {
        return userService.list();
    }
        
}

