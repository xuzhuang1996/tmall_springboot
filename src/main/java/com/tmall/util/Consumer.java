package com.tmall.util;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

//@Service
public class Consumer {

    @JmsListener(destination = "consumer")
    public void receive(String string){
        System.out.println("收到："+string);
    }
}
