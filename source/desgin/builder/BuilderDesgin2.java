package desgin.builder;

/**
 * @Author: zl
 * @Date: Create in 2020/10/14 22:41
 * @Description:
 */
public class BuilderDesgin2 {
}

class Person1{
    private String name ;
    private Integer age ;
    private Double money ;
    private String phone ;
    private String city ;



    public static class Builder{
        private Person1 person1 =  new Person1();

        public Builder setName(String name) {
            person1.name = name;
            return this ;
        }

        public Builder setAge(Integer age) {
            person1.age = age;
            return this ;
        }

        public Builder setMoney(Double money) {
            person1.money = money;
            return this ;
        }

        public Builder setPhone(String phone) {
            person1.phone = phone;
            return this ;
        }

        public Builder setCity(String city) {
            person1.city = city;
            return this ;
        }

        public Person1 build(){
            return person1 ;
        }
    }

}
