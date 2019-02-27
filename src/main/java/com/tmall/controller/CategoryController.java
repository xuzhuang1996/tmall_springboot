package com.tmall.controller;


import com.tmall.pojo.Category;
import com.tmall.service.CategoryService;
import com.tmall.util.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

//RestController:对每个方法的返回值都会直接转换为json,不需在方法前面加@ResponseBody,但是不能返回jsp,html页面，视图解析器无法解析jsp,html页面
@RestController
public class CategoryController {
    //获取参数：URL中？后面的参数获取RequestParam（这里如果想给默认值）
    @Autowired
    CategoryService categoryService;
    @GetMapping("/categories")
    public Page<Category> listCategory(@RequestParam(value = "start", defaultValue = "0") int start,
                                       @RequestParam(value = "size", defaultValue = "5") int size) throws Exception {
        start = start<0?0:start;
        return categoryService.listCategory(start,size);
    }

    // 获取参数：直接post方式。自动注入
    // ajax带着数据image，name请求该路径下的方法，Category有一个name属性（一个也注入,这个时候是没有id的）于是注入对象。
    // 如果是JDBC还需要自己new这个对象
    @PostMapping("/categories")
    public Object add(Category bean, MultipartFile image, HttpServletRequest request) throws Exception{
        categoryService.add(bean);
        saveOrUpdateImageFile(bean,image,request);
        return bean;
    }

    //获取参数：URL中/拿值，PathVariable获取参数
    @DeleteMapping("/categories/{id}")
    public String deleteCategory(@PathVariable("id") int id,HttpServletRequest request) throws Exception {
        categoryService.delete(id);
        File imageFolder=new File(request.getServletContext().getRealPath("img/category"));
        File file=new File(imageFolder,id+".jpg");
        file.delete();
        return null;
    }

    //edit页面给来了image、name，URL还带着id。自动注入的时候bean是有id的。将id与name注入到bean
    @PutMapping("/categories/{id}")
    public Category updateCategory(Category bean, MultipartFile image, HttpServletRequest request) throws Exception{
        categoryService.edit(bean);
        if(image!=null){
            saveOrUpdateImageFile(bean,image,request);
        }
        return bean;
    }

    @GetMapping("/categories/{id}")
    public Category get(@PathVariable("id") int id) throws Exception {
        Category bean=categoryService.get(id);
        return bean;
    }

    public void saveOrUpdateImageFile(Category bean, MultipartFile image, HttpServletRequest request) throws Exception{
        File imageFolder =new File(request.getServletContext().getRealPath("img/category"));
        File file = new File(imageFolder,bean.getId()+".jpg");
        if(!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        image.transferTo(file);
        BufferedImage image1= ImageUtil.change2jpg(file);
        ImageIO.write(image1,"jpg",file);
    }


}
