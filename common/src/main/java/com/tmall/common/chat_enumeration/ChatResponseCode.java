package com.tmall.common.chat_enumeration;

import java.util.HashMap;
import java.util.Map;

public enum ChatResponseCode {
    LOGIN_SUCCESS(1,"登录成功"),
    LOGIN_FAILURE(2,"登录失败"),
    LOGOUT_SUCCESS(3,"下线成功");

    private int code;
    private String desc;
    private static Map<Integer, ChatResponseCode> map = new HashMap<>();

    ChatResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    static {
        for (ChatResponseCode code : values()) {
            map.put(code.getCode(), code);
        }
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static ChatResponseCode fromCode(int code) {
        return map.get(code);
    }
}
