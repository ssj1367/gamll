package com.atguigu.gmall.order.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class OrderController {


    @Reference
    private OrderService orderService;

    @ResponseBody
    @RequestMapping(value = "orderAddress")
    public List<UserAddress> orderAddress(){
        List<UserAddress> userAddressList = orderService.getUserAddressList();
        return userAddressList;
    }

}
