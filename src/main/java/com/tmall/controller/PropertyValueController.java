package com.tmall.controller;

import com.tmall.pojo.PropertyValue;
import com.tmall.service.PropertyValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PropertyValueController {
    @Autowired
    PropertyValueService propertyValueService;

    @GetMapping("products/{pid}/propertyValues")
    public List<PropertyValue> listPropertyValue(@PathVariable int pid){
        return propertyValueService.list(pid);
    }

    @PutMapping("/propertyValues")
    public Object update(@RequestBody PropertyValue bean) throws Exception {
        propertyValueService.update(bean);
        return bean;
    }
}
