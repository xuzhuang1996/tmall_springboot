package com.tmall.chat_client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatClientApplication {

    public static void main(String[] args) {

        SpringApplication.run(ChatClientApplication.class, args);

//        ChatClient chatClient=new ChatClient("x");
//        ChatClient chatClient2=new ChatClient("xx");
//        chatClient.launch();
//        chatClient2.launch();
//        chatClient.send("@xx:hello");
    }

}
