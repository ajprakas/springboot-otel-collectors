package com.ajay.example.mongo;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RootFilter implements Filter {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private OpenTelemetrySdk openTelemetrySdk;
    @Autowired
    private TextMapPropagator.Getter<HttpServletRequest> textMapPropagatorGetter;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)servletRequest;
        HttpServletResponse  myResponse= (HttpServletResponse) servletResponse;
        LOG.info("URL called :%s", httpRequest);
        Context extractedContext = openTelemetrySdk.getPropagators().getTextMapPropagator().extract(Context.current(), httpRequest, textMapPropagatorGetter);
        filterChain.doFilter(httpRequest, myResponse);
    }
}
