# springboot-otel-collectors
This Springboot app contains two microservices user-service and mongo-service. These services are using opentelemetry to send tracing data to jaeger and zipkin by using OtlpGrpcSpanExporter.
See Tracer bean in Userconfig and UserDbConfig.


Also yaml files for deploying these microservices in K8S cluster is avaialble.

Each Pod contains 3 containers- User-servcice, Mongo-Service and mongo-db
