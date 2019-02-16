package com.tmall.dao;

import com.tmall.pojo.Category;
import org.springframework.data.jpa.repository.JpaRepository;

//提供泛型<Category,Integer> 表示这个是针对Category类的DAO,Integer表示主键是Integer类型。
public interface CategoryDAO extends JpaRepository<Category,Integer> {
}
