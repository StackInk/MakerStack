package com.bywlstudio.cas;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 原子引用实现
 */
class User{
    String name ;
    Integer id ;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }

    public User(String name, Integer id) {
        this.name = name;
        this.id = id;
    }
}
public class AtomicRefenceDemo {
    public static void main(String[] args) {
        AtomicReference<User> atomicReference = new AtomicReference<>();
        atomicReference.set(new User("1",1));
        System.out.println(atomicReference.get());
    }
}
