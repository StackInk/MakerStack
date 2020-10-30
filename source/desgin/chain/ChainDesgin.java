package desgin.chain;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: zl
 * @Date: Create in 2020/9/8 18:38
 * @Description:
 */
public class ChainDesgin {
    public static void main(String[] args) {
        Student student = new Student();
        student.setName("木争");

        new FilterChain().addFilter(new TimerFilter())
                .addFilter(new LogFilter()).addFilter(new Sensitive()).doFilter(student);

        System.out.println(student.getName());

    }
}



interface Filter{
    boolean doFilter(Student student);
}


class TimerFilter implements Filter{
    @Override
    public boolean doFilter(Student student) {
        student.setName(student.getName()+new Date().toString());
        return true ;
    }
}

class Sensitive implements Filter {
    @Override
    public boolean doFilter(Student student) {
        student.setName(student.getName()+"sensitive");
        return true ;
    }
}

class LogFilter implements Filter {
    @Override
    public boolean doFilter(Student student) {
        student.setName(student.getName()+"logFilter");
        return false ;
    }
}

class FilterChain implements Filter{

    private List<Filter> filters = new ArrayList<>();

    public FilterChain addFilter(Filter filter){
        filters.add(filter);
        return this ;
    }

    @Override
    public boolean doFilter(Student student) {
        for (Filter filter : filters) {
            if(!filter.doFilter(student)) return false;
        }
        return true ;
    }
}

class Student{

    private String name ;
    private String username ;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
