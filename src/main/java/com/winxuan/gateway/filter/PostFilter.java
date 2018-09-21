package com.winxuan.gateway.filter;

import io.netty.buffer.ByteBufAllocator;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author leitao.
 * @category
 * @time: 2018/9/14 0014-16:02
 * @version: 1.0
 * @description:
 **/
//@Component
public class PostFilter extends AbstractNameValueGatewayFilterFactory{

    private static final String X_APP_ID_HEADER = "X-app-id";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    public PostFilter(){
        super();
    }

    @Override
    public GatewayFilter apply(Consumer<NameValueConfig> consumer) {
        System.out.println("==========apply1============================");
        return null;
    }

    @Override
    public GatewayFilter apply(NameValueConfig nameValueConfig) {
        System.out.println("=================apply2=====================");
        return (exchange, chain) -> {
            URI uri = exchange.getRequest().getURI();
            System.out.println("+++++++++++++++++++++" + uri);
            URI ex = UriComponentsBuilder.fromUri(uri).build(true).toUri();
            ServerHttpRequest request = exchange.getRequest().mutate().uri(ex).build();
            //判断是否为POST请求
            if ("POST".equalsIgnoreCase(request.getMethodValue())) {
                Flux<DataBuffer> body = request.getBody();
                //缓存读取的request body信息
                String bodyStr = PostParamsUtils.getPostParamsStr(body);
                System.out.println(bodyStr);
                //封装我们的request
                request = PostParamsUtils.getServerHttpRequest(request,bodyStr);
            }
            return chain.filter(exchange.mutate().request(request).build());
        };
    }


    @Override
    public String name() {
        return "PostFilter";
    }
}