package com.netty.websocket.init;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 服务端通道初始化
 */
public abstract class WebsocketInitializer extends ChannelInitializer<SocketChannel> {
    /**
     * 业务处理handler调用链
     *
     * @return
     */
    protected abstract ChannelHandler getFirstChannelHandler();

    /**
     * 业务处理handler调用链
     *
     * @return
     */
    protected abstract ChannelHandler getSecondChannelHandler();

    /**
     * 业务处理handler调用链
     *
     * @return
     */
    protected abstract ChannelHandler getLastChannelHandler();

    /**
     * 心跳处理handler调用链
     *
     * @return
     */
    protected abstract ChannelHandler getHearBeatHandler();

    /**
     * 处理输出数据
     *
     * @return
     */
    protected abstract ChannelHandler getOutboundHandler();

    /**
     * 处理ByteBuf数据
     *
     * @return
     */
    protected abstract ChannelHandler getByteBufChatHandler();

    /**
     * （6）设置子通道也就是SocketChannel的处理器， 其内部是实际业务开发的"主战场"
     *
     * @param ch
     * @throws Exception
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // ChannelPipeline类是ChannelHandler实例对象的链表，用于处理或截获通道的接收和发送数据。
        // 对于每个新的通道Channel，都会创建一个新的ChannelPipeline，并将器pipeline附加到channel中
        // ChannelPipeline实际上应该叫做ChannelHandlerPipeline，可以把ChannelPipeline看成是一个ChandlerHandler的链表
        // 当需要对Channel进行某种处理的时候，Pipeline负责依次调用每一个Handler进行处理。
        ChannelPipeline pipeline = ch.pipeline();


        /*如果客户端使用  ChannelFuture future = bootstrap.connect("127.0.0.1", 2222); 方式连接到服务器，则需要屏蔽以下内容
          以下方式是用于WebSocket方式, 通过 ws://127.0.0.1:2222/websocket 连接到服务器

        // websocket基于http协议，需要有http的编解码器
        pipeline.addLast(new HttpServerCodec());
        // 对写大数据流的支持
        pipeline.addLast(new ChunkedWriteHandler());
        // 添加对HTTP请求和响应的聚合器:只要使用Netty进行Http编程都需要使用,设置单次请求的文件的大小
        pipeline.addLast(new HttpObjectAggregator(1024 * 64));
        //webSocket 服务器处理的协议，用于指定给客户端连接访问的路由 : ws://127.0.0.1:2222/websocket
        pipeline.addLast(new WebSocketServerProtocolHandler("/websocket"));

        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, -4, 4));
        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
        pipeline.addLast("decoder", new StringDecoder());
        pipeline.addLast("encoder", new StringEncoder());

        */

        // 添加Netty空闲超时检查的支持,超过一定的时间会发送对应的事件消息
        // 1. 读空闲超时
        // 2. 写空闲超时
        // 3. 读写空闲超时
        pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
        // addFirst添加ChannelHandler在ChannelPipeline的第一个位置
        pipeline.addFirst("secondChatHandler", getSecondChannelHandler());
        pipeline.addLast("firstHandler", getFirstChannelHandler());
        pipeline.addLast("byteBufChatHandler", getByteBufChatHandler());
        // 添加心跳处理
        pipeline.addLast("hearBeatHandler", getHearBeatHandler());
        //ChannelOutboundHandler 在注册的时候需要放在最后一个ChannelInboundHandler之前，否则将无法传递到ChannelOutboundHandler。
        pipeline.addLast("outboundHandler", getOutboundHandler());
        // addLast在ChannelPipeline的末尾添加ChannelHandler
        pipeline.addLast("lastHandler", getLastChannelHandler());

    }
}