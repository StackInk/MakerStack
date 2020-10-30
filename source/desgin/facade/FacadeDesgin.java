package desgin.facade;

/**
 * @Author: zl
 * @Date: Create in 2020/9/16 9:13
 * @Description: 门面设计模式
 */
public class FacadeDesgin {
}


class Contractor{
    private Cement cement = new Cement();
    private Worker worker = new Worker();
    private Brick brick = new Brick();

    void cement(){
        cement.cement();
    }

    void worker(){
        worker.worker();
    }

    void brick(){
        brick.brick();
    }
}

class Cement{
    void cement(){
        System.out.println("水泥");
    }
}

class Worker{
    void worker(){
        System.out.println("工人");
    }
}

class Brick{
    void brick(){
        System.out.println("砖头");
    }
}

