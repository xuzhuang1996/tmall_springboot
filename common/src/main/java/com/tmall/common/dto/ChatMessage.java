package com.tmall.common.dto;

//@Data
//@NoArgsConstructor//构造函数
//@AllArgsConstructor
public class ChatMessage {
    private ChatMessageHeader header;
    private byte[] body;

    public ChatMessage(){}
    public ChatMessage(ChatMessageHeader header, byte[] body) {
        this.header = header;
        this.body = body;
    }

    public ChatMessageHeader getHeader() {
        return header;
    }

    public void setHeader(ChatMessageHeader header) {
        this.header = header;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
