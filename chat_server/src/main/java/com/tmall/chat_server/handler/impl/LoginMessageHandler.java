package com.tmall.chat_server.handler.impl;

import com.tmall.chat_server.handler.ChatMessageHandler;
import com.tmall.chat_server.serverMessage.PromptMsgProperty;
import com.tmall.chat_server.userManage.UserManager;
import com.tmall.common.chat_enumeration.ChatResponseCode;
import com.tmall.common.chat_enumeration.ResponseType;
import com.tmall.common.dto.ChatMessage;
import com.tmall.common.dto.ChatMessageHeader;
import com.tmall.common.dto.ChatResponse;
import com.tmall.common.dto.ChatResponseHeader;
import com.tmall.common.utils.ProtoStuffUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

@Component("LOGIN")
public class LoginMessageHandler extends ChatMessageHandler {
    @Autowired
    private UserManager userManager;

    //注册用户，并广播。不做登录验证
    @Override
    public void handle(ChatMessage message, Selector server, SelectionKey client, AtomicInteger onlineUsers) throws InterruptedException {
        SocketChannel clientChannel = (SocketChannel) client.channel();//SocketChannel用于TCP的数据读写，一般是客户端实现
        ChatMessageHeader header = message.getHeader();
        String username = header.getSender();
        //执行注册，如果注册成功，就返回客户端成功的消息
        if(onlineUsers.get()>3)
            System.out.println("8");
        if (userManager.login(clientChannel, username)) {
            byte[] response_user = ProtoStuffUtil.serialize(
                    new ChatResponse(
                             new ChatResponseHeader.ChatResponseBuilder()//header
                                     .responseCode(ChatResponseCode.LOGIN_SUCCESS.getCode())
                                     .sender(message.getHeader().getSender())
                                     .timestamp(message.getHeader().getTimestamp())
                                     .type_response(ResponseType.PROMPT).build(),
                             String.format(PromptMsgProperty.LOGIN_SUCCESS,onlineUsers.incrementAndGet()).getBytes(PromptMsgProperty.charset)//body
                    )
            );
            byte[] loginBroadcast = ProtoStuffUtil.serialize(
                    new ChatResponse(
                            new ChatResponseHeader.ChatResponseBuilder()//header
                                    .sender(SYSTEM_SENDER)
                                    .timestamp(message.getHeader().getTimestamp())
                                    .type_response(ResponseType.NORMAL).build(),
                            String.format(PromptMsgProperty.LOGIN_BROADCAST,onlineUsers.incrementAndGet()).getBytes(PromptMsgProperty.charset)//body
                    )
            );
            //消息做好后，准备发送
            try {
                clientChannel.write(ByteBuffer.wrap(response_user));//告诉用户登录成功，同时准备广播其他人
                //连续发送信息不可行,必须要暂时中断一下
                //粘包问题，需要查一下
                Thread.sleep(10);
                super.broadcast(loginBroadcast,server);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            //登录失败
            byte[] response_user = ProtoStuffUtil.serialize(
                    new ChatResponse(
                            new ChatResponseHeader.ChatResponseBuilder()//header
                                    .responseCode(ChatResponseCode.LOGIN_FAILURE.getCode())
                                    .sender(message.getHeader().getSender())
                                    .timestamp(message.getHeader().getTimestamp())
                                    .type_response(ResponseType.PROMPT).build(),
                            String.format(PromptMsgProperty.LOGIN_FAILURE,onlineUsers.incrementAndGet()).getBytes(PromptMsgProperty.charset)//body
                    )
            );
            //发送
            try {
                clientChannel.write(ByteBuffer.wrap(response_user));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }//end of else

    }//end of function
}
