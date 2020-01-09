package aio.net.task;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AioThreadFactory implements ThreadFactory {

    //缓存ThreadFactory,key:名称 value:工厂
    private final static HashMap<String, AioThreadFactory> factoryMap = new HashMap<>();
    //缓存ThreadFactory,key:名称 value:aotmic数量
    private final static HashMap<String, AtomicInteger> atomicMap = new HashMap<>();

    private final static int coreSize = 2;
    private final static int maxCoreSize = coreSize + 2;
    private final static int keepLive = 6000;
    private final static TimeUnit unit = TimeUnit.MILLISECONDS;
    private final static int queueSize = 1 << 10;
    private final static LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueSize);

    private String poolName;
    private int priority = Thread.NORM_PRIORITY;
    private boolean deamon = false;

    public static AioThreadFactory getInstance(String name,boolean deamon){
        return getInstance(name,deamon,null);

    }

    public static AioThreadFactory getInstance(String name,boolean deamon,Integer priority){
        AioThreadFactory factory = factoryMap.get(name);
        if(null == factory){
            factory = new AioThreadFactory();
            if(null != priority){
                factory.priority = priority;
            }
            factory.deamon = deamon;
            factory.poolName = name;
            factoryMap.put(name,factory);
            atomicMap.put(name,new AtomicInteger());
        }
        return factory;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(poolName + "-" + atomicMap.get(poolName));
        thread.setPriority(priority);
        thread.setDaemon(deamon);
        return thread;
    }


}
