package com.bywlstudio.service;

import com.bywlstudio.domain.User;

import java.util.List;

public interface IUserService {

    List<User> findAll();

    User findUserById(Integer id) ;

    void delUser(Integer id) ;

    boolean updateUser(User user) ;

    User addUser(User user) ;

}
