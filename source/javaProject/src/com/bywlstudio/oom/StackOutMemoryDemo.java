package com.bywlstudio.oom;

/**
 * 栈溢出，模拟
 */
public class StackOutMemoryDemo {
    public static void main(String[] args) {
        cal();
    }

    private static void cal() {
        cal();
    }
}
