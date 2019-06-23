package com.tmall.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//@RunWith(SpringRunner.class)
//@SpringBootTest
class node{
    public node(int ii,int jj){
        i=ii;
        j=jj;
    }
    int i;
    int j;
}
public class test {
    static List<String>result = new ArrayList<String>();
    static String tag;

    public static int numIslands(char[][] grid) {
        Stack<node>stack=new Stack<>();
        int x=grid.length;
        int y=grid[0].length;
        boolean [][]v=new boolean[x][y];
        int count=0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                if(grid[i][j]=='1'&&!v[i][j]){
                    stack.push(new node(i,j));
                    v[i][j]=true;
                    count++;
                    while (!stack.isEmpty()){
                        node c = stack.pop();

                        if(c.i-1>-1 && grid[c.i-1][c.j]=='1'&&!v[c.i-1][c.j])
                        {
                            stack.push(new node(c.i-1,c.j));
                            v[c.i-1][c.j]=true;
                        }
                        if(c.i+1<x && grid[c.i+1][c.j]=='1'&&!v[c.i+1][c.j])
                        {
                            stack.push(new node(c.i+1,c.j));
                            v[c.i+1][c.j]=true;
                        }
                        if(c.j-1>-1 && grid[c.i][c.j-1]=='1'&&!v[c.i][c.j-1])
                        {
                            stack.push(new node(c.i,c.j-1));
                            v[c.i][c.j-1]=true;
                        }
                        if(c.j+1<y && grid[i][c.j+1]=='1'&&!v[i][c.j+1])
                        {
                            stack.push(new node(c.i,c.j+1));
                            v[c.i][c.j+1]=true;
                        }
                    }
                }
            }
        }
        return count;
    }



    public static void main(String[] args) {
        System.out.println(1^1);
//        String s = "pineapplepenapple";
//        List<String> wordDict = new ArrayList<String>();
//        wordDict.add("a");
//        wordDict.add("aa");
//        wordDict.add("aaaa");
//        wordDict.add("apple");
//        wordDict.add("pen");
//        wordDict.add("applepen");
//        wordDict.add("pine");
//        wordDict.add("pineapple");
//        tag=s;
//        fun(s,new StringBuffer(),wordDict);
//        System.out.println(result);
    }
    public static void fun(String s, StringBuffer one, List<String> dict){
        int len=s.length();
        //本身是句子。
        if(!isTrue(s,dict)){
            return ;
        }
        if(len==1){
            one.append(s);
            if(s.equals(tag)){
                result.add(s);
            }
        }



        for(int i=1;i<len;i++){
            String left = s.substring(0,i);
            String right = s.substring(i,len);
            //将一个句子分成2半，如果左右都是句子才能进行拆分
            if( isTrue(left,dict) && isTrue(right,dict) ){
                String tmp=one.toString();//保留一下，待会用于回溯
                fun(left,one,dict);
                if(one.toString().equals(tmp))//如果执行完一遍后one没变化，说明是个单词。当然这里忽略了比如pineapple可以作一个整体或者分开
                    one.append(left);

                one.append(" ");

                String tmp1 = one.toString();
                fun(right,one,dict);
                if(one.toString().equals(tmp1))
                    one.append(right);

                if(one.toString().replace(" ","").equals(tag)){
                    if(!result.contains(one.toString()))
                    {
                        result.add(new String(one));
                    }
                }

                one=new StringBuffer(tmp);
            }
        }


    }



    public static boolean isTrue(String s, List<String> dict){
        int len = s.length();
        boolean []now = new boolean[len+1];
        now[0]=true;//这里now[i]是指第i个元素而不是s中的下标为i

        for(int i=1;i<=len;i++){
            for(int j=0;j<i;j++){
                String tmp = s.substring(j,i);//只要上一个i为true，且j到i为包含才可以
                if(now[j] && dict.contains(tmp))//如果是我写的now[i-1]，因此这里的j其实是之前的i
                {
                    now[i]=true;//但实际上，不能是i-1应该是i之前成功的那个下标
                    break;//从j到i是否包含的前提是j之前都是好的。
                }

            }
        }
        return now[len];
    }




//    @Resource
//    Producer producer;
//
//    @Autowired
//    pub p;
//    @Test
//    public void ProducerTest(){
//        producer.send("consumer","我是你得");
//    }
//    @Test
//    public void t(){
//        p.pulish("topic目的","xuzhuang");
//    }

}
