package desgin.mementor;

import com.sun.org.apache.xpath.internal.operations.Or;

/**
 * @Author: zl
 * @Date: Create in 2020/10/26 21:55
 * @Description:
 */
public class MementorDesgin {
    public static void main(String[] args) {
        Originator originator = new Originator();
        Caretaker caretaker = new Caretaker(originator);
        //保存当前状态
        Memento memento = caretaker.createMemento();

        originator.setState("我的");
        caretaker.createMemento();
    }
}


interface Memento{

}


class Originator{
    private String state ;

    public Memento createMemento(){
        return new InnerMemento(state);
    }

    public void restoreState(Memento memento){
        this.state = ((InnerMemento)memento).getState();
    }

    public void setState(String state) {
        this.state = state;
    }

    class InnerMemento implements Memento{
        private String state ;

        public InnerMemento(String state){
            this.state = state ;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getState() {
            return state;
        }
    }
}

class Caretaker{
    private Originator originator ;
    public Caretaker(Originator originator){
        this.originator = originator ;
    }
    public Memento createMemento(){
        return originator.createMemento();
    }
    public void restoreState(Memento memento){
        originator.restoreState(memento);
    }
}
