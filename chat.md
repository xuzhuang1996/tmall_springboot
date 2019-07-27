[来源](https://github.com/songxinjianqwe/Chat)
## 设计功能
1. 当前登陆的用户可以查看当前所有登陆的用户列表。然后选择其中用户发送消息。接收者不存在或已下线时将无法发送，并提示：接收者不存在或已下线
2. 用户收到其他人发送的消息时会提醒。因此这里有一个监听。

## 服务端
1. 维护所有登陆用户信息：登陆下线，服务端需要处理。用户已上线时这种通知也需要。
2. MessageHandler抽象类，里面有个重要方法broadcast。
3. 服务端代码参考ChatServer.java

## 客户端
1. 他直接来一个客户端继承了frame，我这里就是前端网页吧。然后登陆时就是连接服务器。

## 问题：
1. 还不清楚他的HTTP这个包的用途，task包应该核心？
2. 后期有空增加聊天时发送图片/文件的功能。以及整理登录的思路。加入线程池。

## 编码过程：
1. 写服务端。这个服务端工程新建，不与原先工程在一起。客户端就在原先的工程中吧。
   - 服务器需要监听是否有客户端连接。即ListenerThread
     - 然后需要处理客户端的连接请求，handleAcceptRequest
     - 需要处理客户端发送的数据。即ReadEventHandler。客户端发送的消息分下线通知、发送给其他人的消息等，因此需要分开处理。均继承ChatMessageHandler。到底是normal还是提示，分别处理。利用@Component("normal")把普通pojo实例化到spring容器中，然后通过类名访问applicationcontext.getBean("normal")。
   - UserManager，服务器的管理员。当有用户登录时注册其channel到onlineUsers，这样服务器可以获取其channel进行发送数据；当用户下线，管理员将其通道置空，这样就不能发送数据。这里所有用户信息存在数据库中，ChatServer工程通过httpclient获取用户数据。
     
2. 客户端。这个工程基于原始的tmall工程。
   - 初始化客户端的socket数据。
     - 登录，作为浏览器登录的附属品。除了网站的登录cookie，也作为服务器的登录附加。
     - 退出
   - 发送数据。这里为了数据统一管理，将发送服务器的数据作为message，而消息又分为消息体跟消息头。
     - 这里源码用的lombok的注解Builder，我选择直接使用链式建造者模式来编码。
   - 接受数据。同样为了管理数据，将接受的数据作为ChatResponse，即来自服务器的回应。
3. 依赖（没处理好，主工程即原先的tmall）
   - 常见工具放入common工程
   - server工程依赖主工程的user
   - server跟主工程都依赖common
   
4. 开始通信
   - 首先启动ChatServer、tmall。
   - 客户端：用户AB上线，即在网页主页登录，输入用户信息，登录时执行了initNetWork，连接ChatServer服务器后就发送上线消息给ChatServer服务器，然后服务器解析消息后，用LoginMessageHandler将用户注册到UserManager。注册成功后服务器返回登录成功的消息，以及广播上线消息，这里服务器的消息统一用PromptMsgProperty来表示消息体。客户端登录成功后将注册到tmall的manager下，便于发送消息使用`ClientManage.map.put(userFact.getName(),chatClient);`
   - 服务器：ListenerThread一直监听请求，handleAcceptRequest()函数处理客户端连接，当客户端open后获取socket，连接到服务器后服务器将可以监听该客户端通道的read事件。然后服务器收到用户上线请求消息，转到ReadEventHandler开始读取消息内容，SelectionKey是传入的用户的通道对象，服务器的manager将该通道SelectionKey.channel注册到onlineUsers，用于后面发目标用户消息，直接从里面取通道进行传递。处理完用户的消息后，开始respose,首先是返回用户登录成功，然后用户数onlineUsers++，同时广播哪位用户上线,这里用到了String.format。到此登录结束。
      > 比如枚举中`LOGIN_BROADCAST = "%s用户已上线";`,这里`String.format(LOGIN_BROADCAST,name)`。`%s`就是字符串占位符。
   - 用户A向用户B发送消息，消息先发送到服务器（流的方式），服务器解析消息（先将流转字节数组，再重新生成消息对象），获取接受对象，服务器向接受对象发送消息（服务器通过onlineUsers获取目标用户的通道channel，然后向目标通道write，这个通道在用户下线的时候被设置为null，如果该用户通道不为空，则在线，否则不在线）。这里就需要服务器来管理用户UserManager，然后向在线用户发消息。
   - 下线时，需要注意：
     - 客户端退出时，分2个关闭。1是处理线程的关闭，2是客户端关闭与服务器的连接。这里需要注意多线程调试：一般新开的线程，后面加断点时是不会进去的，只在主线程的断点有效，需要选择正确的线程调试方法。
     - 再就是注意粘包问题：服务器在处理的时候发送了2个消息给客户端，源码没有sleep处理，因此只收到了广播的消息。
     - 现在开始，服务器客户端发送推出消息到服务器，服务器发送消息后关闭了通道，此时客户端的线程应该接受消息后处理完自己的该消息后就关闭线程。而不是源码里面，在disconnect调用shutdown.
   
5. 难点：tmall与聊天项目的结合。
   1. 粘包问题：之前退出时，服务器一次性发出退出成功+广播退休消息，但是客户端退出报异常错误。调试查看客户端接受的消息情况，发现无论怎么设置断点，都进不去客户端的消息处理线程代码，Google后发现需要多线程调试，于是在多线程调试下发现客户端只收到了广播消息，而没有收到退出消息。Google后发现是粘包问题，于是在发送的数据之间加入sleep。为啥出现异常：因为收到退出消息后会关闭消息处理线程，但是明显没有关闭该线程，然后服务器就关闭了channel，虽然无数据可读，但是selectionKey.isReadable()为true，于是读到空数据，出现NPE。
      > 粘包：NIO的数据要通过channel放到一个缓存池ByteBuffer中，然后再从这个缓冲池中读出数据，而IO的模式是直接从inputstream中read。如果一次性读入两个及两个以上的数据，则无法分辨两个数据包的界限问题，也就造成了粘包。解决方式一般是：定义数据发送协议格式，数据头中包含包体的长度大小，接收方首先读取包体的长度，然后按照包体的长度进行一次或多次读取数据，从而组装成完整合法的数据内容。
   2. 服务器需要处理几种不同类型的客户端消息：于是在消息中加入消息头。里面设置消息的类型。于是采用反射的方式，先根据消息类型，拿到对应处理类的类名，调用对应方法。如果没有spring 就反射，有就直接根据类名拿对象。
   3. chatServer需要所有用户信息，但是两个不同工程数据不能共享，于是通过httpclient获取tmall项目的用户数据，获取时为字符串，因此需要转换——JsonUtils工具包。`List<User> list = JsonUtils.jsonToList(json,User.class);`这样就获取所有用户信息。
   
   
5. 异常
   - 反射异常 java.lang.InstantiationException处理。在ChatServer中的`ChatMessage message = ProtoStuffUtil.deserialize(bytes, ChatMessage.class);`中反射生成对象失败。解决：使用反射的时候编写使用class实例化其他类的对象的时候，一定要自己定义无参的构造方法
   
6. 查看端口占用`netstat -aon|findstr "9000"`


## 进度表
1. 2019.6.23
   - 当前进度：前台传用户名+内容过来了。后面要做的，根据user、receive、content，传数据到下一个项目。
   - 当前问题：依赖问题，UserManage归属问题，tmall想访问在线人数。如何发送消息：最好由客户端发送，解耦
2. 2019.6.30
   - 当前进度：解决23号问题，原先想法是tmall接受用户发送的数据，http请求发送给ChatServer。错。这里没有网页请求。因此唯一的解决方式，将客户端合并进tmall。tmall也需要维护一个在线列表。保存用户的客户端。
   - 另外：user登录时就将其注册进tmall以及ChatServer的用户管理中。当前完成：tmall的用户退出正在完成，下次待完成：退出时发送消息给chatServer时chatServer的处理,以及退出时自身线程是否还要关闭等等。完成这个后可以试着通信。在一台机子上登录2个用户：测试浏览器的session针对浏览器还是针对网页（因为退出时有删除session的user，因此担心同时在一个机器上登录，退出一人时会不会将所有人退出）
3. 2019.7.7
   - 登录没有问题。现在退出的时候报错，下周解决。
4. 2019.7.13
   - 之前遇到的问题是：服务器关闭通道时，客户端由于没有线程还在运行，虽然无数据可读，但是[selectionKey.isReadable()为true](https://segmentfault.com/q/1010000010655743)，于是出现读到空数据，出现NPE。解决后现在正常退出，后面解决基本界面问题后，准备加入线程池。
