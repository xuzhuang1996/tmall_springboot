package com.tmall.cache.cacheServiceImpl;

import com.tmall.cache.AutoReloadCache;
import com.tmall.cache.CacheService;
import com.tmall.dao.CategoryDAO;
import com.tmall.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class CacheServiceImpl implements CacheService {
    @Autowired
    CategoryDAO categoryDAO;
    AutoReloadCache<String, Object> categoryCache=new AutoReloadCache<String, Object>(){
        @Override
        protected void reload() {
            Sort sort = new Sort(Sort.Direction.DESC, "id");
            List<Category>categories = categoryDAO.findAll(sort);
            if(cache==null)
                cache = new HashMap<>();
            cache.put("categories",categories);
        }
    };
    @Override
    public List<Category> getListCategory() {
        return (List<Category>)categoryCache.get("categories");
    }
}
