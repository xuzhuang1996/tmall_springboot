package com.tmall.service;

import com.tmall.dto.Exposer;
import com.tmall.dto.SeckillExecution;
import com.tmall.enums.SeckillStatEnum;
import com.tmall.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
public class SeckillService {
    //md5盐值字符串,用于混淆md5,随便写,根据pid生成md5值
    private final String slat = "asdfasd2341242@#$@#$%$%%#@$%#@%^%^";

    @Autowired
    ProductService productService;

    //秒杀开启的时候：输出确认开启；否则输出时间或者秒杀结束标志。因此需要dto用于数据传输
    public Exposer exportSeckillUrl(int seckillId){
        Product product = productService.get(seckillId);
        if(product==null){
            //说明产品没了，于是输出秒杀结束
            return new Exposer(false,seckillId);
        }
        //说明产品还在，判断时间是否符合
        Date startTime = product.getCreateDate();//这里由于懒得改数据结构，选择用里面的一个属性作为开启秒杀时间
        Date now =new Date();
        if(now.getTime()>startTime.getTime()){
            return new Exposer(true,getMD5(seckillId),seckillId);//说明秒杀开启
        }else {
            return new Exposer(false,seckillId,now.getTime(),startTime.getTime(),0);//说明服务器时间还没到，但客户端时间到了
        }
    }

    @Transactional
    public SeckillExecution execution(int uid,int seckillId,String md5){
        //如果你是修改了地址的话，即你一般不会修改MD５，因为太复杂，你一般修改PID。也就是你的PID与我给你的MD5值不匹配
        if (StringUtils.isEmpty(md5) || !md5.equals(getMD5(seckillId))) {
            return new SeckillExecution(SeckillStatEnum.DATA_REWRITE,false);//原文选择抛出异常，而我选择还是new
        }
        //如何处理点击秒杀之后的操作，加锁？
        return null;
    }





    private String getMD5(long seckillId) {
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

}
