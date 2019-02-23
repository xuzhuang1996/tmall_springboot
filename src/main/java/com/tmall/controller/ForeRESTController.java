package com.tmall.controller;

import com.tmall.dto.Result;
import com.tmall.pojo.Category;
import com.tmall.pojo.User;
import com.tmall.service.CategoryService;
import com.tmall.service.ProductService;
import com.tmall.service.UserService;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
public class ForeRESTController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
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

        userService.add(user);

        return Result.success();
    }

    @PostMapping("forelogin")
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
            return Result.success();
        }
    }
}
