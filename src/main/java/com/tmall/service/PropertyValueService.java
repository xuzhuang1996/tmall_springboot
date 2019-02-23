package com.tmall.service;

import com.tmall.dao.PropertyValueDAO;
import com.tmall.pojo.Product;
import com.tmall.pojo.Property;
import com.tmall.pojo.PropertyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyValueService {

    @Autowired
    PropertyValueDAO propertyValueDAO;

    @Autowired
    ProductService productService;

    @Autowired
    PropertyService propertyService;

    //在创建product时获取获取时需要初始化
    public void init(Product product){
        //Product product = productService.get(pid);//非要用这个会进入死循环。出现StackOverflowError错误看看是否进入死循环。
        List<Property> properties = propertyService.listByCategory(product.getCategory().getId());
        //拿到所有属性后，对该产品，生成该产品的属性值
        for (Property property: properties) {
            PropertyValue propertyValue = getByPropertyAndProduct(product, property);
            if(null==propertyValue){
                propertyValue = new PropertyValue();
                propertyValue.setProduct(product);
                propertyValue.setProperty(property);
                propertyValueDAO.save(propertyValue);
            }
        }
    }

    //查询某一产品某一属性的属性值
    public PropertyValue getByPropertyAndProduct(Product product, Property property) {
        return propertyValueDAO.getByPropertyAndProduct(property,product);
    }


    public List<PropertyValue> list(int pid){
        Product product = productService.get(pid);
        return propertyValueDAO.findByProductOrderByIdDesc(product);
    }

    public void update(PropertyValue bean) {
        propertyValueDAO.save(bean);
    }
}
