package desgin.single.enums;

/**
 * @Author: zl
 * @Date: Create in 2020/10/29 23:16
 * @Description:
 */
public class Single {
    public static void main(String[] args) {
        SingleDesgin.INSTANCE.hashCode();
    }


}

enum SingleDesgin{
    INSTANCE
}
