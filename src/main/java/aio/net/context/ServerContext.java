package aio.net.context;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.HashMap;

/**
 * @author Administrator
 * @title: Shr
 * @description: TODO
 * @date 2019/12/27 15:12
 */
public class ServerContext {

    private final static HashMap<String,AsynchronousSocketChannel> connectMap = new HashMap(100);


    public static void addClient(InetSocketAddress address, AsynchronousSocketChannel socket){
        String ipa = address.getHostName() + ":" + address.getPort();
        connectMap.put(ipa,socket);

    }


}
