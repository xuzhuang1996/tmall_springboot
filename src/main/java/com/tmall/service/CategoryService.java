package com.tmall.service;

import com.tmall.dao.CategoryDAO;
import com.tmall.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    CategoryDAO categoryDAO;
    public List<Category> list() {
        //首先创建一个 Sort 对象，表示通过 id 倒排序， 然后通过 categoryDAO进行查询
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        return categoryDAO.findAll(sort);
    }

    public Page<Category> listCategory(int start,int size){
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(start, size, sort);//new PageRequest(firstResult, maxResults, new Sort(...))过时
        Page page =categoryDAO.findAll(pageable);
        return page;
    }

    public void add(Category bean) {
        categoryDAO.save(bean);
    }

    public void delete(int id){
        categoryDAO.deleteById(id);
    }

    public void edit(Category bean){
        categoryDAO.save(bean);
    }

    public Category get(int id) {
        Optional<Category> CategoryInfoOptional = categoryDAO.findById(id);
        if (!CategoryInfoOptional.isPresent()) {
            return null;
        }
        return CategoryInfoOptional.get();
    }
}
