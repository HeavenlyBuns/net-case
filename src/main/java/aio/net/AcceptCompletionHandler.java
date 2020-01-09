package aio.net;

import aio.net.context.ReadCompletionHandler;
import aio.net.context.ServerContext;
import aio.net.task.ServerWriteWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Administrator
 * @title: Shr
 * @description: TODO
 * @date 2019/12/27 14:51
 */
public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AioServer> {

    private final static Logger LOGGER = LoggerFactory.getLogger(AcceptCompletionHandler.class);
//    public static ByteBuffer read = ByteBuffer.allocate(1024);

    @Override
    public void completed(AsynchronousSocketChannel result, AioServer attachment) {

        try {
            LOGGER.info("接收到连接,address: " + result.getRemoteAddress());
            receiptAccept(result);
            ServerContext.addClient((InetSocketAddress) result.getRemoteAddress(),result);
            if(result.isOpen()){
                //读取
                readAccept(result);
                startWrite(result);

            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            attachment.getServer().accept(attachment,this);

        }
    }

    @Override
    public void failed(Throwable exc, AioServer attachment) {

    }

    /**
     * 读取事件,
     *      暂未完成: 粘包,拆包,
     * @param socket
     */
    public void readAccept(AsynchronousSocketChannel socket){
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        socket.read(byteBuffer,byteBuffer,new ReadCompletionHandler(socket));

    }


    /**
     * 回执accept事件
     * @param socket
     */
    public void receiptAccept(AsynchronousSocketChannel socket) throws ExecutionException, InterruptedException, IOException {
        Future<Integer> write = socket.write(ByteBuffer.wrap("连接成功".getBytes()));
        Integer receiptSize = write.get();
        LOGGER.info("客户端请求握手成功,address = {} ,回执数据Size = ({}) !",socket.getRemoteAddress(),receiptSize);
    }

    public void startWrite(AsynchronousSocketChannel channel){
        new Thread(new ServerWriteWorker(channel))
                .start();

    }

}
