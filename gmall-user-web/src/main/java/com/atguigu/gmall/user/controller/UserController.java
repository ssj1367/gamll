package com.atguigu.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController {
    // dubbo代理注入
    @Reference
    private UserService userService;

    @RequestMapping("/userList")
    @ResponseBody
    public List<UserInfo> userList(){
        List<UserInfo> userInfoListAll = userService.getUserInfoListAll();
        return userInfoListAll;
    }

    @RequestMapping(value="/user",method = RequestMethod.GET)
    public ResponseEntity<Object> addUser(UserInfo userInfo){
        userService.addUser(userInfo);
        return ResponseEntity.ok().build();
    }
}
