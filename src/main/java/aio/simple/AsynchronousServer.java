package aio.simple;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @title: Shr
 * @description: TODO
 * @date 2019/12/23 15:10
 */
public class AsynchronousServer {

    @Test
    public void testStart() throws IOException {
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
        AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
        server.bind(new InetSocketAddress(8088));
        server.accept("fff", new CompletionHandler<AsynchronousSocketChannel, String>() {
            @Override
            public void completed(AsynchronousSocketChannel ch, String attachment) {
                server.accept(null, this);
                System.out.println("客户端连接上: result = " + ch + " ,attachment = " + attachment);
                //方法handler处理这个链接
                //handler(ch)

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                Future<Integer> read = ch.read(buffer);
                try {
                    System.out.println("read data = " + new String(buffer.array(), 0, read.get()));
                    ch.close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failed(Throwable exc, String attachment) {

            }
        });
        for (; ; ) {
        }
    }


    @Test
    public void resive() throws IOException, ExecutionException, InterruptedException {
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
        server.bind(new InetSocketAddress(8088));
        System.out.println("A: " + System.currentTimeMillis());
        Future<AsynchronousSocketChannel> accept = server.accept();

        System.out.println("B: " + System.currentTimeMillis());
        AsynchronousSocketChannel client = accept.get();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Future<Integer> read = client.read(buffer);
        System.out.println("C: " + System.currentTimeMillis());
        System.out.println("read data = " + new String(buffer.array(), 0, read.get()));

        client.close();

    }


    @Test
    public void repeatRead() throws IOException, ExecutionException, InterruptedException {
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
        server.bind(new InetSocketAddress(8088));
        System.out.println("A: " + System.currentTimeMillis());
        Future<AsynchronousSocketChannel> accept = server.accept();

        System.out.println("B: " + System.currentTimeMillis());
        AsynchronousSocketChannel client = accept.get();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Future<Integer> read = client.read(buffer);
//        System.out.println("read data = " + new String(buffer.array(), 0, read.get()));
        Future<Integer> read2 = client.read(buffer);
        System.out.println("C: " + System.currentTimeMillis());
        System.out.println("read data = " + new String(buffer.array(), 0, read.get()));

        client.close();
    }

    @Test
    public void readTimeout() throws IOException {
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
        server.bind(new InetSocketAddress(8088));
        server.accept("fff", new CompletionHandler<AsynchronousSocketChannel, String>() {
            @Override
            public void completed(AsynchronousSocketChannel ch, String attachment) {
                server.accept(null, this);
                System.out.println("客户端连接上: result = " + ch + " ,attachment = " + attachment);
                //方法handler处理这个链接
                //handler(ch)
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                ch.read(buffer, 3000, TimeUnit.MILLISECONDS, "", new CompletionHandler<Integer, String>() {
                    @Override
                    public void completed(Integer result, String attachment) {
                        if (result == -1) {
                            System.out.println("客户端没有传输数据就执行close了,到 stream end");

                        }
                        if (buffer.limit() == result) {
                            System.out.println("服务端已经获得客户端全部数据");
                            System.out.println("read data = " + new String(buffer.array()));
                        }
                    }

                    @Override
                    public void failed(Throwable exc, String attachment) {

                    }
                });
                try {
//                    System.out.println("read data = " + new String(buffer.array()));
                    ch.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void failed(Throwable exc, String attachment) {

            }
        });

        for (; ; ) {
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        AsynchronousServer server = new AsynchronousServer();
        server.openGroup();

    }

    //    @Test
    public void openGroup() throws IOException, InterruptedException {
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor());
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group);
        server.bind(new InetSocketAddress(6000), 3);
        server.accept("",
                new CompletionHandler<AsynchronousSocketChannel, String>() {
            @Override
            public void completed(AsynchronousSocketChannel result, String attachment) {
                //防止在completed的时候又有新的连接进来
                server.accept(attachment, this);

                try {
                    accept(result);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                new Thread(() -> {

                    for (; ; ) {
                        System.out.println("是否打开: open = " + result.isOpen());
                        try {
                            TimeUnit.SECONDS.sleep(2);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }).start();

                InputStream in = System.in;
                Scanner scanner = new Scanner(in);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if ("".equals(line)) {
                        continue;
                    }

                    if ("exit".equals(line)) {
                        try {
                            result.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    Future<Integer> write = result.write(Charset.defaultCharset().encode(line));
                    try {
                        Integer integer = write.get();
                        System.out.println("服务器已经发送数据: " + integer);
                        System.out.println();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void failed(Throwable exc, String attachment) {
                System.out.println("服务器错误,exc = " + exc.getMessage());
                exc.printStackTrace();
            }
        });
        group.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }


    public void accept(AsynchronousSocketChannel conn) throws ExecutionException, InterruptedException {
        ByteBuffer readBuf = ByteBuffer.allocate(1024);
        /*while (conn.read(readBuf).get() != -1){
            readBuf.flip();
            CharBuffer decode = Charset.defaultCharset().decode(readBuf);
            System.out.println("接收到来自客户端的消息: " + decode);
            readBuf.clear();

        }*/
        conn.read(readBuf,
                100000,
                TimeUnit.MILLISECONDS,
                null,
                new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                if (result > 0) {
                    System.out.println("接收到来自客户端的消息大小: " + result);

                }
                readBuf.flip();
                CharBuffer decode = Charset.defaultCharset().decode(readBuf);
                System.out.println("接收到来自客户端的消息: " + decode);

            }

            @Override
            public void failed(Throwable exc, Object attachment) {

            }
        });


    }

}
