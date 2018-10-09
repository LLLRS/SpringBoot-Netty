package me.netty.nettyServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class WSServerInitialzer  extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        // websocket 基于http协议，所以要有http编解码器
        pipeline.addLast(new HttpServerCodec());
        // 对写大数据流的支持
        pipeline.addLast(new ChunkedWriteHandler());

        // 对httpMessage进行聚合
        pipeline.addLast(new HttpObjectAggregator(1024*64));


        // websocket 服务器处理的协议，用于指定给客户端连接访问的路由 : /ws
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        // Netty提供的触发自定义空闲状态检测的handler
        // 设置读写空闲20秒 触发事件  关闭无用的channel
        pipeline.addLast(new IdleStateHandler(20, 20, 20));

        //自定义的handler
        pipeline.addLast(new ChatHandler());
        // 自定义的空闲状态检测handler
        pipeline.addLast(new HeartBeatHandler());

    }
}
