package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public List<UserInfo> getUserInfoListAll(){
        List<UserInfo> userInfos = userInfoMapper.selectAll();
        return userInfos;
    }

    @Override
    public void addUser(UserInfo userInfo){
        // 插入数据的时候返回主键
        userInfoMapper.insertSelective(userInfo);
        System.out.print("user="+userInfo);
    }
}
