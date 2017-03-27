package com.bbc.service;

import com.bbc.entity.User;

/**
 * Created by gonglixun on 2017/2/8.
 */
public interface UserService {

    public int updateUser();

    public User addUser(String openid,String unionid,int parentId,int level);

    public User getUserByUnionid(String unionid);

    public User getUserByParentId(int parentId);
}
