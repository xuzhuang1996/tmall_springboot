package com.tmall.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//适合固定查询语句的。一次性将所有内容查出来，后期根据key取值
public abstract class AutoReloadCache<K, V> {
    private static Logger logger =LoggerFactory.getLogger(AutoReloadCache.class);
    protected Map<K,V> cache=null;//内容都存在这里
    private boolean initialed=false;//判断是否为初次加载。
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new CacheThreadFactory("AutoReloadCacheThread"));
    //==============================================成员方法===============================================
    protected abstract Map<K,V> reload();//具体加载逻辑，即加载什么类型的数据。由具体子类完成
    //默认间隔多少次从数据库取数据.子类可以根据具体加载对象，重写该方法来设置间隔加载时间。
    protected long getDelay(){
        return TimeUnit.MINUTES.toSeconds(10);
    }
    //获取缓存
    public V get(K key){
        if(!initialed)
            this.checkAndStartTimer();
        return cache==null?null:cache.get(key);
    }

    //开启线程任务.对于一个缓存服务的加载，用一个线程来做。原先加了多线程锁。我不知道要不要加，先不加.如果要加，加双层锁判断init
    //initialDalay参数为0，即立即执行。一般用于任务比较长的查询，设置为getDelay()
    private void checkAndStartTimer(){
        if(!initialed){
            executor.scheduleWithFixedDelay(
                    new Runnable() {
                        @Override
                        public void run() {
                            logger.info(Thread.currentThread().getName()+":重新读取数据库，更新缓存");
                            System.out.println(Thread.currentThread().getName()+":重新读取数据库，更新缓存");
                            //LoggerFactory.getLogger(Thread.currentThread().getName()).info("更新缓存"); // 不用新建日志对象。直接输出。还能顺便输出到控制台
                            //这里的try是必须的，否则捕获不到异常
                            try {
                                cache = reload();//如果异常被捕获，则继续运行，但是如果没有捕获，将停止。
                            }catch (Exception e){
                                logger.error(e.getMessage(),e);
                            }
                        }
                    },0,this.getDelay(),TimeUnit.SECONDS
            );
            initialed=true;
        }
    }

    //线程工厂。确定线程的初始属性。这里仅仅设置了名字。
    private class CacheThreadFactory implements ThreadFactory{
        private String name;
        private AtomicInteger atomicInteger = new AtomicInteger(0);
        CacheThreadFactory(String s) {
            name=s;
        }
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r,name+atomicInteger.getAndIncrement());
            //加入异常处理函数。通过cache.md中的分析，认为在该线程工厂中设置处理函数是没有意义的。因此注释
//            t.setUncaughtExceptionHandler((t1, e) -> {
//                System.out.println(t1.getName()+"出错："+e.getMessage());
//                //LoggerFactory.getLogger(t.getName()).error(e.getMessage(), e);
//            });
//            原表达式，上面是lambda表达式
//            t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
//                @Override
//                public void uncaughtException(Thread t, Throwable e) {
//                    System.out.println(t.getName()+"出错："+e.getMessage());
//                    LoggerFactory.getLogger(t.getName()).error(e.getMessage(), e);
//                }
//            });
            return t;
        }
    }
}
