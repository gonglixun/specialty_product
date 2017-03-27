package com.bbc.service.impl;

import com.bbc.dao.UserDao;
import com.bbc.entity.User;
import com.bbc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by gonglixun on 2017/2/8.
 */
@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserDao userDao;

    public int updateUser(){
        return 1;
    }

    @Override
    public User addUser(String openid,String unionid,int parentId,int level){
        User user = new User();
        user.setOpenid(openid);
        user.setUnionid(unionid);
        user.setParentId(parentId);
        user.setLevel(level);
        userDao.addUser(user);
        return user;
    }
    @Override
    public User getUserByUnionid(String unionid){
        return userDao.getUserByUnionid(unionid);
    }

    @Override
    public User getUserByParentId(int parentId){
        return userDao.getInfoById("UserDao.getUserByParentId",parentId);
    }
}
