package com.netty.websocket.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理心跳
 */
@Slf4j
public class HearBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;

            if (idleStateEvent.state() == IdleState.READER_IDLE) {
                log.warn("*****************客户端ID:【" + ctx.channel().id().asShortText() + "】读空闲事件触发***********************");
            } else if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                log.warn("*****************客户端ID:【" + ctx.channel().id().asShortText() + "】写空闲事件触发***********************");
            } else if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                log.warn("+++++++++++客户端ID:【" + ctx.channel().id().asShortText() + "】读写空闲事件触发,关闭通道资源!++++++++++++");
                ctx.channel().close();
                log.warn("-->规定时间内没有收到客户端ID:【" + ctx.channel().id().asShortText() + "】的上行数据, 通道已被断开连接!");
            }
        }
    }
}