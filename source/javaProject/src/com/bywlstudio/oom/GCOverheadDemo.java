package com.bywlstudio.oom;

import java.util.ArrayList;
import java.util.List;

/**
 * GC超过运行限制
 */
public class GCOverheadDemo {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        int i = 0 ;
        try {
            while (true){
                list.add(String.valueOf(++i).intern());
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }finally {
            System.out.println(i);
        }
    }
}
