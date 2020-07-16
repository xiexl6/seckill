package com.xxl.mapper;

import com.xxl.model.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {
    void insert(Order order);
}
