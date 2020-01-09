package aio.net;

import aio.net.context.client.ClientReadCompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Administrator
 * @title: Shr
 * @description: TODO
 * @date 2019/12/27 14:45
 */
public class AioClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(AioClient.class);

    private final static int port = 9000;
    private final static int bindPort = 9001;
    AsynchronousChannelGroup group = null;
    AsynchronousSocketChannel client = null;
    ThreadPoolExecutor executor = null;
    InetSocketAddress serverAddress = new InetSocketAddress("localhost", port);
    InetSocketAddress bindAddress = new InetSocketAddress("localhost", bindPort);

    public AioClient() {
        try {
            executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
            group = AsynchronousChannelGroup.withThreadPool(executor);
            client = AsynchronousSocketChannel.open(group);
            client.bind(bindAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        client.connect(serverAddress);
        ByteBuffer buf = ByteBuffer.allocate(1024);
        client.read(buf,buf,new ClientReadCompletionHandler(client));
        new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {

                while (true) {
                    String s = reader.readLine();
                    if (!"".equals(s)) {
                        Future<Integer> write = client.write(ByteBuffer.wrap(s.getBytes()));
                        Integer integer = write.get();
                        LOGGER.info("客户端发出数据: " + s);

                    }

                }
            } catch (Exception e) {

            }
        }).start();

    }

    public static void main(String[] args) {
        AioClient aioClient = new AioClient();
        aioClient.start();
    }


}
