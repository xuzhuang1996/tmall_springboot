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
            //登录时，源码的处理用socket进行判断，虽然网页的登录成功，但不一定能登录服务器的数据。这里先留着
            login();
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
            logout();
            if (!isConnected) {
                return;
            }
            listener.shutdown();
            //如果发送消息后马上断开连接，那么消息可能无法送达
            Thread.sleep(10);
            clientChannel.socket().close();
            clientChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //本来想把这个登录去掉。但是还是分开了。作为服务器的登录吧。
    private void login() {
        ChatMessage message = new ChatMessage(
                new ChatMessageHeader.ChatBuilder()
                        .type_message(MessageType.LOGIN)
                        .sender(username)
                        .timestamp(System.currentTimeMillis())
                        .build(),null);//密码不要
        try {
            clientChannel.write(ByteBuffer.wrap(ProtoStuffUtil.serialize(message)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logout() {
        if (!isLogin) {
            return;
        }
        System.out.println("客户端发送下线请求");
//		Message message = new Message(
//				MessageHeader.builder()
//						.type(MessageType.LOGOUT)
//						.sender(username)
//						.timestamp(System.currentTimeMillis())
//						.build(), null);
//		try {
//			clientChannel.write(ByteBuffer.wrap(ProtoStuffUtil.serialize(message)));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
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
//            System.out.println(response);
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
