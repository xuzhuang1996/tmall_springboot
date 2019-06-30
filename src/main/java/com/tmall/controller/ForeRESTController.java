package com.tmall.controller;

import com.tmall.chat_client.ChatClient;
import com.tmall.chat_client.ClientManage;
import com.tmall.dto.Exposer;
import com.tmall.dto.Result;
import com.tmall.pojo.*;
import com.tmall.service.*;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class ForeRESTController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    ReviewService reviewService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    OrderService orderService;
    //home.html中需要如下数据
    //homePage.html：
        //categoryAndcarousel.html
            //categoryMenu:categories
            //productsAsideCategorys:categories这个页面ps in c.productsByRow
            //carousel
        //homepageCategoryProducts.html:categories,这个页面除了需要分类，还需要分类下的产品，需要需要有一个填充。v-for="p,index in c.products"
    //top.html:
    //综上所述，该处需要的数据categories，有products属性，有productsByRow属性。
    @GetMapping("/forehome")
    public List<Category> home(){
        //这里就需要fill了
        List<Category>categories = categoryService.list();
        //给每个分类填充产品，遵循控制器只写逻辑的原则，将fill写在productService中。
        productService.fill(categories);
        productService.fillByRow(categories);
        categoryService.removeCategoryFromProduct(categories);
        return categories;//注意，返回数据后，在homepage页面中ajax的数据要对应。
    }


    @PostMapping("/foreregister")
    public Result register(@RequestBody User user){
        String name =  user.getName();
        String password = user.getPassword();
        name = HtmlUtils.htmlEscape(name);
        user.setName(name);

        boolean exist = userService.isExist(name);

        //注册的时候，有几种可能，因此需要统一数据，用result数据结构。
        if(exist){
            String message ="用户名已经被使用,不能使用";
            return Result.fail(message);
        }
//        String salt = new SecureRandomNumberGenerator().nextBytes().toString();
//        int times = 2;
//        String algorithmName = "md5";
//
//        String encodedPassword = new SimpleHash(algorithmName, password, salt, times).toString();
//
//        user.setSalt(salt);
//        user.setPassword(encodedPassword);

        userService.Add(user);

        return Result.success();
    }

    @PostMapping("/forelogin")
    public Result Login(@RequestBody User user, HttpSession session){
        //验证
        String name =  user.getName();
        name = HtmlUtils.htmlEscape(name);
        String passward = user.getPassword();
        User userFact = userService.getByName(name);
        if(userFact==null || !userFact.getPassword().equals(passward)){
            String message ="账号密码错误";
            return Result.fail(message);
        }else {
            session.setAttribute("user", userFact);
            //============================================chat=================================================
            ChatClient chatClient=new ChatClient(userFact.getName());
            chatClient.launch();
            ClientManage.map.put(userFact.getName(),chatClient);
            //=================================================================================================
            return Result.success();
        }
    }


    //img:firstProductImage----pi in p.productSingleImages\
    //productDetail:pv in pvs----pi in p.productDetailImages
    //productPage:category---response.data.data.product;因此这个应该是用result封装了。
    //reviewPage:r in reviews
    @GetMapping("/foreproduct/{pid}")
    public Object prouctPage(@PathVariable("pid")int pid){
        Product product = productService.get(pid);
        product.setProductSingleImages(productImageService.listSingleProductImages(pid));
        product.setProductDetailImages(productImageService.listDetailProductImages(pid));

        List<PropertyValue> pvs = propertyValueService.list(pid);
        List<Review> reviews = reviewService.list(product);
        productService.setSaleAndReviewNumber(product);
        productImageService.setFirstProdutImage(product);

        //如果一个页面需要多条数据，且不同，用map装呀
        Map<String,Object> map= new HashMap<>();
        map.put("product", product);
        map.put("pvs", pvs);
        map.put("reviews", reviews);
        return Result.success(map);
    }

    @GetMapping("/forecheckLogin")
    public Result checkLogin(HttpSession session){
        User user = (User)session.getAttribute("user");
        if(user==null)
        {
            String message = "没有登录";
            return Result.fail(message);
        }else
            return Result.success();
    }


    @GetMapping("/forecategory/{cid}")
    public Category category(@PathVariable("cid") int cid,@RequestParam(value = "sort", defaultValue = "all")String sort){
        Category category = categoryService.get(cid);
        productService.fill(category);
        categoryService.removeCategoryFromProduct(category);//填充之后，产品本身是有category的，为了防止转json进入死循环，删掉product的category。
        if(null!=sort){
            switch(sort){
                case "review":
                    category.getProducts().sort((p1,p2)->p2.getReviewCount() - p1.getReviewCount());
                    break;
                case "date" :
                    category.getProducts().sort((p1,p2)->p2.getCreateDate().compareTo(p1.getCreateDate()));
                    break;

                case "saleCount" :
                    category.getProducts().sort((p1,p2)->p2.getSaleCount() - p1.getSaleCount());
                    break;

                case "price":
                    category.getProducts().sort((p1,p2)->(int)(p1.getPromotePrice() - p2.getPromotePrice()));
                    break;

                case "all":
                    category.getProducts().sort((p1,p2)->p2.getReviewCount() * p2.getSaleCount() - p1.getReviewCount() * p1.getSaleCount());
                    break;
            }
        }
        return category;
    }


    @PostMapping("/foresearch")
    public Object search( String keyword){
        if(null==keyword)
            keyword = "";
        //List<Product> ps= productService.search(keyword,0,20);
        List<Product>ps=productService.listByCategory(categoryService.list().get(0));
        productImageService.setFirstProdutImages(ps);
        productService.setSaleAndReviewNumber(ps);
        return ps;
    }

    @GetMapping("/forebuyone")
    public Integer buyone(@RequestParam("pid")int pid,@RequestParam("num")int num,HttpSession session){
        return buyoneAndAddCart(pid,num,session);
    }

    @GetMapping("/forebuy")
    public Result buy(@RequestParam("oiid")String[] oiid,HttpSession session){
        List<OrderItem> orderItems = new ArrayList<>();
        float total = 0;
        User user =(User)  session.getAttribute("user");
        user = userService.get(user.getId());//估计是会话中的user没有更新优惠券信息。

        float coupon = 0;
        if(user.getCoupons().size()>0)
            coupon = user.getCoupons().get(0).getPromoteMoney();

        for (String strid : oiid) {
            int id = Integer.parseInt(strid);
            OrderItem oi= orderItemService.get(id);
            total +=oi.getProduct().getPromotePrice()*oi.getNumber();
            //这里由于orderItems订单项传到前台，但是orderItems里面包括user，而user包括coupon，而coupon包括user，
            // 因此转json进入死循环，需要remove这个属性
            oi.getUser().setCoupons(null);
            orderItems.add(oi);
        }

        productImageService.setFirstProdutImagesOnOrderItems(orderItems);

        session.setAttribute("ois", orderItems);

        Map<String,Object> map = new HashMap<>();
        map.put("orderItems", orderItems);
        map.put("total", total - coupon);
        return Result.success(map);
    }

    @GetMapping("foreaddCart")
    public Object addCart(int pid, int num, HttpSession session) {
        buyoneAndAddCart(pid,num,session);
        return Result.success();
    }

    private int buyoneAndAddCart(int pid, int num, HttpSession session) {
        Product product = productService.get(pid);
        int oiid = 0;

        User user =(User)  session.getAttribute("user");
        boolean found = false;
        List<OrderItem> ois = orderItemService.listByUser(user);
        for (OrderItem oi : ois) {
            if(oi.getProduct().getId()==product.getId()){
                oi.setNumber(oi.getNumber()+num);
                orderItemService.update(oi);
                found = true;
                oiid = oi.getId();
                break;
            }
        }

        if(!found){
            OrderItem oi = new OrderItem();
            oi.setUser(user);
            oi.setProduct(product);
            oi.setNumber(num);
            orderItemService.add(oi);
            oiid = oi.getId();
        }
        return oiid;
    }

    //像购物车这种，就直接用登录拦截器来判断。而不是返回Result
    @GetMapping("/forecart")
    public List<OrderItem> cart(HttpSession session){
        User user= (User)session.getAttribute("user");
        List<OrderItem>orderItems = orderItemService.listByUser(user);;
        productImageService.setFirstProdutImagesOnOrderItems(orderItems);
        return orderItems;
    }

    @GetMapping("forechangeOrderItem")
    public Object changeOrderItem( HttpSession session, int pid, int num) {
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录");

        List<OrderItem> ois = orderItemService.listByUser(user);
        for (OrderItem oi : ois) {
            if(oi.getProduct().getId()==pid){
                oi.setNumber(num);
                orderItemService.update(oi);
                break;
            }
        }
        return Result.success();
    }

    @GetMapping("foredeleteOrderItem")
    public Object deleteOrderItem(HttpSession session,int oiid){
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录");
        orderItemService.delete(oiid);
        return Result.success();
    }

    @PostMapping("forecreateOrder")
    public Object createOrder(@RequestBody Order order,HttpSession session){
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录");
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);
        order.setOrderCode(orderCode);
        order.setCreateDate(new Date());
        order.setUser(user);
        order.setStatus(OrderService.waitPay);
        List<OrderItem> ois= (List<OrderItem>)  session.getAttribute("ois");

        float total =orderService.add(order,ois);

        Map<String,Object> map = new HashMap<>();
        map.put("oid", order.getId());
        map.put("total", total);

        return Result.success(map);
    }

    @GetMapping("forepayed")
    public Object payed(int oid) {
        Order order = orderService.get(oid);
        order.setStatus(OrderService.waitDelivery);
        order.setPayDate(new Date());
        orderService.update(order);
        return order;
    }

    @GetMapping("forebought")
    public Object bought(HttpSession session) {
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录");
        List<Order> os= orderService.listByUserWithoutDelete(user);
        orderService.removeOrderFromOrderItem(os);
        return os;
    }

    @GetMapping("foreconfirmPay")
    public Object confirmPay(int oid) {
        Order o = orderService.get(oid);
        orderItemService.fill(o);
        orderService.cacl(o);
        orderService.removeOrderFromOrderItem(o);
        return o;
    }

    @GetMapping("foreorderConfirmed")
    public Object orderConfirmed( int oid) {
        Order o = orderService.get(oid);
        o.setStatus(OrderService.waitReview);
        o.setConfirmDate(new Date());
        orderService.update(o);
        return Result.success();
    }

    @PutMapping("foredeleteOrder")
    public Object deleteOrder(int oid){
        Order o = orderService.get(oid);
        o.setStatus(OrderService.delete);
        orderService.update(o);
        return Result.success();
    }

    //领取优惠券foregetCoupon
    @GetMapping("foregetCoupon/{CouponId}")
    public Object GetCoupon(HttpSession session,@PathVariable("CouponId")int CouponId) {
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return Result.fail("未登录");
        userService.AddCoupon(user.getId(),CouponId);
        return Result.success();
    }


    @Autowired
    SeckillService seckillService;
    //秒杀的控制方法
    @PostMapping("foreseckill/{pid}/exposer")
    public Object getExposer(@PathVariable("pid") int pid){
        //进入这个方法进行请求暴露地址，说明此时客户端时间已经到了，将返回是否暴露地址。至于怎么判断交给服务层，控制器只写基本逻辑。
        try {
            Exposer exposer = seckillService.exportSeckillUrl(pid);//请求正常处理，就返回结果
            return Result.success(exposer);
        }catch (Exception e){//如果请求出现异常，就返回错误
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("foreseckill/{pid}/{md5}/execution")
    public Object execution(@PathVariable("pid") int pid ,@PathVariable("md5") String md5){
        //执行秒杀
        //需要注意的是，别人可以通过浏览器拿到地址http://localhost:8888/tmall_springboot/foreseckill/87/04bd86da9e77c909bb78797ba269ad22/execution
        //如果他改了pid即87的值，就可以秒杀其他产品了，但是如果他改了，那么md5值就不是这个了，就访问不到了啊哈哈。
        return pid;
    }


    //========================chat======================
    @PostMapping("/forechat")
    public void chat(@RequestParam("receiver")String receiver,@RequestParam("content")String content,HttpSession session){
        User user =(User)  session.getAttribute("user");
        if(user!=null){
            //当前进度：前台传用户名+内容过来了。后面要做的，根据user、receive、content，传数据到下一个项目。
            //当前问题：依赖问题，UserManage归属问题，tmall想访问在线人数。如何发送消息：最好由客户端发送，解耦
            //解决：tmall接受用户发送的数据，http请求发送给ChatServer。错。这里没有网页请求。因此唯一的解决方式，将客户端合并进tmall
            //另外，tmall也需要维护一个在线列表。保存用户的客户端
            ChatClient chatClient=ClientManage.map.get(user.getName());
            //客户端对象直接从管理员的map中取
            chatClient.send("@"+receiver+":"+content);
        }
    }

}
