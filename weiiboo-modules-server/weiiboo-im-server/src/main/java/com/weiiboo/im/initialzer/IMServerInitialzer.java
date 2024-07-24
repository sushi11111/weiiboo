package com.weiiboo.im.initialzer;

import com.weiiboo.im.handler.IMServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class IMServerInitialzer extends ChannelInitializer<SocketChannel> {
    @Resource
    private IMServerHandler imServerHandler;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new HttpServerCodec())
                .addLast(new ChunkedWriteHandler())
                .addLast(new HttpObjectAggregator(1024*64))
                .addLast(new WebSocketServerProtocolHandler("/hhh/weiiboo"))
                .addLast(new IdleStateHandler(5, 0, 0))
                .addLast(imServerHandler);
    }
}
