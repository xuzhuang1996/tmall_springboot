package com.tmall.chat_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Scanner;

@SpringBootApplication
public class ChatServerApplication {

    public static void main(String[] args) {
        //之前取ConfigurableApplicationContext一直为空，因为run在while循环下面，导致没有运行到run.
        ConfigurableApplicationContext run = SpringApplication.run(ChatServerApplication.class, args);

        System.out.println("Initialing...");
        ChatServer chatServer=run.getBean(ChatServer.class);
        chatServer.launch();
        Scanner scanner = new Scanner(System.in, "UTF-8");
        while (scanner.hasNext()) {
            String next = scanner.next();
            if (next.equalsIgnoreCase("close")) {
                System.out.println("服务器准备关闭");
                chatServer.shutdownServer();
                System.out.println("服务器已关闭");
            }
        }

    }

}
