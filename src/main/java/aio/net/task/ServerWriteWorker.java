package aio.net.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.Charset;

/**
 * @author Administrator
 * @title: Shr
 * @description: TODO
 * @date 2019/12/30 14:08
 */
public class ServerWriteWorker implements Runnable{

    private final static Logger LOGGER = LoggerFactory.getLogger(ServerWriteWorker.class);

    private AsynchronousSocketChannel client;

    public ServerWriteWorker(AsynchronousSocketChannel client) {
        this.client = client;
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        for(;;){
            try {
                String line = reader.readLine();
                if("".equals(line))
                    continue;

                if("exit".equals(line)){
                    client.close();
                }

                ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
                writeBuffer.put(Charset.defaultCharset().encode(line));
                writeBuffer.flip();
                client.write(writeBuffer,writeBuffer,new ServerWriteHandler());
                LOGGER.info("服务器已经向客户端({})发送数据: {}",client.getRemoteAddress(),line);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


}
