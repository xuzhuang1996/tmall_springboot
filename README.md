# TmallBySpringboot

restful标准：

资源名称用复数，即使用categories而不是category
CRUD：
增加post
删除delete
修改put
查询get
id参数的传递都使用/id方式。编辑修改:/categories/90
其他参数采用?name=value/如分页参数/categories?start=6
返回数据
查询多个 返回json属组
增加，查询，修改都返回当前json数组
删除返回null
对原教程的改进：

1.查询分页：教程里面选择对Page接口进行扩展，定义一个类Page4Navigator。我觉得繁琐，因为查询后的Page数据转json后本身自带各种分页数据。

查看用法，建议从CategoryController开始，里面介绍大部分springboot的使用

错误处理：

1.HttpRequestMethodNotSupportedException: Request method 'GET' not supported，一般都是没有写请求对应的处理方法，比如页面发出了ajax请求，但是控制器没有对应的方法来映射处理
