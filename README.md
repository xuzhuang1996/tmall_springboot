[TOC]

# TmallBySpringboot

## restful标准：

- 资源名称用复数，即使用categories而不是category
- CRUD：
  - 增加post
  - 删除delete
  - 修改put
  - 查询get
- id参数的传递都使用/id方式。编辑修改:/categories/90
- 其他参数采用?name=value/如分页参数/categories?start=6
- 返回数据,针对控制器
  - 查询多个 返回json属组
  - 增加，查询，修改都返回当前json数组
  - 删除返回null


## 对原教程的改进：

1.查询分页：教程里面选择对Page接口进行扩展，定义一个类Page4Navigator。我觉得繁琐，因为查询后的Page数据转json后本身自带各种分页数据。

## 亮点难点：
1. [缓存中对线程异常的处理](cache.md)
2. 对redis做集群，虚拟机的构建，设置IP地址等。以及redis配置文件的设置，之前一直在博客找用Ruby，后面直接在官网找，因为版本变动，配置方式也发生了变化，更简单了。解决方式就是：官网

## 错误处理：

1. HttpRequestMethodNotSupportedException: Request method 'GET' not supported，一般都是没有写请求对应的处理方法，比如页面发出了ajax请求，但是控制器没有对应的方法来映射处理

2. JPA中的getOne：在PropertyService中调用CategoryService的get方法时，即使cid已经传进去，依然返回无效的Category。

   原因：基本的区别是getOne延迟加载而findOne不是。不过现在JPA已经移除了findone方法。只能用repository.findById(productId).get()。

3. 如果控制器返回没问题，那么试着注释一下前端启动时调用的函数。找准问题来源。

4. 出现StackOverflowError错误看看是否进入死循环。

5. 如果categorys转json传前端时，category里面有products属性，然后就会遍历product，而product又有category属性，于是进入死循环。

6. server.servlet.context-path=/tmall_springboot，如果前台请求路径http://localhost:8888/tmall_springboot/SecProduct ，但是最终发现请求的就是缺tmall_springboot即http://localhost:8888/SecProduct ，很有可能是前台请求路径写成了/SecProduct，正确的写法是SecProduct

## 查看用法
1. 实体类：从Category开始，之后查看Property类的处理（多对一）。如果某些字段类中有但数据库中没有，则需要注解进行忽略。

   - product表是没有ProductImage信息的，但是product类有该字段，只是运用Transient注解在存储时将其忽略，因此取出来Product时是需要设置这些被忽略的字段。因此可以在productService也可以在控制器中设置。我基于控制器只写逻辑代码的原则，将设置放在了服务层。
   - product表是没有PropertyValue信息的，同时product类也没有PropertyValue字段。因此在创建product时PropertyValue也要跟着创建，同时进行绑定。

2. DAO：继承JPA仓库。如果有特殊需求，需要根据其他实体对象查询该对象，增加方法findByClassName。

3. 在页面跳转控制器中，如果写好跳转的HTML，页面出不来，就可以去页面查需要哪些数据了。

4. 注册拦截器，然后在继承WebMvcConfigurationSupport对其进行配置。由于WebMvcConfigurerAdapter过时了，
才使用的WebMvcConfigurationSupport。在spring中配置WebMvc时有两种方法，一种是继承WebMvcConfigurationSupport.而WebMvcConfigurer只是WebMvcConfigurationSupport的一个扩展类
   >替换的理由是java8之后增加了接口默认的实现的用法：通过在方法名前面加default，就可以写实现。好处是：对于一些公有的方法，直接使用默认的方法，就不用在实现类中写重复代码了。
在这里过时的原因：类A可能同时需要WebMvcConfigurer的某一个方法，如果直接继承需要实现其所有方法，于是新建一个适配器类B，实现这个接口的某个方法，其他方法实现为空，然后A继承B，这样A就只需要重写或者继承B的目的方法，实现了框架的耦合。
但是这样需要多写一个适配器类，现在java8以后，只需要重写目的方法，而不用实现所有方法。

5. 缓存，org.springframework.cache.annotation.Cacheable;
   - @CacheConfig(cacheNames="categories")，指明存在哪个缓存上。我这里只在目录上使用了缓存
   - @Cacheable注解，直接使用“#参数名”或者“#p参数index”。如果多个参数，使用包含所有参数的hashCode作为key
   - @CacheEvict是用来标注在需要清除缓存元素的方法或类上的
   - `keys *`查询所有，或者以。。。开头的缓存
   - springboot 默认生成RedisTemplate<Object,Object>，加@ConditionalOnMissingBean注解后，[来源](https://www.cnblogs.com/zeng1994/p/03303c805731afc9aa9c60dbbd32a323.html)。如果Spring容器中有了RedisTemplate对象了，这个自动配置的RedisTemplate不会实例化。因此我们可以直接自己写个配置类，配置RedisTemplate。
   - Application类加注解@EnableCaching，否则配置完RedisTemplate后不会生效。
   
6. [多对多](https://hellokoding.com/jpa-many-to-many-relationship-mapping-example-with-spring-boot-maven-and-mysql/)

   维护端,在维护端进行操作（在userservice上加的redis）：
   
        @ManyToMany(cascade=CascadeType.REFRESH, fetch = FetchType.EAGER)//如果不写加载方式，默认懒加载，出错
        @JoinTable(name = "user_coupon",
                joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
                inverseJoinColumns = @JoinColumn(name = "coupon_id", referencedColumnName = "id"))
        private Set<Coupon> coupons;//一个用户有多个优惠券
        
   另一端优惠券：
   
        @ManyToMany(mappedBy = "coupons")
        private Set<User> users = new HashSet<>();
        
   之后，在spring boot中，如果在对象层次设置多对多的值，就会添加到数据库的新建表user_coupon中.
   
7. redis集群；
   - 每个服务器开启redis服务后，才可以创建.开启
   - 创建集群redis-cli --cluster create 127.0.0.1:7000 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005  --cluster-replicas 1。这些IP可以换成外机地址
   - 如果redis没有密码，就不要在spring boot的配置文件写spring.redis.password=***。
   - 缓存将随机写入某一个集群，因此，当没有报错的时候，去其他几个服务器找找键值在哪`redis-cli -p 7000`,`keys *`。
   
8. [设置过期时间](https://segmentfault.com/q/1010000015203664/a-1020000015204174)
9. [用户聊天功能](chat.md) 
10. [不用redis实现缓存](cache.md)
11. 加日志。为了将所有错误信息输出到日志，就在定义的全局异常处理类中加入日志对象，进行打印。但是对于某些另开的线程，如果无法处理异常，就自行用try进行捕获，然后输出。一般用法就是在配置文件加日志地点，然后用`private static Logger logger =LoggerFactory.getLogger(AutoReloadCache.class);`，但是如果懒得每个都写对象，可以考虑加@Slf4j，引用自lombok.extern.slf4j.Slf4j;然后将为被注解的类生成一个log对象。可以见别人chat的工程。
12. .gitignore在已经push后，想重新添加忽略文件：
    
        git rm -r --cached .
        git add .
        git commit -m 'update .gitignore'
