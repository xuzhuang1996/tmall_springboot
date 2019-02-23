package com.tmall.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


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

}

