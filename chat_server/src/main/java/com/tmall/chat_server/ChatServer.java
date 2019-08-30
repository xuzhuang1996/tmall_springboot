package com.tmall.chat_server;

import com.tmall.chat_server.exception.InterruptedExceptionHandler;
import com.tmall.chat_server.handler.AbstractChatMessageHandler;
import com.tmall.chat_server.handler.MessageHandlerAdapter;
import com.tmall.common.dto.ChatMessage;
import com.tmall.common.utils.ProtoStuffUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//@Slf4j https://blog.csdn.net/application_context/article/details/82468490
@Component
public class ChatServer {
    //===========================成员变量===================================================================================
    //ServerSocketChannel允许我们监听TCP链接请求，每个请求会创建会一个SocketChannel，一般是服务器实现
    private ServerSocketChannel serverSocketChannel;
    private static final int PORT = 9000;
    public static final String QUIT = "QUIT";//
    private Selector selector;
    //线程池
    private ExecutorService readPool;
    private AtomicInteger onlineUsersNumber;
    private ListenerThread listenerThread;

    //根据消息类型，获取对应的处理类
    @Autowired
    MessageHandlerAdapter messageHandlerAdapter;

    @Autowired
    private InterruptedExceptionHandler interruptedExceptionHandler;
    //private InterruptedExceptionHandler exceptionHandler;
    /**
     * 内部类，监听线程，用于执行服务器的监听操作，即监听是否有客户端连接
     * 推荐的结束线程的方式是使用中断
     * 在while循环开始处检查是否中断，并提供一个方法来将自己中断
     * 不要在外部将线程中断
     * <p>
     * 另外，如果要中断一个阻塞在某个地方的线程，最好是继承自Thread，先关闭所依赖的资源，再关闭当前线程
     */
    private class ListenerThread extends Thread {
        @Override
        public void interrupt() {
            try {
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                super.interrupt();
            }
        }
        @Override
        public void run(){
            //如果有一个及以上的客户端的数据准备就绪
            while (!Thread.currentThread().isInterrupted()) {

                try {
                    //当注册的事件到达时，方法返回；否则,该方法会一直阻塞
                    selector.select();
                    //获取当前选择器中所有注册的监听事件.
                    for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                        SelectionKey key = it.next();
                        //删除已选的key,以防重复处理
                        it.remove();
                        //接着，开始对应key处理；
                        // 如果"接收"事件已就绪
                        if (key.isAcceptable()) {
                            //交由接收事件的处理器处理
                            handleAcceptRequest();
                        } else if (key.isReadable()) {
                            //如果"读取"事件已就绪
                            //取消可读触发标记，本次处理完后才打开读取事件标记
                            key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                            //交由读取事件的处理器处理
                            readPool.execute(new ReadEventHandler(key));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void shutdown() {
            Thread.currentThread().interrupt();
        }
    }

    //注意：处于线程池中的线程会随着线程池的shutdown方法而关闭
    private class ReadEventHandler implements Runnable {
        private ByteBuffer buf;
        private SocketChannel client;
        private ByteArrayOutputStream baos;
        private SelectionKey key;
        public ReadEventHandler(SelectionKey key) {
            this.key = key;
            this.client = (SocketChannel) key.channel();
            this.buf = ByteBuffer.allocate(1024);
            this.baos = new ByteArrayOutputStream();
        }
        @Override
        public void run() {

                try {
                    int size;
                    //返回值大于0表示还没读完，=0即读完，就退出while
                    while (((size = client.read(buf)) > 0))
                    {
                        buf.flip();
                        baos.write(buf.array(), 0, size);
                        buf.clear();
                    }
                    //当客户端主动切断连接的时候，服务端 Socket 的读事件（FD_READ）仍然起作用，服务端 Socket 的状态仍然是有东西可读，当然此时读出来的字节肯定是 0。
                    if (size == -1) {
                        System.out.println("遇到-1的情况");
                        return;
                    }
                    System.out.println("读取完毕，继续监听");
                    //继续监听读取事件，key.interestOps返回代表需要Selector监控的IO操作的bit mask
                    key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                    // 让处在阻塞状态的select()方法立刻返回 该方法使得选择器上的第一个还没有返回的选择操作立即返回。
                    // 如果当前没有进行中的选择操作，那么下一次对select()方法的一次调用将立即返回。
                    // 某个线程调用select()方法后阻塞了，即使没有通道处于就绪状态，也有办法让其从select()方法返回。
                    // 只要让其它线程在第一个线程调用select()方法的那个对象上调用Selector.wakeup()方法即可。阻塞在select()方法上的线程会立马返回。
                    key.selector().wakeup();
                    byte[] bytes = baos.toByteArray();
                    baos.close();
                    //拿到客户端的数据后，后续处理。
                    // 客户端的数据有可能是下线通知、有可能是发送其他人的消息，需要匹配到具体某个处理类.如果if-else太low。
                    ChatMessage message = ProtoStuffUtil.deserialize(bytes, ChatMessage.class);
                    //于是采用反射的方式，先根据消息类型，拿到对应处理类的类名。这里可以直接用spring的getBean获取,或者反射
//                    String beanName = message.getHeader().getType_message().toString();
//                    ChatMessageHandler handler = (ChatMessageHandler)SpringContextUtil.getBean(beanName);//这个工具类没有用tmall的，而是在当前项目中新建的一个。因为一个项目一个spring容器
                    try {
                        AbstractChatMessageHandler handler = messageHandlerAdapter.getHandler(message.getHeader().getType_message());
                        if(handler==null){
                            throw new Exception("获取bean失败，即获取消息处理对象失败");
                        }
                        handler.handle(message, selector, key, onlineUsersNumber);
                    } catch (Exception e) {
                        interruptedExceptionHandler.handle(client, message);
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    key.cancel();
                    System.out.println("cancel key for Exception");
                }


        }
    }



    //===========================成员方法===================================================================================
    public ChatServer() {
        //log.info("服务器启动");
        System.out.println("服务器启动");
        initServer();
    }
    private void initServer() {
        try {
            serverSocketChannel = ServerSocketChannel.open();
            //切换为非阻塞模式.在使用传统的ServerSocket和Socket的时候,很多时候程序是会阻塞的,
            //比如 serversocket.accept()的时候都会阻塞 accept()方法除非等到客户端socket的连接或者被异常中断,否则会一直等待下去
            //在ServerSocket与Socket的方式中 服务器端往往要为每一个客户端(socket)分配一个线程,而每一个线程都有可能处于长时间的阻塞状态中.而过多的线程也会影响服务器的性能.
            // 在JDK1.4引入了非阻塞的通信方式,这样使得服务器端只需要一个线程就能处理所有客户端socket的请求
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            //获得选择器,用于检查一个或多个NIO Channel（通道）的状态是否处于可读、可写
            selector = Selector.open();
            //监听状态为接受就绪的channel
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            //线程池，CallerRunsPolicy饱和策略，即用调用者的线程执行任务，ArrayBlockingQueue阻塞队列，存放来不及执行的线程
            this.readPool = new ThreadPoolExecutor(5, 10, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10), new ThreadPoolExecutor.CallerRunsPolicy());
            this.listenerThread = new ListenerThread();
            this.onlineUsersNumber = new AtomicInteger(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动方法，线程最好不要在构造函数中启动，应该作为一个单独方法，或者使用工厂方法来创建实例
     * 避免构造未完成就使用成员变量
     */
    public void launch() {
        new Thread(listenerThread).start();
        //new Thread(taskManagerThread).start();
    }

    /**
     * 处理客户端的连接请求
     */
    private void handleAcceptRequest() {
        try {
            //accept()方法创建一个SocketChannel对象用户从客户端读/写数据
            SocketChannel client = serverSocketChannel.accept();
            // 接收的客户端也要切换为非阻塞模式
            client.configureBlocking(false);
            // 监控客户端的读操作是否就绪
            client.register(selector, SelectionKey.OP_READ);
            System.out.println("服务器连接客户端:"+client.getLocalAddress());
//            log.info("服务器连接客户端:{}",client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 关闭服务器
     */
    public void shutdownServer() {
        try {
//            taskManagerThread.shutdown();
            listenerThread.shutdown();
            readPool.shutdown();
            serverSocketChannel.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
