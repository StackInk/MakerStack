package desgin.chain.servet;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zl
 * @Date: Create in 2020/9/8 19:27
 * @Description:
 */
public class ChainServlet {
    public static void main(String[] args) {
        MyFilterChain filterChain = new MyFilterChain().addFilter(new LogFilter()).addFilter(new TimerFilter());
        Request request = new Request();
        Response response = new Response();
        filterChain.doFilter(request,response);
    }
}


class Request{
    String requestName ;
}

class Response{
    String responseName ;
}

interface Filter{
    void doFilter(Request request , Response response , FilterChain filterChain);
}

class LogFilter implements Filter{
    public void doFilter(Request request, Response response, FilterChain filterChain) {
        System.out.println("处理Request请求========LogFilter");
        filterChain.doFilter(request,response);
        System.out.println("处理Response请求========LogFilter");
    }
}

class TimerFilter implements Filter{

    @Override
    public void doFilter(Request request, Response response, FilterChain filterChain) {
        System.out.println("处理Request请求========TimerFilter");
        filterChain.doFilter(request,response);
        System.out.println("处理Response请求========TimerFilter");
    }
}



interface FilterChain{
    void doFilter(Request request , Response response);
}

class MyFilterChain implements FilterChain {

    int index = 0 ;

    List<Filter> filters = new ArrayList<>();

    public MyFilterChain addFilter(Filter filter){
        filters.add(filter);
        return this ;
    }


    @Override
    public void doFilter(Request request, Response response) {
        if(index == filters.size()){
            return ;
        }
        Filter filter = filters.get(index);
        index++;
        filter.doFilter(request,response,this);
    }
}