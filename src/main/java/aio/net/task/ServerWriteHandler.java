package aio.net.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

/**
 * @author Administrator
 * @title: Shr
 * @description: TODO
 * @date 2019/12/30 14:11
 */
public class ServerWriteHandler implements CompletionHandler<Integer, ByteBuffer> {
    private final static Logger LOGGER = LoggerFactory.getLogger(ServerWriteHandler.class);

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        if(result <= 0){
            LOGGER.warn("发送数据出现异常");

        }else {
            System.out.println("已经发送数据: " + attachment);

        }

    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {

    }
}
