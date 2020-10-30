package desgin.single.ehan;

import desgin.single.SingleDesgin;

/**
 * @Author: zl
 * @Date: Create in 2020/10/29 23:07
 * @Description:
 */
public class Single {
    private static final SingleDesgin singleDesgin = new SingleDesgin();

    private Single(){}

    public SingleDesgin newInstance(){
        return singleDesgin ;
    }
}
