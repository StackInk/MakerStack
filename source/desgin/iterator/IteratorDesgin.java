package desgin.iterator;

import java.util.Iterator;

/**
 * @Author: zl
 * @Date: Create in 2020/10/12 20:17
 * @Description:
 */
public class IteratorDesgin {
}


class ArrayList_{
    Object[] objects = new Object[16] ;

    int size = 0 ;

    public void add(Object object){
        if(objects.length < size){
            Object[] newObjects = new Object[objects.length*2];
            System.arraycopy(objects,0,newObjects,0,objects.length);
            objects = newObjects ;
        }else{
            objects[size] = object ;
            size++;
        }
    }

    public Object remove(){
        Object object = objects[size];
        objects[size] = null ;
        size--;
        return object ;
    }

    public Iterator_ iterator(){
        return new ListIterator_();
    }

    private class ListIterator_ implements Iterator_{
        int currentIndex = 0 ;

        @Override
        public boolean hasNext() {
            if(currentIndex <= size){
                return true ;
            }
            return false;
        }

        @Override
        public Object next() {
            return objects[currentIndex++];
        }
    }
}

class LinkedList{

}

interface Iterator_{
    boolean hasNext();
    Object next();
}

