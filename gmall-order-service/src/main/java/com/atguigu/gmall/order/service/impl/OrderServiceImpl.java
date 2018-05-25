package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.order.mapper.OrderMapper;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public List<UserAddress> getUserAddressList() {
        List<UserAddress> userAddresses = orderMapper.selectAll();
        return userAddresses;
    }
}
