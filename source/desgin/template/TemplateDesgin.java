package desgin.template;

import java.io.Serializable;

/**
 * @Author: zl
 * @Date: Create in 2020/10/24 15:10
 * @Description:
 */
public class TemplateDesgin {
    public static void main(String[] args) {
        SubClass subClass = new SubClass();
        subClass.exec();
    }

}

abstract class AbstractClass{
    public final void exec(){
        method1();
        method2();
        method3();
    }

    protected void method1(){
        System.out.println("这是方法一");
    }

    protected void method2(){
        System.out.println("这是方法二");
    }

    protected void method3(){
        System.out.println("这是方法三");
    }
}

class SubClass extends AbstractClass{
    @Override
    protected void method1() {
        System.out.println("子类改变方法一");
    }

    @Override
    protected void method2() {
        super.method2();
    }

    @Override
    protected void method3() {
        System.out.println("子类改变方法三");
    }
}


