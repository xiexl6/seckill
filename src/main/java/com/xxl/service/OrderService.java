package com.xxl.service;

import com.sun.org.apache.xpath.internal.operations.Or;
import com.xxl.mapper.OrderMapper;
import com.xxl.model.Order;
import com.xxl.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    @Autowired
    private ProductService productService;
    @Autowired
    private OrderMapper orderMapper;


    @Transactional
    public void secondKill(Integer productId){
        //查询商品
        Product product = productService.selectByPrimaryKey(productId);

        if (product!=null && product.getStock()<= 0){
            throw new RuntimeException("商品库存已经售完");
        }

        //创建秒杀订单
        Order order = new Order();
        order.setProductId(productId);
        order.setAmount(product.getPrice());
        saveOrder(order);

        //减库存
        int updateNums = productService.deductProductStock(productId);
        if (updateNums <= 0){
            throw new RuntimeException("商品已经售完");
        }

    }
    @Transactional
    public void saveOrder(Order order) {
        orderMapper.insert(order);
    }


}
