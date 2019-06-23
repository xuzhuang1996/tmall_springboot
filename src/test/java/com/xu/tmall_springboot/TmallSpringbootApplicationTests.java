package com.xu.tmall_springboot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;


public class TmallSpringbootApplicationTests {


    public static void rotate(int[] nums, int k) {
        int n = 1;
        String s=n+"";
        s.indexOf("1");

        for(int i=0;i<k/2;i++){
            int t = nums[i];
            nums[i] = nums[k-i-1];
            nums[k-i-1] = t;
        }
        for(int i=k;i<k+(n-k)/2;i++){
            int t = nums[i];
            nums[i] = nums[n-i-1+k];
            nums[n-i-1+k] = t;
        }
    }
    public static void main(String[] args) {

//        HashMap<Integer,Integer>map = new HashMap<>();
//        PriorityQueue<Integer> p = new PriorityQueue<Integer>((a,b)->a-b);//最小堆
//        p.add(2);p.add(22);p.add(1);p.add(12);p.add(134);p.add(2);p.add(13);
//        p.remove();
//        System.out.println(p);
//        int []a={1,3,-1,-3,5,3,6,7};
//        int k=3;
//        List<Integer>list=new ArrayList<>();
//        PriorityQueue<Integer>p=new PriorityQueue<Integer>((a1,a2)->a2-a1);//需要最大堆
//        for (int i = 0; i < a.length; i++) {
//            if(p.size()<k){
//                p.add(a[i]);
//                if(p.size()==k){
//                    list.add(p.peek());
//                    p.remove(a[i-k+1]);
//                }
//            }
//        }

    }





    public static void part(int []n,int start,int end){
        int s=start,e=end;//主要是为了记录边界，后面根据中间分隔点。来构造2个数组
        if(start>end)return;
        int tag=n[start];
        //从右边开始，因为左边第一个是基准元素，空出来了，因此右边找到比基准小的，直接放左边第一个位置就行
        while (start<end){
            while (start<end && n[end]>=tag){//这里必须为=
                end--;
            }
            n[start] = n[end];
//            start++;
            while (start<end && n[start]<=tag){
                start++;
            }
            n[end]=n[start];
//            end--;
        }
        n[start]=tag;
        part(n,s,start-1);
        part(n,start+1,e);
    }
}

