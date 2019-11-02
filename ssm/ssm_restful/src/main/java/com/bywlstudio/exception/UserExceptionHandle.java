package com.bywlstudio.exception;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserExceptionHandle implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object o, Exception e) {
        UserException ue = null ;
        if(e instanceof UserException){
            e = (UserException) e ;
        }else {
            ue = new UserException("用户不存在错误");
        }
        ModelAndView mv = new ModelAndView();
        mv.addObject("error",ue.getMessage());
        mv.setViewName("error");
        return mv ;
    }
}
