package com.netty.websocket.handler;

import com.alibaba.fastjson.JSON;
import com.netty.websocket.channel.UserChannelMap;
import com.netty.websocket.pojo.MessageDataContent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * ChannelInboundHandler(入站): 处理输入数据和Channel状态类型改变。
 * 对于入站的Handler可能会继承SimpleChannelInboundHandler或者ChannelInboundHandlerAdapter,
 * <p>
 * 而SimpleChannelInboundHandler又是继承于ChannelInboundHandlerAdapter,
 * 最大的区别在于SimpleChannelInboundHandler会对没有外界引用的资源进行一定的清理,并且入站的消息可以通过泛型来规定。
 * <p>
 * Channel 生命周期(执行顺序也是从上倒下)
 * （1）channelRegistered: channel注册到一个EventLoop。
 * （2）channelActive: 变为活跃状态（连接到了远程主机），可以接受和发送数据
 * （3）channelInactive: channel处于非活跃状态，没有连接到远程主机
 * （4）channelUnregistered: channel已经创建，但是未注册到一个EventLoop里面，也就是没有和Selector绑定
 */
@Slf4j
@ChannelHandler.Sharable
public class FirstChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    /**
     * channel注册到一个EventLoop时调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        log.info("-->FirstChatHandler注册到一个EventLoop, 客户端ID:【" + incoming.id().asShortText() + "】IP：【" + ctx.channel().remoteAddress() + "】");
    }

    /**
     * 当Channel中有新的事件消息会自动调用
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        // 当接收到数据后会自动调用
        Channel incoming = ctx.channel();
        // 获取客户端发送过来的文本消息
        log.info("-->FirstChatHandler服务器收到客户端:{} 消息：{} IP: {}", incoming.id().asShortText(), msg.text(), ctx.channel().remoteAddress());
        MessageDataContent message = JSON.parseObject(msg.text(), MessageDataContent.class);

        switch (message.getType()) {
            case DATA: {
                log.info("++>FirstChatHandler收到客户端ID:【"
                        + incoming.id().asShortText() + "】IP：【"
                        + ctx.channel().remoteAddress() + "】的数据包: " + message.getMessage());
                break;
            }
            case KEEPALIVE: {
                // 接收心跳消息
                log.info("++>FirstChatHandler收到客户端ID:【"
                        + incoming.id().asShortText() + "】IP：【"
                        + ctx.channel().remoteAddress() + "】的心跳包: " + message.getMessage());
                ctx.channel().writeAndFlush(new TextWebSocketFrame("-->FirstChatHandler心跳包收到后,来自服务端的回答! 系统时间: " + LocalDateTime.now()));
                break;
            }
        }

        //ChannelInboundHandler之间的传递，通过调用  ctx.fireChannelRead 实现
        //ctx.fireChannelRead(msg);
    }

    /**
     * 当 ChannelHandler 添加到 ChannelPipeline 调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("-->FirstChatHandler有新的客户端连接服务器, handlerAdded 被调用!客户端ID：【" + ctx.channel().id().asLongText()
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
        log.info("-->FirstChatHandler已建立与远程客户端ID:【" + ctx.channel().id().asShortText() + "】IP：【" + ctx.channel().remoteAddress() + "】的连接。");
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
        log.info("-->FirstChatHandler异常:" + cause.getMessage());
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
        log.info("-->FirstChatHandler与客户端ID：【" + ctx.channel().id().asLongText() + "】IP:【" + ctx.channel().remoteAddress() + "】断开连接 handlerRemoved 被调用!");
        //删除通道
        UserChannelMap.getChannelGroup().remove(ctx.channel());
        UserChannelMap.removeByChannelId(ctx.channel().id().asLongText());
        UserChannelMap.remove("user-" + ctx.channel().id().asShortText());
        UserChannelMap.print();
    }
}