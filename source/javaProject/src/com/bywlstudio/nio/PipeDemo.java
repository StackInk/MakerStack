package com.bywlstudio.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

public class PipeDemo {
    public static void main(String[] args) throws IOException {
        Pipe open = Pipe.open();

        ByteBuffer allocate = ByteBuffer.allocate(1024);

        //传输
        Pipe.SinkChannel sink = open.sink();
        allocate.put("管道数据传输".getBytes());
        allocate.flip();
        sink.write(allocate);

        //读取
        Pipe.SourceChannel source = open.source();
        allocate.flip();
        int len = source.read(allocate);
        System.out.println(new String(allocate.array(),0,len));

        source.close();
        sink.close();

    }
}
