package com.bywlstudio.nio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class ChannelNioDemo {


    public static void main(String[] args) throws IOException {
        Charset gbk = Charset.forName("GBK");

        //主要是字符和字节之间的解码和加码
        CharBuffer cb = CharBuffer.allocate(1024);
        cb.put("公众号：MakerStack");
        cb.flip();


        //编码
        ByteBuffer encode = gbk.encode(cb);
        for (int i = 0; i < encode.limit(); i++) {
            System.out.println(encode.get());
        }

        //解码
        encode.flip();
        CharBuffer decode = gbk.decode(encode);
        System.out.println(decode.toString());
        System.out.println("==============================");

        Charset charset = Charset.forName("UTF-8");
        encode.flip();
        CharBuffer decode1 = charset.decode(encode);
        System.out.println(decode1.toString());
    }

    private static void charTest() throws CharacterCodingException {
        Charset gbk = Charset.forName("GBK");
        //获取编码器
        CharsetEncoder ce = gbk.newEncoder();
        //获取解码器
        CharsetDecoder cd = gbk.newDecoder();

        //主要是字符和字节之间的解码和加码
        CharBuffer cb = CharBuffer.allocate(1024);
        cb.put("公众号：MakerStack");
        cb.flip();

        //编码
        ByteBuffer encode = ce.encode(cb);
        for (int i = 0; i < encode.limit(); i++) {
            System.out.println(encode.get());
        }
        //解码
        encode.flip();
        CharBuffer decode = cd.decode(encode);
        System.out.println(decode.toString());
        System.out.println("==============================");
        Charset charset = Charset.forName("UTF-8");
        encode.flip();
        CharBuffer decode1 = charset.decode(encode);
        System.out.println(decode1.toString());
    }

    /**
     * 分散和聚集
     * @throws IOException
     */
    private static void tranferAndCol() throws IOException {
        RandomAccessFile rw = new RandomAccessFile("G:\\BugReport.txt", "rw");
        //获取通道
        FileChannel channel = rw.getChannel();
        //分配两个缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(12);
        ByteBuffer buffer1 = ByteBuffer.allocate(1024);

        //分散读取，封装为一个数组
        ByteBuffer[] buffers = {buffer,buffer1};
        channel.read(buffers);
        //转换为读模式
        for (ByteBuffer byteBuffer : buffers) {
            byteBuffer.flip();
        }

        System.out.println(new String(buffers[0].array(), 0, buffers[0].limit()));
        System.out.println(new String(buffers[1].array(), 0, buffers[1].limit()));

        RandomAccessFile rw1 = new RandomAccessFile("G:\\BugReport1.txt", "rw");
        FileChannel channel1 = rw1.getChannel();
        channel1.write(buffers);
    }

    /**
     * 通道直接传输
     */
    private static void channelTranfer() {
        try {
            FileChannel in = FileChannel.open(Paths.get("G:\\BugReport.txt"), StandardOpenOption.READ);
            FileChannel out = FileChannel.open(Paths.get("G:\\BugReport3.txt"),
                    StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
            out.transferFrom(in,0,in.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通道写入，连接直接缓冲流
     */
    private static void channelTest() {
        //打开文件，按照只读模式
        try {
            FileChannel in = FileChannel.open(Paths.get("G:\\BugReport.txt"), StandardOpenOption.READ);
            FileChannel out = FileChannel.open(Paths.get("G:\\BugReport3.txt"),
                    StandardOpenOption.WRITE, StandardOpenOption.READ, StandardOpenOption.CREATE);
            //获取物理映射空间
            MappedByteBuffer inMap = in.map(FileChannel.MapMode.READ_ONLY, 0, in.size());
            MappedByteBuffer outMap = out.map(FileChannel.MapMode.READ_WRITE, 0, in.size());

            //复制文件，先直接在物理空间中获取，然后再进行写入
            byte[] bytes = new byte[inMap.limit()];
            inMap.get(bytes);
            outMap.put(bytes);

            System.out.println();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通道复制文件案例
     *  非直接缓冲区
     */
    private static void channelCopy() {
        FileInputStream fileInputStream = null ;
        FileOutputStream fileOutputStream = null ;
        FileChannel channelIn = null ;
        FileChannel channelOut = null ;
        ByteBuffer allocate = ByteBuffer.allocate(1024);
        try {
            fileInputStream = new FileInputStream("G:\\BugReport.txt");
            fileOutputStream = new FileOutputStream("G:\\BugReport2.txt");
            channelIn = fileInputStream.getChannel() ;
            channelOut = fileOutputStream.getChannel();
            while (channelIn.read(allocate) != -1){
                allocate.flip();
                channelOut.write(allocate);
                allocate.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(channelOut != null ){
                    channelOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(channelIn !=null){
                        channelIn.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        if(fileOutputStream !=null){
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        try {
                            if(fileInputStream !=null){
                                fileInputStream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
