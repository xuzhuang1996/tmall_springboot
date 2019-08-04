package com.tmall.cache.cacheServiceImpl;

import com.tmall.cache.AutoReloadCache;
import com.tmall.cache.CacheService;
import com.tmall.dao.CategoryDAO;
import com.tmall.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CacheServiceImpl implements CacheService {
    @Autowired
    CategoryDAO categoryDAO;

    private AutoReloadCache<String, Object> categoryCache=new AutoReloadCache<String, Object>(){
        @Override
        protected Map<String, Object> reload() {
            Sort sort = new Sort(Sort.Direction.DESC, "id");
            List<Category>categories = categoryDAO.findAll(sort);
            return categories.stream().collect(Collectors.toMap(Category::getName,r->r));//转为Map，以Name为key，以category对象为value。
        }
    };
    @Override
    public List<Category> getListCategory() {
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        return categoryDAO.findAll(sort);
    }
}
