package com.tmall.common.dto;

import com.tmall.common.chat_enumeration.ResponseType;

//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
public class ChatResponseHeader {
    private String sender;
    private ResponseType type;
    private Integer responseCode;
    private Long timestamp;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public ResponseType getType() {
        return type;
    }

    public void setType(ResponseType type) {
        this.type = type;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    //==============================================Builder==================================================
    //第一步，私有构造函数。
    private ChatResponseHeader(ChatResponseBuilder chatResponseBuilder) {
        this.sender = chatResponseBuilder.sender;
        this.type = chatResponseBuilder.type;
        this.responseCode = chatResponseBuilder.responseCode;
        this.timestamp = chatResponseBuilder.timestamp;
    }
    //第二步，静态Builder类
    public static class ChatResponseBuilder {
        private String sender;
        private ResponseType type;
        private Integer responseCode;
        private Long timestamp;

        public ChatResponseBuilder sender(String s){
            this.sender=s;
            return this;
        }
        public ChatResponseBuilder type_response(ResponseType s){
            this.type=s;
            return this;
        }
        public ChatResponseBuilder responseCode(Integer s){
            this.responseCode=s;
            return this;
        }
        public ChatResponseBuilder timestamp(Long s){
            this.timestamp=s;
            return this;
        }

        //根据目标类的私有构造函数生成，目标对象
        public ChatResponseHeader build(){
            return new ChatResponseHeader(this);
        }
    }
}
