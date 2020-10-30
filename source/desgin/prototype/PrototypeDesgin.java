package desgin.prototype;

/**
 * @Author: zl
 * @Date: Create in 2020/9/26 21:30
 * @Description:
 */
public class PrototypeDesgin {
    public static void main(String[] args) throws CloneNotSupportedException {
        Person person = new Person();
        person.name = new String("我得");
        person.account = new Account();
        Person person1 = (Person)person.clone();
//        person1.name = new String("我得");
        System.out.println(person.name == person1.name);
    }
}




class Account implements Cloneable{
    int money ;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}


class Person implements Cloneable{
    String name ;
    int age ;
    Account account ;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Person clone = (Person)super.clone();
        clone.account = (Account)account.clone();
        return clone ;
    }
}
