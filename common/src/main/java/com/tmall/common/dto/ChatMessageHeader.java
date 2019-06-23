package com.tmall.common.dto;

import com.tmall.common.chat_enumeration.MessageType;
//lombok简化POJO开发
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
public class ChatMessageHeader {
    private String sender;
    private String receiver;
    private MessageType type_message;
    private Long timestamp;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public MessageType getType_message() {
        return type_message;
    }

    public void setType_message(MessageType type_message) {
        this.type_message = type_message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    //=======================================Bulider注解==========================================================
    private ChatMessageHeader(ChatBuilder chatBuilder){
        this.receiver=chatBuilder.receiver;
        this.sender=chatBuilder.sender;
        this.type_message=chatBuilder.type_message;
        this.timestamp=chatBuilder.timestamp;
    }
    //这里源码用的注解Builder，我选择直接使用链式建造者模式来编码
    public static class ChatBuilder {
        //必须有的属性要用final修饰，防止这些属性没有被赋值，其他非必须的属性不能用final
        private String sender;
        private String receiver;
        private MessageType type_message;
        private Long timestamp;

        public ChatBuilder sender(String s){
            this.sender=s;
            return this;
        }
        public ChatBuilder receiver(String s){
            this.receiver=s;
            return this;
        }
        public ChatBuilder type_message(MessageType s){
            this.type_message=s;
            return this;
        }
        public ChatBuilder timestamp(Long s){
            this.timestamp=s;
            return this;
        }

        public ChatMessageHeader build(){
            return new ChatMessageHeader(this);
        }
    }
}
