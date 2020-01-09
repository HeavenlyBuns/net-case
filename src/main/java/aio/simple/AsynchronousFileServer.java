package aio.simple;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @title: Shr
 * @description: TODO
 * @date 2019/12/20 10:40
 */
public class AsynchronousFileServer {


    @Test
    public void testLockA() throws IOException, ExecutionException, InterruptedException {
        //创建一个异步文件通道
        //异步文件在文件中没有当前位置,而是将文件位置指定给启动异步操作的每个读取和写入方法
        //CompletionHandler 被指定参数,并被调用以消耗I/O操作的结果
        //此类还定义了启动异步操作的读取和写入方法,并返回Future对象以表示操作的挂起结果
        Path path = Paths.get("D:\\txt\\a.txt");
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
        Future<FileLock> future = fileChannel.lock();
        FileLock fileLock = future.get();
        System.out.println("A get lock time = " + System.currentTimeMillis());
        TimeUnit.SECONDS.sleep(8);
        fileLock.release();
        System.out.println("A release lock time = " + System.currentTimeMillis());
        fileChannel.close();
    }

    @Test
    public void testLockB() throws IOException, ExecutionException, InterruptedException {
        Path path = Paths.get("D:\\txt\\a.txt");
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
        Future<FileLock> future = fileChannel.lock();
        System.out.println("B begin lock time = " + System.currentTimeMillis());
        FileLock fileLock = future.get();
        System.out.println("B end lock time = " + System.currentTimeMillis());
        fileLock.release();
        fileChannel.close();
    }

    @Test
    public void testPosition1() throws IOException, ExecutionException, InterruptedException {
        Path path = Paths.get("D:\\txt\\a.txt");
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
        Future<FileLock> future = fileChannel.lock(0, 10, false);
        FileLock fileLock = future.get();
        System.out.println("A get lock time = " + System.currentTimeMillis());
        TimeUnit.SECONDS.sleep(8);
        fileLock.release();
        System.out.println("A release lock time = " + System.currentTimeMillis());

        fileChannel.close();
    }

    @Test
    public void testPosition2() throws IOException, ExecutionException, InterruptedException {
        Path path = Paths.get("D:\\txt\\a.txt");
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
        Future<FileLock> future = fileChannel.lock(0, 10, false);
        System.out.println("B begin lock time = " + System.currentTimeMillis());
        FileLock fileLock = future.get();
        System.out.println("B end lock time = " + System.currentTimeMillis());
        fileLock.release();
        fileChannel.close();
    }

    @Test
    public void testChannel() throws IOException, ExecutionException, InterruptedException {
        Path path = Paths.get("D:\\txt\\a.txt");
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
        System.out.println("start completed time = " + System.currentTimeMillis());
        fileChannel.lock("附加值", new CompletionHandler<FileLock, String>() {
            @Override
            public void completed(FileLock result, String attachment) {
                System.out.println("开始调用.......,attachment = " + attachment);
                try {
                    result.release();
                    fileChannel.close();
                    System.out.println("release and close .... ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, String attachment) {
                System.out.println("调用失败.....");
                System.out.println("调用失败错误信息 = " + exc.getMessage());
            }
        });
        System.out.println("end completed time = " + System.currentTimeMillis());
        TimeUnit.SECONDS.sleep(20);
    }

    @Test
    public void testChannelFailed() throws IOException, ExecutionException, InterruptedException {
        Path path = Paths.get("D:\\txt\\a.txt");
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
        System.out.println("start completed time = " + System.currentTimeMillis());
        fileChannel.close();
        fileChannel.lock("附加值", new CompletionHandler<FileLock, String>() {
            @Override
            public void completed(FileLock result, String attachment) {
                System.out.println("开始调用.......,attachment = " + attachment);
                try {
                    result.release();
                    fileChannel.close();
                    System.out.println("release and close .... ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, String attachment) {
                System.out.println("调用失败.....attachment = " + attachment);
                System.out.println("调用失败错误信息 = " + exc.getMessage());
            }
        });
        System.out.println("end completed time = " + System.currentTimeMillis());
        TimeUnit.SECONDS.sleep(3);
    }

    @Test
    public void testChannelLock() throws IOException, ExecutionException, InterruptedException {
        Path path = Paths.get("D:\\txt\\a.txt");
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
        System.out.println("start completed time = " + System.currentTimeMillis());
        fileChannel.lock("附加值", new CompletionHandler<FileLock, String>() {
            @Override
            public void completed(FileLock result, String attachment) {
                System.out.println("开始调用.......,attachment = " + attachment);
                try {
                    TimeUnit.SECONDS.sleep(10);
                    result.release();
                    fileChannel.close();
                    System.out.println("release and close .... ");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, String attachment) {
                System.out.println("调用失败.....attachment = " + attachment);
                System.out.println("调用失败错误信息 = " + exc.getMessage());
            }
        });
        System.out.println("end completed time = " + System.currentTimeMillis());
        TimeUnit.SECONDS.sleep(30);
    }

    @Test
    public void testRead() throws IOException, ExecutionException, InterruptedException {
        Path path = Paths.get("D:\\txt\\a.txt");
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE , StandardOpenOption.READ);
        System.out.println("start completed time = " + System.currentTimeMillis());

        ByteBuffer dst = ByteBuffer.allocate(1024);
        Future<Integer> read = fileChannel.read(dst, 0);
        System.out.println("read buffer = " + read.get());

        fileChannel.close();
        System.out.println("读取的内容是: " + new String(dst.array()));
    }


    @Test
    public void testRead2() throws IOException, ExecutionException, InterruptedException {
        Path path = Paths.get("D:\\txt\\a.txt");
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE , StandardOpenOption.READ);
        System.out.println("start completed time = " + System.currentTimeMillis());

        ByteBuffer dst = ByteBuffer.allocate(1024);
        fileChannel.read(dst, 0, "附加项", new CompletionHandler<Integer, String>() {
            @Override
            public void completed(Integer result, String attachment) {
                System.out.println("result = " + result + ", attachment = " + attachment);

            }

            @Override
            public void failed(Throwable exc, String attachment) {

            }
        });
        fileChannel.close();
        TimeUnit.SECONDS.sleep(3);
        System.out.println("读取的内容是: " + new String(dst.array()));

    }

    @Test
    public void testWrite() throws IOException, ExecutionException, InterruptedException {
        Path path = Paths.get("D:\\txt\\a.txt");
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE , StandardOpenOption.READ);
        System.out.println("start completed time = " + System.currentTimeMillis());


        ByteBuffer dst = ByteBuffer.wrap("测试一下哈".getBytes());
        //position 代表文件中的通道位置
        Future<Integer> write = fileChannel.write(dst, fileChannel.size());
        System.out.println("write num = " + write.get());

        fileChannel.close();

    }

    @Test
    public void testWrite2() throws IOException, ExecutionException, InterruptedException {
        Path path = Paths.get("D:\\txt\\a.txt");
        AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE , StandardOpenOption.READ);
        System.out.println("start completed time = " + System.currentTimeMillis());

        ByteBuffer dst = ByteBuffer.wrap("佘浩然测试".getBytes());
        fileChannel.write(dst, fileChannel.size(), "附加项", new CompletionHandler<Integer, String>() {
            @Override
            public void completed(Integer result, String attachment) {
                System.out.println("result = " + result + ", attachment = " + attachment);

            }

            @Override
            public void failed(Throwable exc, String attachment) {

            }
        });
        fileChannel.close();
        TimeUnit.SECONDS.sleep(3);
        System.out.println("读取的内容是: " + new String(dst.array()));

    }








}
