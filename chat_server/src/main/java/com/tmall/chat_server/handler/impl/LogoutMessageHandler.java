package com.tmall.chat_server.handler.impl;

import com.tmall.chat_server.handler.ChatMessageHandler;
import com.tmall.chat_server.serverMessage.PromptMsgProperty;
import com.tmall.chat_server.userManage.UserManager;
import com.tmall.common.chat_enumeration.ChatResponseCode;
import com.tmall.common.chat_enumeration.ResponseType;
import com.tmall.common.dto.ChatMessage;
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

@Component("LOGOUT")
public class LogoutMessageHandler extends ChatMessageHandler {

    @Autowired
    private UserManager userManager;
    @Override
    public void handle(ChatMessage message, Selector server, SelectionKey client, AtomicInteger onlineUsers) throws InterruptedException {
        SocketChannel clientChannel = (SocketChannel) client.channel();
        userManager.logoutHandle(clientChannel);
        //退出登录直接就是当前服务器的userManage退出,以及告诉其他人自己退了，即广播
        //开始准备消息，退出消息，然后打包成字节数组.告诉自己退出成功以及告诉别人我退了
        byte[] response = ProtoStuffUtil.serialize(
                new ChatResponse(new ChatResponseHeader.ChatResponseBuilder()
                        .type_response(ResponseType.PROMPT)
                        .responseCode(ChatResponseCode.LOGOUT_SUCCESS.getCode())
                        .sender(message.getHeader().getSender())
                        .timestamp(message.getHeader().getTimestamp()).build(),
                        PromptMsgProperty.LOGOUT_SUCCESS.getBytes(PromptMsgProperty.charset)));

        byte[] logoutBroadcast = ProtoStuffUtil.serialize(
                new ChatResponse(
                        new ChatResponseHeader.ChatResponseBuilder()//header
                                .sender(SYSTEM_SENDER)
                                .timestamp(message.getHeader().getTimestamp())
                                .type_response(ResponseType.NORMAL).build(),
                        String.format(PromptMsgProperty.LOGOUT_BROADCAST,message.getHeader().getSender()).getBytes(PromptMsgProperty.charset)//body
                )
        );
        //消息做好后，准备发送
        try {
            onlineUsers.decrementAndGet();
            clientChannel.write(ByteBuffer.wrap(response));
            Thread.sleep(10);
            super.broadcast(logoutBroadcast,server);//java.nio.channels.ClosedChannelException
            //必须要cancel，否则无法从keys从去除该客户端==================可以调试看一下发生了什么：java.io.IOException: 你的主机中的软件中止了一个已建立的连接。===================
            client.cancel();
            clientChannel.close();//用完Selector后调用其close()方法会关闭该Selector，且使注册到该Selector上的所有SelectionKey实例无效。通道本身并不会关闭。
            clientChannel.socket().close();//与该通道相关的socket也要关闭

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
