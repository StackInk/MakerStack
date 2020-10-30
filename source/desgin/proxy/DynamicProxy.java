package desgin.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @Author: zl
 * @Date: Create in 2020/9/5 10:37
 * @Description:
 */
public class DynamicProxy {
    public static void main(String[] args) {
        NaiKe naiKe = new NaiKe();
        System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");
        Shoes shoes = (Shoes) Proxy.newProxyInstance(NaiKe.class.getClassLoader(), new Class[]{Shoes.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("begin timer : " + System.currentTimeMillis());
                method.invoke(naiKe,args);
                System.out.println("after timer : " + System.currentTimeMillis());
                return null;
            }
        });
        shoes.run();
    }
}
