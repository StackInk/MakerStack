package com.bywlstudio.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Date;
import java.util.Scanner;

public class DataProgramClientDemo {
    public static void main(String[] args) throws IOException {
        DatagramChannel open = DatagramChannel.open();
        open.configureBlocking(false);
        ByteBuffer allocate = ByteBuffer.allocate(1024);
        Scanner scanner = new Scanner(System.in);
        while(scanner.hasNext()){
            String str = scanner.next();
            allocate.put((new Date().toString()+"\n"+str).getBytes());
            allocate.flip();
            open.send(allocate,new InetSocketAddress("127.0.0.1",10001));
            allocate.clear();
        }
        open.close();

    }
}
