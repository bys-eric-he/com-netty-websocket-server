package com.netty.websocket.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ChannelOutboundHandler(出站): 处理输出数据
 */
public class OutboundChatHandler extends ChannelOutboundHandlerAdapter {
    private final Logger L = LoggerFactory.getLogger(getClass());

    @Override
    public void write(ChannelHandlerContext ctx, Object msg,
                      ChannelPromise promise) throws Exception {
        String sChannelId = null;
        Channel incoming = ctx.channel();
        if (incoming != null) {
            sChannelId = incoming.id().asShortText();
        }
        L.info("输出消息 message : {}, IP={}, 通道Id={}", msg, ctx.channel().remoteAddress(), sChannelId);
        super.write(ctx, msg, promise);
    }
}
