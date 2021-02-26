package com.ajay.example.userApp;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpUtils {

    @Autowired
    private OpenTelemetrySdk openTelemetrySdk;

    @Autowired
    private RestTemplate restTemplate;

    private TextMapPropagator.Setter<HttpHeaders> setter = new TextMapPropagator.Setter<HttpHeaders>() {
        @Override
        public void set(HttpHeaders headers, String key, String value) {
            headers.set(key, value);
        }
    };

    private TextMapPropagator textFormat;

//    private TextMapPropagator jaegerPropagator;

    public HttpUtils(OpenTelemetrySdk openTelemetrySdk) {
        textFormat = openTelemetrySdk.getPropagators().getTextMapPropagator();
    }

    public <T> T get(String url, Class T){
        HttpHeaders headers = new HttpHeaders();

//        textFormat.inject(Context.current(), headers, setter);

        textFormat.inject(Context.current(), headers, setter);
        HttpEntity<T> entity = new HttpEntity<T>(headers);

        ResponseEntity<T> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, T);

        return response.getBody();
    }

}
