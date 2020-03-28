package com.bywlstudio.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
@Setter@Getter
public class User implements Serializable {
    private Integer uid ;
    private String uname ;
    private String usex ;
    private String uphone ;

    @Override
    public String toString() {
        return "User{" +
                "uid=" + uid +
                ", uname='" + uname + '\'' +
                ", usex='" + usex + '\'' +
                ", uphone='" + uphone + '\'' +
                '}';
    }
}
