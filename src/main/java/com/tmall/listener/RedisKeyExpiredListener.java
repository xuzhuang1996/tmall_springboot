package com.tmall.listener;

import com.tmall.pojo.User;
import com.tmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.nio.charset.StandardCharsets;

public class RedisKeyExpiredListener extends KeyExpirationEventMessageListener {

    public RedisKeyExpiredListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Autowired
    UserService userService;

    //在监听函数里面写，事件处理
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        //过期的key
        String key = new String(message.getBody(),StandardCharsets.UTF_8);

        if(key.startsWith("UserCouponInfo")){
            //users::UserCoupon::users-2:coupon-1.这个字符串的解析
            int last = key.lastIndexOf(":");
            String sb2 = key.substring(last);//为了解析拿到uid\cid
            String sb1 = key.substring(0,last);
            int uid= Integer.valueOf(sb1.replaceAll("[^(0-9)]",""));
            int cid= Integer.valueOf(sb2.replaceAll("[^(0-9)]",""));
            userService.DeleteCoupon(uid,cid);//过期的时候，就触发删除
            System.out.println("优惠券过期:"+uid+"-"+cid);
        }

        System.out.println("过期的key:"+key);
        System.out.println("过期的channel:"+channel);//__keyevent@0__:expired
        System.out.println("过期的pattern:"+pattern);//[B@4061ddde
    }
}
