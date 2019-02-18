package com.tmall.dao;

import com.tmall.pojo.Category;
import com.tmall.pojo.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

//有根据分类获取产品的需求，因此。。。
public interface ProductDAO extends JpaRepository <Product,Integer>{
    Page<Product> findByCategory(Category category, Pageable pageable);
}
