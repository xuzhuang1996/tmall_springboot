package com.tmall.chat_client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ClientManage {
    //存放所有用户客户端对象
    public static Map<String,ChatClient>map=new ConcurrentHashMap<>();
}
