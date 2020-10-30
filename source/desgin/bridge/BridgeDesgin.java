package desgin.bridge;

/**
 * @Author: zl
 * @Date: Create in 2020/9/14 10:18
 * @Description:
 */
public class BridgeDesgin {
    public static void main(String[] args) {
        Model model = new Redme(new RedmePro20());
        model.play();
    }
}

interface Mi {
    void play();
}

class Note extends Model{
    public Note(Mi mi) {
        super(mi);
    }

    @Override
    void play() {
        super.mi.play();
        System.out.println("这是Note系列手机");
    }
}

class Redme extends Model{
    public Redme(Mi mi) {
        super(mi);
    }

    @Override
    void play() {
        super.mi.play();
        System.out.println("这是RedMe系列手机");
    }
}

class Mix extends Model{
    public Mix(Mi mi) {
        super(mi);
    }

    @Override
    void play() {
        super.mi.play();
        System.out.println("这是Mix系列手机");
    }
}

abstract class Model{
    protected Mi mi ;
    public Model(Mi mi){
        this.mi = mi ;
    }
    abstract void play() ;
}

class Note4 implements Mi{
    @Override
    public void play() {
        System.out.println("这是Note4");
    }

}


class RedmePro20 implements Mi{

    @Override
    public void play() {
        System.out.println("这是RedmePro20");
    }

}


class Mix3 implements Mi{
    @Override
    public void play() {
        System.out.println("这是Mix3");
    }
}