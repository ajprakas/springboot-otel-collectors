# start mongo db container and find ip address of this container using --> docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' <container_id>

# start springboot mongo container using command
docker run --name mongo-service -p 8102:8080 ajprakash/opentelemetry-mongo-service:1.0 -it --spring.data.mongodb.database="user_db" --spring.data.mongodb.port="27017" --spring.data.mongodb.host=<172.17.0.3<mongodb container port>> --otel-collector_host="10.196.173.72" --otel-collector_port="4317" --otel-collector_useSsl=false
