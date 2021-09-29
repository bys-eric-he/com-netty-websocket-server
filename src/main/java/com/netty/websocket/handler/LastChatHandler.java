package com.netty.websocket.handler;

import com.alibaba.fastjson.JSON;
import com.netty.websocket.channel.UserChannelMap;
import com.netty.websocket.enums.MessageType;
import com.netty.websocket.pojo.MessageDataContent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

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
public class LastChatHandler extends SimpleChannelInboundHandler<String> {
    /**
     * channel注册到一个EventLoop时调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        log.info("-->LastChatHandler注册到一个EventLoop, 客户端ID:【" + incoming.id().asShortText() + "】IP：【" + ctx.channel().remoteAddress() + "】");
    }

    /**
     * 当Channel中有新的事件消息会自动调用
     *
     * @param ctx
     * @param data
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String data) throws Exception {
        // 当接收到数据后会自动调用
        Channel incoming = ctx.channel();
        try {
            MessageDataContent messageDataContent = JSON.parseObject(data, MessageDataContent.class);
            if (messageDataContent != null) {
                if (messageDataContent.getType().equals(MessageType.KEEPALIVE)) {
                    log.info("-->LastChatHandler收到客户端ID:【" + incoming.id().asShortText() + "】IP：【" + ctx.channel().remoteAddress() + "】的心跳包: " + messageDataContent.getMessage());
                    // 返回服务端响应【谁发的发给谁】
                    ctx.channel().writeAndFlush("LastChatHandler服务端应答【SERVER】已收到客户端ID：【" + messageDataContent.getExtend() + "】 IP：【" + ctx.channel().remoteAddress() + "】的心跳包: " + messageDataContent.getMessage());
                } else if (messageDataContent.getType().equals(MessageType.DATA)) {
                    log.info("-->LastChatHandler收到客户端ID:【" + incoming.id().asShortText() + "】的数据包: " + messageDataContent.getMessage());
                    ctx.channel().writeAndFlush("LastChatHandler服务端应答【SERVER】已收到客户端ID：【" + messageDataContent.getExtend() + "】 IP：【" + ctx.channel().remoteAddress() + "】的数据包: " + messageDataContent.getMessage());
                }
            }
            //将传递到ChannelOutboundHandler
            //ctx.write(data);
            //ctx.write()方法执行后，需要调用flush()方法才能令它立即执行
            //ctx.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 变为活跃状态（连接到了远程主机），可以接受和发送数据
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("-->LastChatHandler已建立与远程客户端ID:【" + ctx.channel().id().asShortText() + "】IP：【" + ctx.channel().remoteAddress() + "】的连接。");
        // 添加到channelGroup 通道组
        UserChannelMap.getChannelGroup().add(ctx.channel());
        UserChannelMap.put("user-" + ctx.channel().id().asShortText(), ctx.channel());
        UserChannelMap.print();
        ctx.fireChannelActive();
    }

    /**
     * channel处于非活跃状态，没有连接到远程主机时调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("-->LastChatHandler与远程客户端ID：【" + ctx.channel().id().asShortText() + "】IP：【" + ctx.channel().remoteAddress() + "】断开连接。");
        // 删除通道
        UserChannelMap.getChannelGroup().remove(ctx.channel());
        UserChannelMap.removeByChannelId(ctx.channel().id().asLongText());
        UserChannelMap.remove("user-" + ctx.channel().id().asShortText());
        UserChannelMap.print();
        ctx.fireChannelInactive();
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
        cause.printStackTrace();
        //删除通道
        UserChannelMap.getChannelGroup().remove(ctx.channel());
        UserChannelMap.removeByChannelId(ctx.channel().id().asLongText());
        UserChannelMap.remove("user-" + ctx.channel().id().asShortText());
        UserChannelMap.print();
        ctx.close();
    }
}