package com.winxuan.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author leitao.
 * @category
 * @time: 2018/9/17 0017-13:49
 * @version: 1.0
 * @description:
 **/
@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("=====================AuthorizeFilter==============");
        ServerHttpRequest request = exchange.getRequest();
        //GET 请求可以直接获取入参
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        if (HttpMethod.POST.equals(request.getMethod())){
            Flux<DataBuffer> body = request.getBody();
            String postParamsStr = PostParamsUtils.getPostParamsStr(body);
            queryParams = PostParamsUtils.initQueryParams(postParamsStr);
            request = PostParamsUtils.getServerHttpRequest(request,postParamsStr);
        }
        String token = queryParams.getFirst("accessToken");
        System.out.println("======================"+token);
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
