package com.tmall.service;

import com.tmall.dao.CategoryDAO;
import com.tmall.dao.ProductDAO;
import com.tmall.dao.PropertyDAO;
import com.tmall.pojo.Category;
import com.tmall.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    ProductDAO productDAO;
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;

    public Page<Product> listProduct(int start, int size, int cid){
        Category c = categoryService.get(cid);
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(start, size, sort);//new PageRequest(firstResult, maxResults, new Sort(...))过时
        Page page = productDAO.findByCategory(c,pageable);
        productImageService.setFirstProdutImages(page.getContent());//取出product后进行设置图片
        return page;
    }

    public void add(Product product){
        productDAO.save(product);
        //propertyValueService.init(product.getId());//初始化产品属性值
    }

    public void delete(int id){
        productDAO.deleteById(id);
    }

    public void edit(Product product){
        productDAO.save(product);
    }

    public Product get(int id){
        Optional<Product> ProductInfoOptional = productDAO.findById(id);
        if (!ProductInfoOptional.isPresent()) {
            return null;
        }
        Product product = ProductInfoOptional.get();
        //propertyValueService.init(id);//初始化产品属性值
        return product;
    }
}
