package com.tmall.controller;

import com.tmall.pojo.Property;
import com.tmall.service.CategoryService;
import com.tmall.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

//如果是返回json数据且不用responseBody就用它，否则报错。
@RestController
public class PropertyController {
    @Autowired
    PropertyService propertyService;

    @GetMapping("/categories/{cid}/properties")
    public Page<Property> listProperty(@PathVariable("cid")int cid,@RequestParam(value = "start", defaultValue = "0") int start,
                                       @RequestParam(value = "size", defaultValue = "5") int size) {
        start = start<0?0:start;
        Page page = propertyService.listProperty(start,size,cid);
        return page;
    }

    //增加的时候，前端对应的url=properties，传来Property。
    //分类因为要上传文件，用的是 FormData 方式，因此参数直接接受，而 property 并没有采用 formData，需要使用@RequestBody接受
    @PostMapping("/properties")
    public Object add(@RequestBody Property property){
        propertyService.add(property);
        return property;
    }

    @DeleteMapping("/properties/{id}")
    public String delete(@PathVariable("id") int id){
        propertyService.delete(id);
        return null;
    }

    @PutMapping("/properties")
    public Object update(@RequestBody Property property){
        propertyService.edit(property);
        return property;
    }

    @GetMapping("/properties/{id}")
    public Property get(@PathVariable("id") int id)  {
        Property bean=propertyService.get(id);
        return bean;
    }

}
