package com.tmall.common.dto;

public class ChatResponse {
    private ChatResponseHeader header;
    private byte[] body;

    public ChatResponse(ChatResponseHeader header, byte[] body) {
        this.header = header;
        this.body = body;
    }

    public ChatResponse() {
    }

    public ChatResponseHeader getHeader() {
        return header;
    }

    public void setHeader(ChatResponseHeader header) {
        this.header = header;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
