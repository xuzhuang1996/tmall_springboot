package com.tmall.chat_server.handler;

import com.tmall.common.chat_enumeration.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageHandlerAdapter {
    //如果这里为null，（也许），查一下@PostConstruct注解对应的处理方法
    @Autowired
    private List<AbstractChatMessageHandler> handlerList;

    public AbstractChatMessageHandler getHandler(MessageType messageType) throws Exception {
        if(messageType != null){
            for (AbstractChatMessageHandler handler : this.handlerList) {
                if (handler.supports(messageType)) {
                    return handler;
                }
            }
        }
        throw new Exception("无法处理该消息");
    }
}
