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




## 错误处理：

1. HttpRequestMethodNotSupportedException: Request method 'GET' not supported，一般都是没有写请求对应的处理方法，比如页面发出了ajax请求，但是控制器没有对应的方法来映射处理

2. JPA中的getOne：在PropertyService中调用CategoryService的get方法时，即使cid已经传进去，依然返回无效的Category。

原因：基本的区别是getOne延迟加载而findOne不是。不过现在JPA已经移除了findone方法。只能用repository.findById(productId).get()。

3. 如果控制器返回没问题，那么试着注释一下前端启动时调用的函数。找准问题来源。

## 查看用法
1. 实体类：从Category开始，之后查看Property类的处理（多对一）。如果某些字段类中有但数据库中没有，则需要注解进行忽略。

- product表是没有ProductImage信息的，但是product类有该字段，只是运用Transient注解在存储时将其忽略，因此取出来Product时是需要设置这些被忽略的字段。因此可以在productService也可以在控制器中设置。我基于控制器只写逻辑代码的原则，将设置放在了服务层。
- product表是没有PropertyValue信息的，同时product类也没有PropertyValue字段。因此在创建product时PropertyValue也要跟着创建，同时进行绑定。

2. DAO：继承JPA仓库。如果有特殊需求，需要根据其他实体对象查询该对象，增加方法findByClassName。
3. 