package nio.simple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @Author : 佘浩然
 * 描述
 * --------> nio的Server
 * @Package : demo
 * @Create :  2017/12/27  15:45
 */
public class NIOServer {

    // 专属高速通道的socket
    private static ServerSocketChannel server;

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

    /**
     * 维护一个时间标签的集合(缓存器)
     */
    private static Map<SelectionKey, String> sessionMsg = new HashMap<>();

    public NIOServer(int port) throws IOException {
        this.port = port;
        server = ServerSocketChannel.open();
        /**
         * 设置为不阻塞，默认为阻塞true
         */
        server.configureBlocking(false);
        server.socket().bind(new InetSocketAddress(port));


        /**
         * 打开多路复用器
         */
        selector = Selector.open();

        /**
         * 注册到多路复用器
         */
        server.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("NIO启动，绑定port为 ： " + port);

    }

    public void listenter() throws Exception {
        while (true) {
            //通过多路复用器selector查看有没有注册事件
            // 数量
            int eventCount = selector.select();
            if (eventCount <= 0) {
                /**
                 * 继续轮询,不断地轮询注册到selector上的多个chanel
                 */
                continue;

            }
            /**
             * 返回所有的注册上的集合
             */
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey next = iterator.next();
                // 处理·
                process(next);

                //处理完就移除一个

                iterator.remove();
            }

        }

    }

    /**
     * 处理客户端的详细事件
     */
    public static void process(SelectionKey key) {
        SocketChannel clientChanel = null;
        /**
         * 判断key可用并且是一个可以接受的事件
         */
        try {

            if (key.isValid() && key.isAcceptable()) {
                /**
                 * 拿到已经加入的连接
                 */
                clientChanel = server.accept();
                /**
                 * 设置为不阻塞
                 */
                clientChanel.configureBlocking(false);
                //注册到多路复用器
                clientChanel.register(selector, SelectionKey.OP_READ);


            } else if (key.isValid() && key.isReadable()) {  //判断是一个有效的，并且支持可读的
                rcBuffer.clear();
                clientChanel = (SocketChannel) key.channel();
                int readLen = clientChanel.read(rcBuffer);   //读取长度

                /**
                 * 如果读取到的数据大于0   说明已经读取到了数据
                 */
                if (readLen > 0) {
                    String msg = new String(rcBuffer.array(), 0, readLen);
                    sessionMsg.put(key, msg);
                    System.out.println("服务器已经收到消息: " + msg);
                    /**
                     * 告诉复用器，下次可以写数据
                     * 状态变换为   SelectionKey.OP_WRITE
                     */
                    clientChanel.register(selector, SelectionKey.OP_WRITE);

                }

                /**
                 * 判断key是有效的并且是可以支持写的操作
                 */
            } else if (key.isValid() && key.isWritable()) {
                /**
                 * 如果集合不包含这个key，那么则返回
                 */
                if (!sessionMsg.containsKey(key)) {
                    return;

                }

                clientChanel = (SocketChannel) key.channel();
                sendBuffer.clear();
                sendBuffer.put(new String(sessionMsg.get(key) + "您好，你的请求已经完成 ！").getBytes());
                /**
                 * 这个实际上是设置读取位,优化读写操作
                 */
                sendBuffer.flip();

                /**
                 * 把缓冲池的内容写到缓冲区
                 */
                clientChanel.write(sendBuffer);

                /**
                 * 再一次注册到多路复用器上，并且转换状态为可读的  OP_READ
                 */
                clientChanel.register(selector, SelectionKey.OP_READ);

            }
        } catch (IOException e) {
            //遇到异常的时候，关闭socket和通道
            try {
                key.cancel();
                clientChanel.socket().close();
                clientChanel.close();


            } catch (IOException e1) {
                e1.printStackTrace();
            }


        }

    }


    public static void main(String[] args) {
        try {
            new NIOServer(port).listenter();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
