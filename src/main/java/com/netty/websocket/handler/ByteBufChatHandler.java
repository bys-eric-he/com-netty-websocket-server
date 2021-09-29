package com.netty.websocket.handler;

import com.netty.websocket.channel.UserChannelMap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class ByteBufChatHandler extends SimpleChannelInboundHandler<ByteBuf> {
    public static final byte PING_MSG = 1;
    public static final byte PONG_MSG = 2;
    private final String name = "服务端 Server-20210923";
    private int heartbeatCount = 0;

    /**
     * channel注册到一个EventLoop时调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        log.info("-->ByteBufChatHandler注册到一个EventLoop, 客户端ID:【" + incoming.id().asShortText() + "】IP：【" + ctx.channel().remoteAddress() + "】");
    }

    /**
     * 当Channel中有新的事件消息会自动调用
     */
    @Override
    protected void channelRead0(ChannelHandlerContext context, ByteBuf byteBuf) throws Exception {
        if (byteBuf.getByte(4) == PING_MSG) {
            sendPongMsg(context);
        } else if (byteBuf.getByte(4) == PONG_MSG) {
            log.info(name + " 收到 pong 数据包 来自 " + context.channel().remoteAddress());
        } else {
            byte[] data = new byte[byteBuf.readableBytes() - 5];
            byteBuf.skipBytes(5);
            byteBuf.readBytes(data);
            String content = new String(data);
            System.out.println(name + " 收到消息内容: " + content);
        }
    }

    private void sendPongMsg(ChannelHandlerContext context) {
        ByteBuf buf = context.alloc().buffer(5);
        buf.writeInt(5);
        buf.writeByte(PONG_MSG);
        context.channel().writeAndFlush(buf);
        heartbeatCount++;
        System.out.println(name + " 发送 pong 数据包到 " + context.channel().remoteAddress() + ", 次数: " + heartbeatCount);
    }

    /**
     * 当 ChannelHandler 添加到 ChannelPipeline 调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("-->ByteBufChatHandler有新的客户端连接服务器, handlerAdded 被调用!客户端ID：【" + ctx.channel().id().asLongText()
                + "】IP：【" + ctx.channel().remoteAddress() + "】");
        // 添加到channelGroup 通道组
        UserChannelMap.getChannelGroup().add(ctx.channel());
        UserChannelMap.put("user-" + ctx.channel().id().asShortText(), ctx.channel());
        UserChannelMap.print();
    }

    /**
     * 变为活跃状态（连接到了远程主机），可以接受和发送数据
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("-->ByteBufChatHandler已建立与远程客户端ID:【" + ctx.channel().id().asShortText() + "】IP：【" + ctx.channel().remoteAddress() + "】的连接。");
        // 添加到channelGroup 通道组
        UserChannelMap.getChannelGroup().add(ctx.channel());
        UserChannelMap.put("user-" + ctx.channel().id().asShortText(), ctx.channel());
        UserChannelMap.print();
        ctx.fireChannelActive();
    }

    /**
     * 当 ChannelPipeline 执行抛出异常时调用
     *
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("-->ByteBufChatHandler异常:" + cause.getMessage());
        // 删除通道
        UserChannelMap.getChannelGroup().remove(ctx.channel());
        UserChannelMap.removeByChannelId(ctx.channel().id().asLongText());
        UserChannelMap.remove("user-" + ctx.channel().id().asShortText());
        ctx.channel().close();
    }

    /**
     * 当 ChannelHandler 从 ChannelPipeline 移除时调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("-->ByteBufChatHandler与客户端ID：【" + ctx.channel().id().asLongText() + "】IP:【" + ctx.channel().remoteAddress() + "】断开连接 handlerRemoved 被调用!");
        //删除通道
        UserChannelMap.getChannelGroup().remove(ctx.channel());
        UserChannelMap.removeByChannelId(ctx.channel().id().asLongText());
        UserChannelMap.remove("user-" + ctx.channel().id().asShortText());
        UserChannelMap.print();
    }
}
