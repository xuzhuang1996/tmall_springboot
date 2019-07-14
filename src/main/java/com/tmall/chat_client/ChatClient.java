package com.tmall.chat_client;

import com.tmall.common.chat_enumeration.ChatResponseCode;
import com.tmall.common.chat_enumeration.MessageType;
import com.tmall.common.dto.ChatMessage;
import com.tmall.common.dto.ChatMessageHeader;
import com.tmall.common.dto.ChatResponse;
import com.tmall.common.dto.ChatResponseHeader;
import com.tmall.common.utils.ProtoStuffUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;


public class ChatClient {
    //=========================================Chat=====================================================
    private String username;
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    private Selector selector;
    private SocketChannel clientChannel;
    private ByteBuffer buf;
    private boolean isLogin = false;
    private boolean isConnected = false;
    private Charset charset = StandardCharsets.UTF_8;
    private ReceiverHandler listener;//监听-接受数据


    //控制
    public void launch() {
        this.listener = new ReceiverHandler();
        new Thread(listener).start();
    }

    public ChatClient(String s) {
        this.username = s;
        this.initNetWork();
    }

    /**
     * 初始化网络模块。准备用在网页端的登录那里调用
     */
    private void initNetWork() {
        try {
            selector = Selector.open();
            clientChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9000));
            //设置客户端为非阻塞模式
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            buf = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
            //登录时，想服务器进行发送登录请求，即注册请求
            ChatMessage message = new ChatMessage(
                    new ChatMessageHeader.ChatBuilder()
                            .type_message(MessageType.LOGIN)
                            .sender(username)
                            .timestamp(System.currentTimeMillis())
                            .build(),null);//密码不要
            clientChannel.write(ByteBuffer.wrap(ProtoStuffUtil.serialize(message)));

            isConnected = true;
            isLogin=true;//由于服务器不进行验证，因此初始化网络后就是登录状态了
        } catch (ConnectException e) {
            //"连接服务器失败"
            System.out.println("连接服务器失败");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //我认为不应该为私有
    public void disConnect() {
        try {
            if (!isLogin) {
                return;
            }
            System.out.println("客户端发送下线请求");
            ChatMessage message = new ChatMessage(
                    new ChatMessageHeader.ChatBuilder()
                            .type_message(MessageType.LOGOUT)
                            .sender(username)
                            .timestamp(System.currentTimeMillis())
                            .build(), null);
            clientChannel.write(ByteBuffer.wrap(ProtoStuffUtil.serialize(message)));
            // Thread.sleep(1000000000);//这里可以取消注释，多线程调试一下run方法，发现客户端发送完消息后服务器关闭了连接，
            // 但是客户端的Socket 的读事件（FD_READ）仍然起作用，也就是说调试的时候还是会进入到run方法的if(selectionKey.isReadable())中。导致读到的数据为null。
            // 因此发送消息后就要调用shutdown，待线程自己处理完当前事务后关闭线程。
            //如果在发送请求下线消息后，在此等待一会，接受消息线程会收到head-null,body-null的消息。
            if (!isConnected) {
                return;
            }

            //listener.shutdown();//这样客户端接收消息的线程在处理完手头的东西后就就关闭了。不过这里我在下线消息接收后调用了。
            //如果发送消息后马上断开连接，那么消息可能无法送达，导致服务器无法关闭他那边的socket以及通道。于是推迟一下，待发送后自己线程停止，然后关闭通道。
            Thread.sleep(1000);
            clientChannel.socket().close();
            clientChannel.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }



    //发送信息
    public void send(String content) {
        if (!isLogin) {
            return;
        }
        ChatMessage message;
        try {
            //普通模式
            if (content.startsWith("@")) {
                String[] slices = content.split(":");
                String receiver = slices[0].substring(1);
                message = new ChatMessage(
                        new ChatMessageHeader.ChatBuilder()
                                .type_message(MessageType.NORMAL)
                                .sender(this.username)
                                .receiver(receiver)
                                .timestamp(System.currentTimeMillis())
                                .build()
                        ,slices[1].getBytes(charset)
                );
            }else {
                //广播模式
                message = new ChatMessage(
                        new ChatMessageHeader.ChatBuilder()
                                .type_message(MessageType.BROADCAST)
                                .sender(this.username)
                                .timestamp(System.currentTimeMillis())
                                .build(), content.getBytes(charset));
            }
            System.out.println(new String(message.getBody()));
            clientChannel.write(ByteBuffer.wrap(ProtoStuffUtil.serialize(message)));
        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 用于接收信息的线程
     */
    private class ReceiverHandler implements Runnable {
        private boolean connected = true;
        public void shutdown() {
            connected = false;
        }

        public void run() {
            try {
                while (connected) {
                    int size = 0;
                    selector.select();//等待通道
                    for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
                        SelectionKey selectionKey = it.next();
                        it.remove();
                        //Tests whether this key's channel is ready for reading
                        if (selectionKey.isReadable()) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            while ((size = clientChannel.read(buf)) > 0) {
                                buf.flip();
                                baos.write(buf.array(), 0, size);
                                buf.clear();
                            }
                            byte[] bytes = baos.toByteArray();
                            baos.close();
                            ChatResponse response = ProtoStuffUtil.deserialize(bytes, ChatResponse.class);
                            handleResponse(response);
                        }
                    }
                }
            }catch (IOException e) {
                System.out.println("服务器关闭，请重新尝试连接");
                isLogin = false;
            }
        }

        private void handleResponse(ChatResponse response) {
            ChatResponseHeader header = response.getHeader();
            switch (header.getType()) {
                case PROMPT://PROMPT提示
                    if (header.getResponseCode() != null) {
                        ChatResponseCode code = ChatResponseCode.fromCode(header.getResponseCode());
                        if (code == ChatResponseCode.LOGIN_SUCCESS) {
                            isLogin = true;
                            System.out.println("登录成功");
                        } else if (code == ChatResponseCode.LOGOUT_SUCCESS) {
                            System.out.println("下线成功");
                            //我觉得这里应该加入线程关闭的操作，而不是在发送退出消息时调用shutdown。
                            listener.shutdown();
                            break;
                        }
                    }
                    String info = new String(response.getBody(), charset);//拿到消息，自己处理==========================
                    System.out.println(info);
                    break;

                case NORMAL://消息
                    //拿到response后自己处理
                    byte[] body = response.getBody();
                    String content = new String(body);
                    System.out.println(content);
                    break;
//				case FILE:
//					try {
//						String path = JOptionPane.showInputDialog("请输入保存的文件路径");
//						byte[] buf = response.getBody();
//						FileUtil.save(path, buf);
//						if(path.endsWith("jpg")){
//							//显示该图片
//							new PictureDialog(ChatClient.this, "图片", false, path);
//						}
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
                default:
                    break;
            }
        }
    }
}
