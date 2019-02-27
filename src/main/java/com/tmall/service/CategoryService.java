package com.tmall.service;

import com.tmall.dao.CategoryDAO;
import com.tmall.pojo.Category;
import com.tmall.pojo.Product;
import org.apache.commons.collections.ArrayStack;
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

    // 这个方法的用处是删除Product对象上的 分类。 为什么要删除呢？
    // 因为在对分类做序列还转换为 json 的时候，会遍历里面的 products, 然后遍历出来的产品上，又会有分类，接着就开始子子孙孙无穷溃矣地遍历了，就搞死个人了
    // 而在这里去掉，就没事了。 只要在前端业务上，没有通过产品获取分类的业务，去掉也没有关系

    public void removeCategoryFromProduct(List<Category> cs) {
        for (Category category : cs) {
            removeCategoryFromProduct(category);
        }
    }

    public void removeCategoryFromProduct(Category category) {
        List<Product> products =category.getProducts();
        if(null!=products) {
            for (Product product : products) {
                product.setCategory(null);
            }
        }

        List<List<Product>> productsByRow =category.getProductsByRow();
        if(null!=productsByRow) {
            for (List<Product> ps : productsByRow) {
                for (Product p: ps) {
                    p.setCategory(null);
                }
            }
        }
    }
}
