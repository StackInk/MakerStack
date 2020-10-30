package desgin.strategy;

/**
 * @Author: zl
 * @Date: Create in 2020/9/28 18:34
 * @Description:
 */
public class StrategyDesgin {
    public static void main(String[] args) {
        test(()-> System.out.println("Java输出"));
    }

    public static void test(IPrintInterface printInterface){
        printInterface.print();
    }
}


class Java implements IPrintInterface{

    @Override
    public void print() {
        System.out.println("Java输出");
    }
}

class C implements IPrintInterface{

    @Override
    public void print() {
        System.out.println("C输出");
    }
}

class JavaScript implements IPrintInterface{

    @Override
    public void print() {
        System.out.println("JavaScript输出");
    }
}

interface IPrintInterface{
    void print();
}

