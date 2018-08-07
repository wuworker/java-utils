package com.wxl.utils.http;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by wuxingle on 2017/7/14 0014.
 * http响应
 */
@Setter
@Getter
public class HttpResponsed {

    //状态
    private String statusLine = "";

    //状态码
    private int statusCode;

    //响应体
    private byte[] body;

    //响应头
    private HttpHeaders headers = new HttpHeaders();

    public HttpResponsed() {
    }

    public HttpResponsed addHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }

    public HttpResponsed addHeader(String name, List<String> values) {
        for (String v : values) {
            headers.add(name, v);
        }
        return this;
    }

    public String getStringBody(String code) {
        if (!StringUtils.isEmpty(body)) {
            return new String(body, Charset.forName(code));
        }
        return null;
    }

}

