package com.tmall.util;

import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.jms.Destination;

//@Service
public class pub {
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    public void pulish(String desName,String message){
        Destination destination = new ActiveMQTopic(desName);
        System.out.println("发布动态"+message);
        jmsMessagingTemplate.convertAndSend(destination,message);
    }
}
