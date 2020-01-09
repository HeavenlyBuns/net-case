package aio.net;

import aio.net.task.AioThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.Calendar;
import java.util.concurrent.*;

/**
 * @author Administrator
 * @title: Shr
 * @description: TODO
 * @date 2019/12/27 14:44
 */
public class AioServer {


    private final static Logger LOGGER = LoggerFactory.getLogger(AioServer.class);

    private final static int port = 9000;

    private AsynchronousChannelGroup group = null;
    private AsynchronousServerSocketChannel server = null;
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(1,
            2,
            5000L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue());
    private InetSocketAddress address = new InetSocketAddress(port);
    private AcceptCompletionHandler acceptHandler = null;

    public AsynchronousServerSocketChannel getServer() {
        return server;
    }

    public AioServer() {
        try {
            executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(AioThreadFactory.getInstance("Aio-Server-ThreadPool", false));
            group = AsynchronousChannelGroup.withThreadPool(executor);
            server = AsynchronousServerSocketChannel.open(group);
            server.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            server.setOption(StandardSocketOptions.SO_RCVBUF, 64 * 1024);
            acceptHandler = new AcceptCompletionHandler();
            executor.prestartCoreThread();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void start() {
        try {
            server.bind(address);
            LOGGER.info("服务启动成功......");
//            while (true) {
            server.accept(this, acceptHandler);
            LOGGER.info("服务启动时间: " + Calendar.getInstance().getTime());
//                System.in.read();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new AioServer().start();
        new Thread(() -> {
            LOGGER.info("---------| 服务器守护线程启动 |----------");
            for (; ; ) {
            }
        }).start();
    }
}
