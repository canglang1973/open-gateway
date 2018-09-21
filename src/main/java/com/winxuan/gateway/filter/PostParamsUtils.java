package com.winxuan.gateway.filter;

import io.netty.buffer.ByteBufAllocator;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author leitao.
 * @category
 * @time: 2018/9/21 0021-9:16
 * @version: 1.0
 * @description:
 **/
public class PostParamsUtils {

    private static final Pattern QUERY_PATTERN = Pattern.compile("([^&=]+)(=?)([^&]+)?");

    public static String getPostParamsStr(Flux<DataBuffer> body) {
        AtomicReference<String> bodyRef = new AtomicReference<>();
        body.subscribe(dataBuffer -> {
            CharBuffer charBuffer = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer());
            DataBufferUtils.release(dataBuffer);
            bodyRef.set(charBuffer.toString());
        });//读取request body到缓存
        //获取request body
        return bodyRef.get();
    }

    public static MultiValueMap<String, String> initQueryParams(String postParamsStr) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap();
        Matcher matcher = QUERY_PATTERN.matcher(postParamsStr);
        while (matcher.find()) {
            String name = decodeQueryParam(matcher.group(1));
            String eq = matcher.group(2);
            String value = matcher.group(3);
            value = value != null ? decodeQueryParam(value) : (StringUtils.hasLength(eq) ? "" : null);
            queryParams.add(name, value);
        }
        return queryParams;
    }

    public static ServerHttpRequest getServerHttpRequest(ServerHttpRequest request, String postParamsStr) {
        DataBuffer bodyDataBuffer = stringBuffer(postParamsStr);
        Flux<DataBuffer> bodyFlux = Flux.just(bodyDataBuffer);

        request = new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                return bodyFlux;
            }
        };
        return request;
    }

    private static String decodeQueryParam(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException var3) {
            return URLDecoder.decode(value);
        }
    }

    private static DataBuffer stringBuffer(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return buffer;
    }
}
