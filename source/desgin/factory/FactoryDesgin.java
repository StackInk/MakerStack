package desgin.factory;

/**
 * @Author: zl
 * @Date: Create in 2020/9/2 21:02
 * @Description:
 */
public class FactoryDesgin {
    public static void main(String[] args) {
        Traffic traffic = new TrafficFactory().createPlane();
    }

}



class TrafficFactory{
    public Plane createPlane(){
        return new Plane();
    }

    public Train createTrain(){
        return new Train();
    }

    public Bus createBus(){
        //before processor
        return new Bus();
        //after processor
    }
}


abstract class Traffic {
    public abstract void run();
}

abstract class Food{

}




class Plane extends Traffic{
    public void run(){
        System.out.println("Plane fei................");
    }
}

class Train extends Traffic{
    public void run(){
        System.out.println("Train liu ..................");
    }
}

class Bus extends Traffic{
    public void run(){
        System.out.println("Bus zou............");
    }
}
