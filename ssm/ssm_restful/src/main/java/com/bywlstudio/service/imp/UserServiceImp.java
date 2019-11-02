package com.bywlstudio.service.imp;

import com.bywlstudio.dao.IUserDao;
import com.bywlstudio.domain.User;
import com.bywlstudio.service.IUserService;
import com.bywlstudio.util.CompareBeanAtt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service("userService")
public class UserServiceImp implements IUserService {

    @Autowired
    private IUserDao userDao ;

    @Override
    public List<User> findAll() {
        return userDao.findAll();
    }

    @Override
    public User findUserById(Integer id) {
        return userDao.findUserById(id);
    }

    @Override
    public void delUser(Integer id) {
        userDao.delUser(id);
    }

    @Override
    public boolean updateUser(User user) {
        //PUT方法幂等性实现
        User oldUser = userDao.findUserById(user.getUid());
        boolean compare = CompareBeanAtt.compare(oldUser, user);
        if(!compare) {
            userDao.updateUser(user);
            return true ;//此时对数据做了修改，返回状态码为201
        }
        return false;//此时没有对资源进行修改，返回状态码为200
    }

    @Override
    public User addUser(User user) {
        userDao.insertUser(user);
        return user ;
    }
}
