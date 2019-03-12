package com.tmall;

import com.tmall.util.PortUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//这个类放的位置也讲究，就是要在com.tmall包下。因为其他包都在com.tmall。这样才能扫描到其他包下的类
@SpringBootApplication
@EnableCaching
public class Application {

    static {
        PortUtil.checkPort(6379,"Redis 服务端",true);
//        PortUtil.checkPort(9300,"ElasticSearch 服务端",true);
//        PortUtil.checkPort(5601,"Kibana 工具", true);
    }
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


}
