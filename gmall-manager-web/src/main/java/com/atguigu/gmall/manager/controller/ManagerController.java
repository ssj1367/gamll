package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.service.MangerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ManagerController {

    @Reference
    MangerService managerService;

    @RequestMapping("index")
    public String index(){
        return "index";
    }

    @RequestMapping("attrListPage")
    public String attrListPage(){
        return "attrListPage";
    }

    @RequestMapping("spuListPage")
    public String spuListPage(){
        return "spuListPage";
    }
}
