package com.satech.pharmacy.connector;

import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Mehmet CELIKSOY
 */
@Component
public class TcpServerManager {

    /**
     * The listening port (check that it's not already in use)
     */
    @Value("${satech.mina.server.port}")
    private int port;

    @Value("${satech.mina.coreThreadCount}")
    private int coreThreadCount;

    @Value("${satech.mina.maxThreadCount}")
    private int maxThreadCount;

    @Value("${satech.mina.threadTimeoutSeconds}")
    private int threadTimeoutSeconds;

    private final NioSocketAcceptor nioSocketAcceptor;

    private final RequestHandler requestHandler;

    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    public TcpServerManager(NioSocketAcceptor nioSocketAcceptor, RequestHandler requestHandler) {
        this.nioSocketAcceptor = nioSocketAcceptor;
        this.requestHandler = requestHandler;
    }

    @PostConstruct
    public void bind() {
        //        ProfilerTimerFilter profiler = new ProfilerTimerFilter(TimeUnit.MILLISECONDS, IoEventType.MESSAGE_RECEIVED);
        //        nioSocketAcceptor.getFilterChain().addFirst("Profiler", profiler);
        //
        //        nioSocketAcceptor.getFilterChain().addLast("logger", new LoggingFilter());
        //        nioSocketAcceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(messageCodecFactory));

        ExecutorFilter executorFilter = new ExecutorFilter(coreThreadCount, maxThreadCount, threadTimeoutSeconds,
                TimeUnit.SECONDS);
        threadPoolExecutor = (ThreadPoolExecutor) executorFilter.getExecutor();

        nioSocketAcceptor.getFilterChain().addLast("executorFilter", executorFilter);
        nioSocketAcceptor.setReuseAddress(true);
        nioSocketAcceptor.setHandler(requestHandler);

        // nioSocketAcceptor.addListener(new ServiceListener());
        try {
            nioSocketAcceptor.bind(new InetSocketAddress(port));
            System.out.println("Satech Communicator is ready on port : " + port + ", if you are !");
        } catch (IOException e) {
            System.out.println("Satech Communicator can not bind to port : " + port);
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void unbind() {
        nioSocketAcceptor.unbind(new InetSocketAddress(port));
        System.out.println("Satech Communicator give the port : " + port + " free. Goodbye");
    }

    public ThreadPoolExecutor getExecutor() {
        return threadPoolExecutor;
    }

}
