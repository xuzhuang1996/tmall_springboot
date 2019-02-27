package com.tmall.controller;


import com.tmall.dto.Result;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpSession;


@Controller
public class ForePageController {
    @GetMapping(value="/")
    public String index(){
        return "redirect:home";
    }
    @GetMapping(value="/home")
    public String home(){
        return "fore/home";//这里其实已经跳转到home.html页面，但是如果页面没有值的话，就会报解析错误。
    }

    @GetMapping(value="/register")
    public String register(){
        return "fore/register";//这里其实已经跳转到register.html页面，但是如果页面没有值的话，就会报解析错误。
    }
    @GetMapping(value="/registerSuccess")
    public String registerSuccess(){
        return "fore/registerSuccess";//这里其实已经跳转到register.html页面，但是如果页面没有值的话，就会报解析错误。
    }

    @GetMapping(value="/login")
    public String login(){
        return "fore/login";//这里其实已经跳转到register.html页面，但是如果页面没有值的话，就会报解析错误。
    }
    @GetMapping("/forelogout")
    public String logout(HttpSession session ) {
        session.removeAttribute("user");
        return "redirect:home";
    }

    //就算有参数pid，也不用管，因为是转发，因此还在URL中
    @GetMapping("/product")
    public String product( ) {
        return "fore/product";
    }

    @GetMapping("/category")
    public String category(){
        return "fore/category";
    }

    @GetMapping("/search")
    public String search(){
        return "fore/search";
    }

    @GetMapping("/buy")
    public String buy(){
        return "fore/buy";
    }

    @GetMapping("/cart")
    public String cart(){
        return "fore/cart";
    }

    @GetMapping(value="/orderConfirmed")
    public String orderConfirmed(){
        return "fore/orderConfirmed";
    }
    @GetMapping(value="/payed")
    public String payed(){
        return "fore/payed";
    }
    @GetMapping(value="/confirmPay")
    public String confirmPay(){
        return "fore/confirmPay";
    }
    @GetMapping(value="/bought")
    public String bought(){
        return "fore/bought";
    }

    @GetMapping(value="/alipay")
    public String alipay(){
        return "fore/alipay";
    }







}

