package com.tmall.common;

import com.xu.lombok.anno.myBuilder;

@myBuilder
public class CommonApplicationTests {

    public int xu;
    public String xuxux;

    public void contextLoads() {
    }

    public static void main(String[] args) {
        CommonApplicationTests xu = new CommonApplicationTests(1, "abcxc");
        System.out.println(xu.xuxux);
//        System.out.println("5");
    }

}
