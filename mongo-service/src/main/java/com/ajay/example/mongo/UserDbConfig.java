package com.ajay.example.mongo;

import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.exporters.logging.LoggingSpanExporter;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
public class UserDbConfig {

    // Zipkin API Endpoints for uploading spans
    private static final String ENDPOINT_V2_SPANS = "/api/v2/spans";

    // Name of the service
    private static final String SERVICE_NAME = "mongo-service";

    private OpenTelemetrySdk openTelemetrySdk;

    @Value("${otel-collector_host}")
    private String otelCollectorHost;

    @Value("${otel-collector_port}")
    private String otelCollectorPort;

    @Value("${otel-collector_useSsl}")
    private Boolean otelCollectoruseSsl;

    @Bean
    public OpenTelemetrySdk openTelemetrySdk() {
        // install the W3C Trace Context propagator
        // Get the tracer management instance
        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder().setResource(Resource.create(Attributes.of(AttributeKey.stringKey("service.name"), SERVICE_NAME))).build();
        String httpUrl = String.format("http://%s:%s", "localhost", 9411);

        // Set to process the the spans by the LogExporter

        this.openTelemetrySdk =  OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(JaegerPropagator.getInstance()))
                .build();
        return this.openTelemetrySdk;
    }

    @Bean
    public Tracer tracer(OpenTelemetrySdk openTelemetrySdk){
        //        return null;
        String httpUrl = String.format("http://%s:%s", "localhost", 9411);
        JaegerGrpcSpanExporter jaegerGrpcSpanExporter = JaegerGrpcSpanExporter.builder()
                        .setServiceName(SERVICE_NAME)
                        .setChannel(ManagedChannelBuilder.forAddress("localhost", 49160).usePlaintext().build())
                        .build();
        ZipkinSpanExporter
                        zipkinExporter = ZipkinSpanExporter.builder().setEndpoint(httpUrl + ENDPOINT_V2_SPANS).setServiceName(SERVICE_NAME).build();

        LoggingSpanExporter loggingSpanExporter = new LoggingSpanExporter();
        OtlpGrpcSpanExporter otlpGrpcSpanExporter =  OtlpGrpcSpanExporter.builder().setEndpoint(String.format("%s:%s",otelCollectorHost, otelCollectorPort)).setUseTls(otelCollectoruseSsl).readSystemProperties().build();

        /*SpanProcessor multiSpanProcessor = SpanProcessor.composite(Arrays.asList(SimpleSpanProcessor.builder(jaegerGrpcSpanExporter).build(),
                                                                        SimpleSpanProcessor.builder(zipkinExporter).build(), SimpleSpanProcessor.builder(loggingSpanExporter).build()));*/
        SpanProcessor multiSpanProcessor = SpanProcessor.composite(Arrays.asList(SimpleSpanProcessor.builder(otlpGrpcSpanExporter).build(),
                SimpleSpanProcessor.builder(loggingSpanExporter).build()));


        this.openTelemetrySdk.getTracerManagement().addSpanProcessor(multiSpanProcessor);

        return openTelemetrySdk.getTracer("systemB_microservice1");
    }

    @Bean
    public TextMapPropagator.Getter<HttpServletRequest> textMapPropagatorGetter() {
        return new TextMapPropagator.Getter<HttpServletRequest>() {
            @Override public String get(HttpServletRequest request, String key) {
                return request.getHeader(key);
            }

            @Override public Iterable<String> keys(HttpServletRequest request) {
                List<String> headerKeys = new ArrayList<>();
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        headerKeys.add(headerNames.nextElement());
                    }
                }
                return headerKeys;
            }
        };
    }


}
