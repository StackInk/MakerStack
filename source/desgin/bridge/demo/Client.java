package desgin.bridge.demo;

/**
 * @Author: zl
 * @Date: Create in 2020/9/14 10:36
 * @Description:
 */
public class Client {
    public static void main(String[] args) {
        Phone phone = new FoldedPhone(new XiaoMi());
        phone.call();
    }
}

interface Brand{
    void call();
}

class HuaWei implements Brand{
    @Override
    public void call() {
        System.out.println("这是华为手机");
    }
}

class XiaoMi implements Brand{
    @Override
    public void call() {
        System.out.println("这是小米手机");
    }
}


abstract class Phone{
    Brand brand ;
    public Phone(Brand brand){
        this.brand = brand ;
    }

    abstract void call() ;
}


class FoldedPhone extends Phone{

    public FoldedPhone(Brand brand){
        super(brand);
    }

    public void call() {
        System.out.println("折叠式手机");
        super.brand.call();
    }
}


class UpRightPhone extends Phone{

    public UpRightPhone(Brand brand){
        super(brand);
    }

    public void call() {
        System.out.println("优化式手机");
        super.brand.call();
    }
}

