package com.tmall.chat_server.handler.impl;

import com.tmall.chat_server.handler.ChatMessageHandler;
import com.tmall.chat_server.serverMessage.PromptMsgProperty;
import com.tmall.chat_server.userManage.UserManager;
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

//这个名字在chatServer中使用SpringContextUtil.getBean(beanName);来获取bean
@Component("NORMAL")
public class NormalMessageHandler extends ChatMessageHandler {

    @Autowired
    private UserManager userManager;
    @Override
    public void handle(ChatMessage message, Selector server, SelectionKey client, AtomicInteger onlineUsers) throws InterruptedException {
        SocketChannel clientChannel = (SocketChannel) client.channel();
        ChatMessageHeader header = message.getHeader();
        SocketChannel receiverChannel = userManager.getUserChannel(header.getReceiver());//获取消息接收者的通道
        if (receiverChannel == null) {
            //接收者下线
            byte[] response_user = ProtoStuffUtil.serialize(
                    new ChatResponse(
                            new ChatResponseHeader.ChatResponseBuilder()//header
                                    .sender(message.getHeader().getSender())
                                    .timestamp(message.getHeader().getTimestamp())
                                    .type_response(ResponseType.PROMPT).build(),
                            PromptMsgProperty.RECEIVER_LOGGED_OFF.getBytes(PromptMsgProperty.charset)//body
                    )
            );
            try {
                clientChannel.write(ByteBuffer.wrap(response_user));//告诉发送方，接收方下线
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            byte[] response_user = ProtoStuffUtil.serialize(
                    new ChatResponse(
                            new ChatResponseHeader.ChatResponseBuilder()//header
                                    .sender(message.getHeader().getSender())
                                    .timestamp(message.getHeader().getTimestamp())
                                    .type_response(ResponseType.PROMPT).build(),
                            message.getBody()//body
                    )
            );
            System.out.println("消息转发："+receiverChannel);
            try {
                receiverChannel.write(ByteBuffer.wrap(response_user));//接收方接收
                clientChannel.write(ByteBuffer.wrap(response_user));//通知发送方，消息已送达
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
