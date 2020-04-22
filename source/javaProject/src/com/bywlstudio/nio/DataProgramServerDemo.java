package com.bywlstudio.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class DataProgramServerDemo {
    public static void main(String[] args) throws IOException {
        DatagramChannel dc = DatagramChannel.open();
        dc.configureBlocking(false);
        dc.bind(new InetSocketAddress(10001));

        Selector selector = Selector.open();
        dc.register(selector, SelectionKey.OP_READ);

        while(selector.select() > 0){
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey next = iterator.next();
                if(next.isReadable()){
                    ByteBuffer allocate = ByteBuffer.allocate(1024);

                    dc.receive(allocate);
                    allocate.flip();
                    System.out.println(new String(allocate.array(),0,allocate.limit()));
                    allocate.clear();
                }
            }
            iterator.remove();
        }
    }
}
