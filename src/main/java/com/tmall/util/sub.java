package com.tmall.util;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

//@Service
public class sub {
    @JmsListener(destination ="topic目的",containerFactory = "wqe")
    public void subscribe(String text){
        System.out.println("收到"+text);
    }
}
