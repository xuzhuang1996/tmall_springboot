package com.tmall.service;

import com.tmall.dao.OrderItemDAO;
import com.tmall.pojo.Order;
import com.tmall.pojo.OrderItem;
import com.tmall.pojo.Product;
import com.tmall.pojo.User;
import com.tmall.util.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderItemService {
    @Autowired
    OrderItemDAO orderItemDAO;
    @Autowired
    ProductImageService productImageService;

    public int getSaleCount(Product product) {
        List<OrderItem> ois =listByProduct(product);
        int result =0;
        for (OrderItem oi : ois) {
            if(null!=oi.getOrder())
                if(null!= oi.getOrder() && null!=oi.getOrder().getPayDate())//如果付款。
                    result+=oi.getNumber();
        }
        return result;
    }

    //各种list
    public List<OrderItem> listByUser(User user) {
        return orderItemDAO.findByUserAndOrderIsNull(user);
    }
    public List<OrderItem> listByProduct(Product product) {
        return orderItemDAO.findByProduct(product);
    }
    public List<OrderItem> listByOrder(Order order) {
        return orderItemDAO.findByOrderOrderByIdDesc(order);
    }
    //=================================crud
    public void add(OrderItem orderItem) {
        orderItemDAO.save(orderItem);
    }
    public OrderItem get(int id) {
        Optional<OrderItem> ProductInfoOptional = orderItemDAO.findById(id);
        if (!ProductInfoOptional.isPresent()) {
            return null;
        }
        OrderItem orderItem = ProductInfoOptional.get();
        return orderItem;
    }
    public void delete(int id) {
        orderItemDAO.deleteById(id);
    }
    public void update(OrderItem orderItem) {
        orderItemDAO.save(orderItem);
    }
}
