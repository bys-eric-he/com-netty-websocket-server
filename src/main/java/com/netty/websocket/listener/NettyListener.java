package com.netty.websocket.listener;

import com.netty.websocket.server.WebSocketServer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 服务监听启动
 */
@Component
public class NettyListener implements ApplicationListener<ContextRefreshedEvent> {

    @Resource
    private WebSocketServer websocketServer;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            try {
                websocketServer.ayncStart();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}