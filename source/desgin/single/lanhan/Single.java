package desgin.single.lanhan;

import desgin.single.SingleDesgin;

/**
 * @Author: zl
 * @Date: Create in 2020/10/29 23:08
 * @Description:
 */
public class Single {
    private SingleDesgin singleDesgin ;

    private Single(){}

    public SingleDesgin newInstance(){
        if(singleDesgin == null){
            singleDesgin = new SingleDesgin();
        }
        return singleDesgin ;
    }
}
