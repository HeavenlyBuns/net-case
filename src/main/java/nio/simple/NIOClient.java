package nio.simple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @Author : 佘浩然
 * 描述
 * --------> nio客户端
 * @Package : demo
 * @Create :  2017/12/27  16:40
 */
public class NIOClient {

    // 专属高速通道的socket
    private static SocketChannel client;

    private static int port = 8080;

    /* 多路复用器 */
    private static Selector selector;

    /**
     * 信息写入
     */
    private static ByteBuffer rcBuffer = ByteBuffer.allocate(1024);

    /**
     * 信息写出
     */
    private static ByteBuffer sendBuffer = ByteBuffer.allocate(1024);

    public NIOClient() throws Exception {
        client = SocketChannel.open();
        client.configureBlocking(false);
        client.connect(new InetSocketAddress("localhost",port));
        /**
         * 设置为不阻塞，默认为阻塞true
         */

        /**
         * 打开多路复用器
         */
        selector = Selector.open();

        /**
         * 注册到多路复用器
         */
        client.register(selector, SelectionKey.OP_CONNECT);

        System.out.println("NIO客户端启动，绑定port为 ： " + port);

    }


    public void session() throws Exception {
        if (client.isConnectionPending()) {
            client.finishConnect();

            client.register(selector, SelectionKey.OP_WRITE);

            System.out.println("已经连接到服务器了，可以进行控制了！");

        }

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String name = scanner.nextLine();
            if ("".equals(name)) {
                continue;
            }

            if ("finssh".equals(name)) {
                System.exit(0);

            }
            System.out.println(name);
            process(name);

        }


    }

    public void process(String name) {
        boolean waitHelo = true;
        Iterator<SelectionKey> iterator = null;
        Set<SelectionKey> keySet = null;

        while (waitHelo) {

            try {
                /**
                 * 这里会阻塞
                 */
                Integer read = selector.select();
                if(read == 0){
                    continue;

                }
                keySet = selector.selectedKeys();
                iterator = keySet.iterator();

                while (iterator.hasNext()){
                    SelectionKey key = iterator.next();

                    if(key.isValid() && key.isWritable()){

                        sendBuffer.clear();
                        sendBuffer.put(name.getBytes());
                        sendBuffer.flip();
                        client.write(sendBuffer);
                        client.configureBlocking(false);
                        client.register(selector,SelectionKey.OP_READ);

                        /**
                         * 服务器发送消息回来给客户端去读
                         */
                    }else if(key.isValid() && key.isReadable()){
                        rcBuffer.clear();
                        int rcLen = client.read(rcBuffer);
                        if(rcLen > 0){
                            //rcBuffer.flip();
                            System.out.println("客户端反馈的消息： " + new String(rcBuffer.array(),0,rcLen));
                            client.configureBlocking(false);
                            client.register(selector,SelectionKey.OP_WRITE);
                            waitHelo = false;


                        }


                    }
                    iterator.remove();

                }

            } catch (Exception e) {
                try {
                    ((SelectionKey)keySet).cancel();
                    client.socket().close();
                    client.close();


                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }


        }
    }


    public static void main(String[] args) {
        try {
            new NIOClient().session();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
