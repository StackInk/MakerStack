package com.bywlstudio.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ServerChannelDemo {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel open = ServerSocketChannel.open();

        open.bind(new InetSocketAddress((10001)));
        SocketChannel accept = open.accept();

        ByteBuffer allocate = ByteBuffer.allocate(1024);
        byte[] bytes = new byte[512];
        while(accept.read(allocate) !=-1){
            allocate.flip();
            allocate.get(bytes);
            System.out.println(new String(bytes,0,bytes.length));
            allocate.clear();
        }


        allocate.put("数据收集成功".getBytes());
        allocate.flip();
        accept.write(allocate);

        open.close();
        accept.close();


    }
}
