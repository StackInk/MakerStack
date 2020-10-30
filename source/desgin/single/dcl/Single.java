package desgin.single.dcl;

import desgin.single.SingleDesgin;

/**
 * @Author: zl
 * @Date: Create in 2020/10/29 23:11
 * @Description:
 */
public class Single {
    private volatile SingleDesgin singleDesgin ;

    public SingleDesgin newInstance(){
        if(singleDesgin == null){
            synchronized (Single.class){
                if(singleDesgin == null){
                    singleDesgin = new SingleDesgin();
                }
            }
        }
        return singleDesgin ;
    }

}
