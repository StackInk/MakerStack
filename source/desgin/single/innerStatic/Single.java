package desgin.single.innerStatic;

import desgin.single.SingleDesgin;

/**
 * @Author: zl
 * @Date: Create in 2020/10/29 23:09
 * @Description:
 */
public class Single {

    private Single(){}

    public SingleDesgin newInstance(){
        return SingleBuild.singleDesgin;
    }

    private static class SingleBuild{
        static SingleDesgin singleDesgin = new SingleDesgin();
    }

}
