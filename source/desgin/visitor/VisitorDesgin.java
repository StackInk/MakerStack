package desgin.visitor;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author: zl
 * @Date: Create in 2020/10/15 22:31
 * @Description: 访问者设计模式
 */
public class VisitorDesgin {


    public static void main(String[] args) {
        Element element = new ConElement().addVistor(new Jack()).addVistor(new Tom());

        StuVistor stuVistor = new StuVistor();
        element.accept(stuVistor);
    }
}


class ConElement implements Element{

    private List<Element> lists = new LinkedList<>();

    @Override
    public void accept(Vistor vistor) {
        for (int i = 0; i < lists.size(); i++) {
            lists.get(i).accept(vistor);
        }
    }

    public ConElement addVistor(Element element){
        lists.add(element);
        return this ;
    }
}

interface Element{
    void accept(Vistor vistor);
}


abstract class Vistor{
    abstract void vistorTom(Tom tom);
    abstract void vistorJack(Jack jack);
}

class StuVistor extends Vistor{
    @Override
    void vistorTom(Tom tom) {
        tom.name="stu1" ;
        System.out.println("这是学生访问者"+tom.name);
    }

    @Override
    void vistorJack(Jack jack) {
        jack.name="jack1";
        System.out.println("这是Jack访问者"+jack.name);
    }
}

class Tom implements Element{
    String name ;
    String age ;

    @Override
    public void accept(Vistor vistor) {
        vistor.vistorTom(this);
    }
}
class Jack implements Element{
    String name ;
    String age ;

    @Override
    public void accept(Vistor vistor) {
        vistor.vistorJack(this);
    }
}