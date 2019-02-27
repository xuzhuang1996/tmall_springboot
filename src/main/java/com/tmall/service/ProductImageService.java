package com.tmall.service;

import com.tmall.dao.ProductImageDAO;
import com.tmall.pojo.OrderItem;
import com.tmall.pojo.Product;
import com.tmall.pojo.ProductImage;
import com.tmall.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductImageService {
    @Autowired
    ProductImageDAO productImageDAO;
    @Autowired
    ProductService productService;
    public static final String type_single = "type_single";
    public static final String type_detail = "type_detail";


    //前台根据pid以及type请求图片.这里我不知道为何要分开
    public List<ProductImage> listSingleProductImages(int pid) {
        Product product1=productService.get(pid);
        return productImageDAO.findByProductAndTypeOrderByIdDesc(product1, type_single);
    }

    public List<ProductImage> listDetailProductImages(int pid) {
        Product product1=productService.get(pid);
        return productImageDAO.findByProductAndTypeOrderByIdDesc(product1, type_detail);
    }

    //为产品设置图片的处理
    public void setFirstProdutImage(Product product) {
        //不清楚博主下面步骤的意义。如果图片显示有问题在去掉下面注释
        //ProductImageService productImageService = SpringContextUtil.getBean(ProductImageService.class);
        //List<ProductImage> singleImages = productImageService.listSingleProductImages(product);
        List<ProductImage> singleImages = listSingleProductImages(product.getId());
        if(!singleImages.isEmpty())
            product.setFirstProductImage(singleImages.get(0));
        else
            product.setFirstProductImage(new ProductImage()); //这样做是考虑到产品还没有来得及设置图片，但是在订单后台管理里查看订单项的对应产品图片。
    }

    public void setFirstProdutImages(List<Product> products) {
        for (Product product : products)
            setFirstProdutImage(product);
    }

    //每个订单项，在结账或者购物车的时候需要图片
    public void setFirstProdutImagesOnOrderItems(List<OrderItem> ois) {
        for (OrderItem orderItem : ois) {
            setFirstProdutImage(orderItem.getProduct());
        }
    }
    //++++++++++++++++++crud++++++++++++++++++++++++++++
    public void add(ProductImage bean) {
        productImageDAO.save(bean);
    }
    public void delete(int id) {
        productImageDAO.deleteById(id);
    }
    public ProductImage get(int id) {
        Optional<ProductImage> ProductImageInfoOptional = productImageDAO.findById(id);
        if (!ProductImageInfoOptional.isPresent()) {
            return null;
        }
        return ProductImageInfoOptional.get();
    }
}
