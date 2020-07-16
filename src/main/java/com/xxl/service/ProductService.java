package com.xxl.service;

import com.xxl.mapper.ProductMapper;
import com.xxl.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    public List<Product> listProduct(){
        return productMapper.listProduct();
    }

    public Product selectByPrimaryKey(Integer id){
        return productMapper.selectByPrimaryKey(id);
    }
    @Transactional
    public int deductProductStock(Integer productId) {
        return productMapper.deductProductStock(productId);
    }
}
