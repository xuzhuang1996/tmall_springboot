package com.tmall.dao;

import com.tmall.pojo.Product;
import com.tmall.pojo.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageDAO extends JpaRepository<ProductImage,Integer> {
    public List<ProductImage> findByProductAndTypeOrderByIdDesc(Product product, String type);//为啥要用List，不用Page，因为取图片肯定要一次取完
}
