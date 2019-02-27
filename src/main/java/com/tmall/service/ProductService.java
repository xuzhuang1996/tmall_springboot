package com.tmall.service;

import com.tmall.dao.CategoryDAO;
import com.tmall.dao.ProductDAO;
import com.tmall.dao.PropertyDAO;
import com.tmall.pojo.Category;
import com.tmall.pojo.OrderItem;
import com.tmall.pojo.Product;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ReviewService reviewService;

    public Page<Product> listProduct(int start, int size, int cid){
        Category c = categoryService.get(cid);
        Sort sort = new Sort(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(start, size, sort);//new PageRequest(firstResult, maxResults, new Sort(...))过时
        Page page = productDAO.findByCategory(c,pageable);
        productImageService.setFirstProdutImages(page.getContent());//取出product后进行设置图片
        return page;
    }

    //单个分类进行fill，因此还需要为多个分类来个fill
    public void fill(Category category){
        List<Product> products = listByCategory(category);
        productImageService.setFirstProdutImages(products);//取出product后进行设置图片
        category.setProducts(products);
    }

    public void fill(List<Category> categorys) {
        for (Category category : categorys) {
            fill(category);
        }
    }

    //首页每个分类侧都有几个推荐的产品。
    public void fillByRow(List<Category> categorys) {
        int productNumberEachRow = 8;
        for (Category category : categorys) {
            List<Product> products =  category.getProducts();
            List<List<Product>> productsByRow =  new ArrayList<>();
            for (int i = 0; i < products.size(); i+=productNumberEachRow) {
                int size = i+productNumberEachRow;
                size= size>products.size()?products.size():size;
                List<Product> productsOfEachRow =products.subList(i, size);
                productsByRow.add(productsOfEachRow);
            }
            category.setProductsByRow(productsByRow);
        }
    }

    //增加评论
    public void setSaleAndReviewNumber(Product product) {
        int saleCount = orderItemService.getSaleCount(product);
        product.setSaleCount(saleCount);

        int reviewCount = reviewService.getCount(product);
        product.setReviewCount(reviewCount);
    }


    public void setSaleAndReviewNumber(List<Product> products) {
        for (Product product : products)
            setSaleAndReviewNumber(product);
    }

    public List<Product> listByCategory(Category category) {
        return productDAO.findByCategoryOrderById(category);
    }

    public List<Product> search(String keyword, int start, int size) {
//        initDatabase2ES();
//        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery()
//                .add(QueryBuilders.matchPhraseQuery("name", keyword),
//                        ScoreFunctionBuilders.weightFactorFunction(100))
//                .scoreMode("sum")
//                .setMinScore(10);
//        Sort sort  = new Sort(Sort.Direction.DESC,"id");
//        Pageable pageable = new PageRequest(start, size,sort);
//        SearchQuery searchQuery = new NativeSearchQueryBuilder()
//                .withPageable(pageable)
//                .withQuery(functionScoreQueryBuilder).build();
//
//        Page<Product> page = productESDAO.search(searchQuery);
//        return page.getContent();
        return null;
    }

    //==========================================crud========================================
    public void add(Product product){
        productDAO.save(product);
        propertyValueService.init(product);//初始化产品属性值
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
        //propertyValueService.init(product);//初始化产品属性值，本来这里不该写，但是我这边数据每个产品没有属性值，正式的获取产品是不需要这个的。
        return product;
    }
}
