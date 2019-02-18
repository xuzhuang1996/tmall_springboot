package com.tmall.service;

import com.tmall.dao.CategoryDAO;
import com.tmall.dao.PropertyDAO;
import com.tmall.pojo.Category;
import com.tmall.pojo.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropertyService {
    @Autowired
    PropertyDAO PropertyDAO;

    @Autowired
    CategoryService categoryService;//如果我自己写，会用DAO，教程用的服务

    public Page<Property> listProperty(int start, int size,int cid){
        Category category = categoryService.get(cid);
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(start, size, sort);//new PageRequest(firstResult, maxResults, new Sort(...))过时
        Page page =PropertyDAO.findByCategory(category,pageable);
        return page;
    }

    public List<Property> listByCategory(int cid){
        Category category = categoryService.get(cid);
        return PropertyDAO.findByCategory(category);
    }

    //即便是多对一，多属性对目录，依然只保存Property，不需cid参数。因为已经在Property里面了
    public void add(Property p){
        PropertyDAO.save(p);
    }

    public void delete(int id){
        PropertyDAO.deleteById(id);
    }

    public void edit(Property p){
        PropertyDAO.save(p);
    }

    public Property get(int id) {
        Optional<Property> PropertyInfoOptional = PropertyDAO.findById(id);
        if (!PropertyInfoOptional.isPresent()) {
            return null;
        }
        return PropertyInfoOptional.get();
    }

}
