package com.tmall.chat_client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ClientManage {
    //存放所有用户客户端对象。其实可以让spring保管这个对象。不过需要注入，干脆静态
    public static Map<String,ChatClient>map=new ConcurrentHashMap<>();
}
