package com.xxl.mapper;

import com.xxl.model.Product;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductMapper {
    Product selectByPrimaryKey(Integer id);

    int deductProductStock(Integer id);

    List<Product> listProduct();
}
