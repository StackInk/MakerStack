package com.bywlstudio.dao;

import com.bywlstudio.domain.User;
import com.bywlstudio.service.IUserService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UserDaoImp {


    @Test
    public void addUser(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        IUserService userService = ac.getBean("userService", IUserService.class);
        User user = new User();
        user.setUsex("男");
        user.setUname("张三");
        userService.addUser(user);
    }

    @Test
    public void delUser(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        IUserService userService = ac.getBean("userService", IUserService.class);
        userService.delUser(2);
    }
}
