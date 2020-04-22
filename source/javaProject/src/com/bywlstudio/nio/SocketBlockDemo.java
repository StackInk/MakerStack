package com.bywlstudio.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.Scanner;

/***
 * 客户端
 */
public class SocketBlockDemo {
    public static void main(String[] args) throws IOException {
        //创建一个通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 10001));

        //分配缓冲区
        ByteBuffer allocate = ByteBuffer.allocate(1024);
        Scanner scanner = new Scanner(System.in);

            allocate.put(scanner.nextLine().getBytes());
            socketChannel.write(allocate);

            //终止输出，接受输入
            socketChannel.shutdownOutput();

            int len = 0 ;
            while((len = socketChannel.read(allocate))!=-1){
                allocate.flip();
                System.out.println(new String(allocate.array(),0,len));
                allocate.clear();
            }
    }
}
