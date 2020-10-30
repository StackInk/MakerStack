package desgin.builder;

/**
 * @Author: zl
 * @Date: Create in 2020/10/14 22:34
 * @Description:
 */
public class BuilderDesgin {
    public static void main(String[] args) {
        Person person = new Person.Builder()
                .setAge(18)
                .setCity("武汉").setPhone("10086").build();
    }
}

class Person{
    private String name ;
    private Integer age ;
    private Double money ;
    private String phone ;
    private String city ;

    public Person(String name, Integer age, Double money, String phone, String city) {
        this.name = name;
        this.age = age;
        this.money = money;
        this.phone = phone;
        this.city = city;
    }

    public static class Builder{
        private String name ;
        private Integer age ;
        private Double money ;
        private String phone ;
        private String city ;

        public Builder setName(String name) {
            this.name = name;
            return this ;
        }

        public Builder setAge(Integer age) {
            this.age = age;
            return this ;
        }

        public Builder setMoney(Double money) {
            this.money = money;
            return this ;
        }

        public Builder setPhone(String phone) {
            this.phone = phone;
            return this ;
        }

        public Builder setCity(String city) {
            this.city = city;
            return this ;
        }
        public Person build(){
            return new Person(this.name,this.age,this.money,this.phone,this.city);
        }
    }
}
