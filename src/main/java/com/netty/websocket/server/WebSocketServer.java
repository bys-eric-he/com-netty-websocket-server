package com.netty.websocket.server;

import com.netty.websocket.init.MainWebsocketInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 搭建websocket服务器
 */

@Slf4j
@Component
public class WebSocketServer {

    @Value("${netty.port}")
    private int port;

    /**
     * 主线程池
     */
    private EventLoopGroup bossGroup;
    /**
     * 工作线程池
     */
    private EventLoopGroup workGroup;
    /**
     * 服务器
     */
    private ServerBootstrap server;
    /**
     * 回调
     */
    private ChannelFuture future;

    /**
     * 服务器是否在运行
     */
    private boolean isRunning;

    /**
     * 服务端启动程序
     */
    public void start() {
        try {
            // 绑定主线程组和工作线程组
            // （1）初始化用于Acceptor的主"线程池"以及用于I/O工作的从"线程池"；
            bossGroup = new NioEventLoopGroup();
            workGroup = new NioEventLoopGroup();

            // （2）初始化ServerBootstrap实例， 此实例是netty服务端应用开发的入口
            server = new ServerBootstrap();
            // （3）通过ServerBootstrap的group方法，设置（1）中初始化的主从"线程池"
            server.group(bossGroup, workGroup)
                    // （4）指定通道channel的类型，由于是服务端，故而是NioServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    // （5）设置ServerSocketChannel的处理器
                    .childHandler(new MainWebsocketInitializer());
            //（7）绑定并侦听某个端口
            future = server.bind(port);
            isRunning = true;
            log.info("---> netty server - 启动成功! 端口号: " + port);
            // (8) 等待关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("启动ServerSocket异常", e);
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            stop();
        }
    }

    /**
     * 关闭通道
     */
    public void stop() {
        if (isRunning) {
            future.channel().close();
            log.warn("----Channel通道已关闭!----");
        }
    }

    /**
     * 异步启动
     */
    public void ayncStart() {
        Thread oTh = new Thread(new Runnable() {
            @Override
            public void run() {
                start();
            }
        });
        oTh.setDaemon(true);
        oTh.start();
    }
}
