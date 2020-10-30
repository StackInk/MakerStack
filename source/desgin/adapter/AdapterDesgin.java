package desgin.adapter;

/**
 * @Author: zl
 * @Date: Create in 2020/10/19 22:29
 * @Description:
 */
public class AdapterDesgin {
    public static void main(String[] args) {
        DC5V dc5V =  new Adapter(new AC110V());
        int dc5 = dc5V.dc5v();
        System.out.println("转换后的电压为：" + dc5 + " 伏...");
    }
}

class AC220V implements ACV{
    public int output() {
        return 220;
    }
}

class AC110V implements ACV{
    public int output() {
        return 110;
    }
}

interface DC5V {
    int dc5v();
}

class Adapter  implements DC5V {

    private ACV acv ;

    public Adapter(AC220V ac220V) {
        this.acv = ac220V;
    }

    public Adapter(AC110V ac110V) {
        this.acv = ac110V;
    }

    @Override
    public int dc5v() {
        int ac = 0;
        if (acv != null) {
            ac = acv.output();
        }
        int sta = ac / 5;
        return (ac / sta);
    }
}

interface ACV{
    int output();
}