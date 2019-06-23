package com.tmall.chat_server.userManage;

import com.tmall.chat_server.utils.HttpClientUtil;
import com.tmall.common.utils.JsonUtils;
import com.tmall.pojo.User;
import org.springframework.stereotype.Component;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserManager {
    //存放用户名，用户对象。这个对象是存放所有用户数据，而不是在线用户。因此意义不大，主要是从数据库取出用户对象。
    private Map<String, User> users;
    String URL = "http://localhost:8888/tmall_springboot/users_all";//获取用户数据的URL
    /**
     * key是ip和端口号，value是用户名
     */
    private Map<SocketChannel, String> onlineUsers;
    public UserManager() {
        users = new ConcurrentHashMap<>();
        onlineUsers = new ConcurrentHashMap<>();
        //初始化users，利用httpclient调用服务
        //调用服务,获取数据后转成对象使用
        String json = HttpClientUtil.doGet(URL, null);
        List<User> list = JsonUtils.jsonToList(json,User.class);
        if(list!=null && list.size()>0)
            for (User user : list) {
                users.put(user.getName(), user);
            }
    }

    //用户登录时需要注册用户到管理员中
    public synchronized  boolean login(SocketChannel channel, String username) {
        if (!users.containsKey(username)) {
            return false;
        }
        User user = users.get(username);
        user.setChannel(channel);
        onlineUsers.put(channel, username);//这步就是注册进管理员中
        return true;
    }
    //获取用户的通道，进行发送消息。这个通道在注册的时候就会设置。如果为空，说明用户下线。
    public synchronized SocketChannel getUserChannel(String username) {
        User user = users.get(username);
        if(user == null){
            return null;
        }
        SocketChannel lastLoginChannel = user.getChannel();
        if (onlineUsers.containsKey(lastLoginChannel)) {
            return lastLoginChannel;
        } else {
            return null;
        }
    }

}