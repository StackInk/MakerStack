package com.bywlstudio.exception;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserException extends Exception {

    private String message ;

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserException(String message){
        this.message = message ;
    }

    @Override
    public String toString() {
        return "UserException{" +
                "message='" + message + '\'' +
                '}';
    }
}
