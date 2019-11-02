package com.bywlstudio.controler;

import com.bywlstudio.domain.User;
import com.bywlstudio.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserControler {

    @Autowired
    private IUserService userService ;

    //REST风格实现方法

    /**
     * 查询所有
     * @return
     */
    @GetMapping(produces = "application/json;charset=utf-8")
    public ResponseEntity<List<User>> findAll(){
        List<User> users = userService.findAll();
        return new ResponseEntity<List<User>>(users , HttpStatus.OK);
    }

    /**、
     * 根据ID查询
     * @param id
     * @return
     */

    @GetMapping(path = "/{id}" , produces = "application/json;charset=utf-8")
    @ResponseStatus(HttpStatus.OK)
    public User findUserById(@PathVariable("id")Integer id){
        User user = userService.findUserById(id);
        return user ;
    }
    /**
     * 增加一个用户
     * 返回该用户
     */
    @PostMapping(produces = "application/json;charset=utf-8")
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@RequestBody User user){
        User newUser = userService.addUser(user);
        return newUser ;
    }

    /**
     * 更新
     * @param user
     */
    @PutMapping(path = "/{id}" ,produces = "application/json;charset=utf-8")
    public ResponseEntity<User> updateUser(@PathVariable("id") Integer id , @RequestBody User user){
        user.setUid(id);
        //资源是否修改
        boolean flag = userService.updateUser(user);
        User deUser = userService.findUserById(id);
        if(flag)
            return new ResponseEntity<User>(deUser,HttpStatus.CREATED);
        return new ResponseEntity<User>(deUser,HttpStatus.OK);
    }

    @DeleteMapping(path = "/{id}"  , produces = "application/json;charset=utf-8")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delUser(@PathVariable("id") Integer id){
        User user = userService.findUserById(id);
        userService.delUser(id);
    }


}
