package aio.simple;

import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousChannelGroup;
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
 * @date 2019/12/23 15:54
 */
public class AsynchronousClient {

    @Test
    public void socketConnect() throws IOException {
        Socket socket = new Socket("localhost", 8088);
        OutputStream outputStream = socket.getOutputStream();

        outputStream.write("测试链接".getBytes());
        outputStream.close();
        socket.close();

    }

    @Test
    public void channelConnect() throws IOException, InterruptedException {
        AsynchronousSocketChannel open = AsynchronousSocketChannel.open();
        open.connect(new InetSocketAddress("localhost", 8088), "客户端", new CompletionHandler<Void, String>() {
            @Override
            public void completed(Void result, String attachment) {
                ByteBuffer dst = ByteBuffer.wrap("客户端channel 链接".getBytes());
                Future<Integer> write = open.write(dst);
                try {
                    System.out.println("客户端已经发送: " + write.get() + " , result = " + result + ", attachment = " + attachment);
                    open.close();
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
        TimeUnit.SECONDS.sleep(10);

    }

    @Test
    public void channelRepeat() throws IOException, InterruptedException {
        AsynchronousSocketChannel open = AsynchronousSocketChannel.open();
        open.connect(new InetSocketAddress("localhost", 8088), "客户端", new CompletionHandler<Void, String>() {
            @Override
            public void completed(Void result, String attachment) {
                try {
                    ByteBuffer dst = ByteBuffer.allocate(1024);
                    for (int i = 0; i < 1024; i++) {
                        dst.put((byte) i);

                    }
                    dst.flip();
                    //测试 result == 1时 注释掉
                    int writeLength = 0;
//                    while (writeLength < dst.limit()) {
//                        Future<Integer> write = open.write(dst);
//                        Integer integer = write.get();
//                        writeLength = writeLength + integer;
//                    }
                    open.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, String attachment) {

            }
        });
        TimeUnit.SECONDS.sleep(10);
    }


    @Test
    public void writeA() throws IOException, InterruptedException {
        AsynchronousSocketChannel open = AsynchronousSocketChannel.open();
        open.connect(new InetSocketAddress("localhost", 8088), "客户端", new CompletionHandler<Void, String>() {
            @Override
            public void completed(Void result, String attachment) {
                try {
                    ByteBuffer dst = ByteBuffer.allocate(1024);
                    for (int i = 0; i < 1024; i++) {
                        dst.put((byte) i);

                    }
                    dst.flip();
                    //测试 result == 1时 注释掉
                    int writeLength = 0;
//                    while (writeLength < dst.limit()) {
                    //延迟为1则有可能发送不过去
                    open.write(dst, 1, TimeUnit.MICROSECONDS, "idx", new CompletionHandler<Integer, String>() {
                        @Override
                        public void completed(Integer result, String attachment) {
                            try {
                                open.close();
                                System.out.println("客户端已经关闭.....");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void failed(Throwable exc, String attachment) {

                        }
                    });
                    writeLength = writeLength + 0;
//                    }
//                    open.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, String attachment) {

            }
        });
        TimeUnit.SECONDS.sleep(5);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        AsynchronousClient client = new AsynchronousClient();
        client.connGroup();

    }

    @Test
    public void connGroup() throws IOException, InterruptedException {
        AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(Executors.newSingleThreadExecutor());
        AsynchronousSocketChannel client = AsynchronousSocketChannel.open(group);
        client.connect(new InetSocketAddress("localhost", 6000),
                "客户端",
                new CompletionHandler<Void, String>() {
                    @Override
                    public void completed(Void result, String attachment) {
                        try {
                            connect(client);

                            Scanner scanner = new Scanner(System.in);
                            while (scanner.hasNextLine()) {
                                String line = scanner.nextLine();
                                if ("".equals(line)) {
                                    continue;
                                }

                                if ("exit".equals(line)) {
                                    try {
                                        client.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                client.write(Charset.defaultCharset().encode(line), "", new CompletionHandler<Integer, String>() {
                                    @Override
                                    public void completed(Integer result, String attachment) {
                                        System.out.println("");

                                    }

                                    @Override
                                    public void failed(Throwable exc, String attachment) {

                                    }
                                });
//                                Integer integer = write.get();
//                                System.out.println("客户端已经发送数据: " + integer);
                                System.out.println();

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failed(Throwable exc, String attachment) {

                    }
                });
        group.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }


    public void connect(AsynchronousSocketChannel client) {
        ByteBuffer readBuf = ByteBuffer.allocate(100);
        client.read(readBuf,
//                1000,
//                TimeUnit.MILLISECONDS,
                "",
                new CompletionHandler<Integer, String>() {
                    @Override
                    public void completed(Integer result, String attachment) {
                        if (result > 0) {
                            readBuf.flip();
                            CharBuffer decode = Charset.defaultCharset().decode(readBuf);
                            System.out.println("收到来自服务端的消息: " + decode);

                        }

                    }

                    @Override
                    public void failed(Throwable exc, String attachment) {

                    }
                });

    }
}
