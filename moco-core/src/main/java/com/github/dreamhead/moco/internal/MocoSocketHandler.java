package com.github.dreamhead.moco.internal;

import com.github.dreamhead.moco.MocoException;
import com.github.dreamhead.moco.Response;
import com.github.dreamhead.moco.SocketRequest;
import com.github.dreamhead.moco.model.DefaultSocketRequest;
import com.github.dreamhead.moco.model.DefaultSocketResponse;
import com.github.dreamhead.moco.model.MessageContent;
import com.github.dreamhead.moco.util.ByteBufs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;
import java.util.Optional;

import static com.github.dreamhead.moco.model.MessageContent.content;
import static io.netty.channel.ChannelHandler.Sharable;
import static java.lang.String.format;

@Sharable
public final class MocoSocketHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private final ActualSocketServer server;

    public MocoSocketHandler(final ActualSocketServer server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final ByteBuf msg) {
        final InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        MessageContent content = content().withContent(new ByteBufInputStream(msg)).build();
        SocketRequest request = new DefaultSocketRequest(content, address.getAddress().getHostAddress());
        SessionContext context = new SessionContext(request, new DefaultSocketResponse());
        Optional<Response> response = server.getResponse(context);
        Response actual = response.orElseThrow(() ->
                new MocoException(format("No handler found for request: %s", context.getRequest().getContent())));
        ctx.write(ByteBufs.toByteBuf(actual.getContent().getContent()));
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        this.server.onException(cause);
    }
}
