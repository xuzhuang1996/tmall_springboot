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
1. 数据库中没有存储图片，一个产品一对多张图片，如何处理：取出产品的时候需要进行设置图片。


## 错误处理：

1. HttpRequestMethodNotSupportedException: Request method 'GET' not supported，一般都是没有写请求对应的处理方法，比如页面发出了ajax请求，但是控制器没有对应的方法来映射处理

2. JPA中的getOne：在PropertyService中调用CategoryService的get方法时，即使cid已经传进去，依然返回无效的Category。

   原因：基本的区别是getOne延迟加载而findOne不是。不过现在JPA已经移除了findone方法。只能用repository.findById(productId).get()。

3. 如果控制器返回没问题，那么试着注释一下前端启动时调用的函数。找准问题来源。

4. 出现StackOverflowError错误看看是否进入死循环。

5. 如果categorys转json传前端时，category里面有products属性，然后就会遍历product，而product又有category属性，于是进入死循环。


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