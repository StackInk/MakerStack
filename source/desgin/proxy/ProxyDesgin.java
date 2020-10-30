package desgin.proxy;

/**
 * @Author: zl
 * @Date: Create in 2020/9/5 8:23
 * @Description:
 */
public class ProxyDesgin {
    public static void main(String[] args) {
        int x = 10 ;
        switch (x){
            case 10 : x++ ;
            default: x++ ; break ;
            case 11 : x++ ;
        }
        System.out.println(x);
    }
}

interface Shoes{
   void run();

   default void write(){}
}

class NaiKe implements Shoes{

    @Override
    public void run() {
        System.out.println("耐克");
    }
}

class Adi implements Shoes{
    @Override
    public void run() {
        System.out.println("阿迪达斯");
    }
}

//代理类
class ShoesProxy implements Shoes {

    Shoes shoes ;

    public ShoesProxy(Shoes shoes){
        this.shoes = shoes ;
    }

    public void run() {
        System.out.println("agency shoes before");
        shoes.run();
        System.out.println("agency shoes after");
    }
}


class ShoesTimer implements Shoes {

    Shoes shoes ;

    public ShoesTimer(Shoes shoes){
        this.shoes = shoes ;
    }

    public void run() {
        System.out.println("log timer shoes before");
        shoes.run();
        System.out.println("log timer shoes after");
    }
}

