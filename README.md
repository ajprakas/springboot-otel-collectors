# springboot-otel-collectors
This Springboot app contains two microservices user-service and mongo-service. These services are using opentelemetry to send tracing data to jaeger and zipkin by using OtlpGrpcSpanExporter.
See Tracer bean in Userconfig and UserDbConfig.
