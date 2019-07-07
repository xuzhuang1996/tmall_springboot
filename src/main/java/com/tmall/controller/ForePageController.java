package com.tmall.controller;


import com.tmall.chat_client.ChatClient;
import com.tmall.chat_client.ClientManage;
import com.tmall.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
        //============================================chat=================================================
        User user =(User)  session.getAttribute("user");
        ChatClient chatClient = ClientManage.map.remove(user.getName());
        chatClient.disConnect();

        //=================================================================================================
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

    //就算有参数pid，也不用管，因为是转发，因此还在URL中
    //update 'product' set 'createDate'='2019-03-11 00:00:00'
    @GetMapping("/SecProduct")
    public String SecProduct( ) {
        return "fore/SecProduct";
    }

    @GetMapping("/chat")
    public String chat( ) {
        return "fore/chat";
    }


}

