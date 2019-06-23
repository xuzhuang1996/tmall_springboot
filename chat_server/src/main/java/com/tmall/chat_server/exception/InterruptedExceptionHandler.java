package com.tmall.chat_server.exception;

import com.tmall.common.dto.ChatMessage;
import org.springframework.stereotype.Component;

import java.nio.channels.SocketChannel;

@Component
public class InterruptedExceptionHandler {
    public void handle(SocketChannel channel, ChatMessage message) {
        System.out.println("异常需要处理");
    }
}
