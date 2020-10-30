package desgin.decorator;

/**
 * @Author: zl
 * @Date: Create in 2020/9/7 15:28
 * @Description:
 */
public class DecoratorDesgin {
    public static void main(String[] args) {
        //全部都装饰
        Hero hero1 = new Skin(new Weapons(new Clothes(new HanXin())));
        hero1.attack();

        //部分装饰
        Hero hero2 = new Skin(new Clothes(new HanXin()));
        hero2.attack();
    }
}

interface Hero{
    void attack();
}

class HanXin implements Hero{
    @Override
    public void attack() {
        System.out.println("赵子龙参见陛下");
    }
}


class Skin implements Hero{
    private Hero hero ;
    public Skin(Hero hero){
        this.hero = hero ;
    }
    @Override
    public void attack(){
        hero.attack();
    }
}


class Weapons extends Skin{

    @Override
    public void attack() {
        System.out.println("我的大刀在哪里");
        super.attack();
    }

    public Weapons(Hero hero) {
        super(hero);
    }
}

class Clothes extends Skin{
    @Override
    public void attack() {
        System.out.println("我的战袍准备好了嘛");
        super.attack();
    }

    public Clothes(Hero hero) {
        super(hero);
    }
}