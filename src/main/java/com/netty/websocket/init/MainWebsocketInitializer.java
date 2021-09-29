package com.netty.websocket.init;

import com.netty.websocket.handler.*;
import io.netty.channel.ChannelHandler;

public class MainWebsocketInitializer extends WebsocketInitializer {
    /**
     * 业务处理handler调用链
     *
     * @return
     */
    @Override
    protected ChannelHandler getFirstChannelHandler() {
        return new FirstChatHandler();
    }

    /**
     * 业务处理handler调用链
     *
     * @return
     */
    @Override
    protected ChannelHandler getSecondChannelHandler() {
        return new SecondChatHandler();
    }

    /**
     * 业务处理handler调用链
     *
     * @return
     */
    @Override
    protected ChannelHandler getLastChannelHandler() {
        return new LastChatHandler();
    }

    /**
     * 心跳处理handler调用链
     *
     * @return
     */
    @Override
    protected ChannelHandler getHearBeatHandler() {
        return new HearBeatHandler();
    }

    /**
     * 处理输出数据
     *
     * @return
     */
    @Override
    protected ChannelHandler getOutboundHandler() {
        return new OutboundChatHandler();
    }

    /**
     * 处理ByteBuf数据
     *
     * @return
     */
    @Override
    protected ChannelHandler getByteBufChatHandler() {
        return new ByteBufChatHandler();
    }
}
