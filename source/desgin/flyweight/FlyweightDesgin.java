package desgin.flyweight;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * @Author: zl
 * @Date: Create in 2020/10/5 16:02
 * @Description:
 */
public class FlyweightDesgin {

    public static void main(String[] args) {
        FlyweightFactory flyweightFactory = new FlyweightFactory();
        Flyweight flyweight = flyweightFactory.getFlyweight();
        flyweight.print();

    }

}

abstract class Flyweight{
    boolean isUsed ;
    public Flyweight(boolean isUsed){
        this.isUsed = isUsed;
    }
    abstract void print();
}

class ConcreteFlyweight extends Flyweight{

    public ConcreteFlyweight(boolean isUsed){
        super(isUsed);
    }

    public void print(){
        System.out.println("这是普通的享元对象");
    }

}

class UnsharedConcreteFlyweight extends Flyweight{

    public UnsharedConcreteFlyweight(boolean isUsed){
        super(isUsed);
    }

    public void print(){
        System.out.println("这是非共享的享元对象");
    }
}

class FlyweightFactory{
    private List<Flyweight> lists = new ArrayList<>();

    {
        for(int i = 0 ; i < 5 ; i++){
            lists.add(new ConcreteFlyweight(false));
        }
    }

    public Flyweight getFlyweight(){
        for (Flyweight flyweight : lists) {
            if(!flyweight.isUsed){
                flyweight.isUsed = true ;
                return flyweight ;
            }
        }
        lists.add(new ConcreteFlyweight(true));
        return lists.get(lists.size()-1);
    }

    public boolean setFlyweight(Flyweight flyweight){
        int index = lists.indexOf(flyweight);
        if(index == -1){
            return false ;
        }
        lists.get(index).isUsed=false;
        return true ;
    }


    public Flyweight getShareFlyweight(){
        return new UnsharedConcreteFlyweight(true);
    }
}
