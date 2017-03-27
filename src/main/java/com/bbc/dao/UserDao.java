package com.bbc.dao;

import com.bbc.entity.User;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserDao extends BaseDao<User>{

	public int addUser(User user){
		return this.addInfo("UserDao.addUser",user);
	}

	public User getUserByUnionid(String unionid){
		return this.getInfoById("UserDao.getUserByUnionid",unionid);


	}

	public User getUserByParentId(int parentId){
		return this.getInfoById("UserDao.getUserByParentId",parentId);
	}

}
