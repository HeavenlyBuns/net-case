package aio.net.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;

/**
 * @author Administrator
 * @title: Shr
 * @description: TODO
 * @date 2019/12/27 16:43
 */
public class ReadCompletionHandler implements CompletionHandler<Integer, Object> {

    private Logger LOGGER = LoggerFactory.getLogger(ReadCompletionHandler.class);

    private AsynchronousSocketChannel client;

    public ReadCompletionHandler(AsynchronousSocketChannel client) {
        this.client = client;
    }

    @Override
    public void completed(Integer result, Object atta) {
        try {

            if (result <= 0) {
//                attachment.close();
                LOGGER.info("未接收到客户端数据: " + client.getRemoteAddress());
            } else {
                ByteBuffer buffer = (ByteBuffer)atta;
                buffer.flip();
                CharBuffer decode = Charset.defaultCharset().decode(buffer);
                String msg = String.valueOf(decode);
                LOGGER.info("来自客户端[" + client.getRemoteAddress() + "]的消息: " + msg);
                //在读取完后,必须得清除,如果不清除buffer,那么就会相当于在执行一次read,导致下一次执行读取不到数据
                buffer.clear();

                if("exit".equals(msg)){
                    client.close();

                }else {
                    client.read(buffer, buffer, this);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    @Override
    public void failed(Throwable exc, Object attachment) {

    }
}
