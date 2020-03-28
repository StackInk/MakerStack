package com.bywlstudio.util;

import com.bywlstudio.domain.User;

public class CompareBeanAtt {
    public static boolean compare(User oldUser,User newUser){
        if(oldUser == null || newUser == null) return false ;
        if(oldUser.getUname().equals(newUser.getUname())&& oldUser.getUphone().equals(newUser.getUphone())
                && oldUser.getUsex().equals(newUser.getUsex()))
        return true;
        return false;
    }
}
