package com.ajay.example.userApp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.attributes.SemanticAttributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MongoServiceClient {

    @Value("${db.host}")
    private String dbHost;

    @Value("${db.port}")
    private String dbPort;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    private String baseUrl;

    @Autowired
    private Tracer tracer;

    @Autowired
    private HttpUtils httpUtils;

   /*@Autowired
    private OpenTelemetry openTelemetry;*/

    public MongoServiceClient() {
        this.baseUrl = "http://"+dbHost+":"+dbPort+"/users";
    }

    public User getUser(String id){
        String baseUrl = "http://"+dbHost+":"+dbPort+"/users";
        String url = baseUrl+"/"+id;
        return restTemplate.getForObject(url, User.class);
    }

    public List<Object> getAllUsers() throws IOException {
        String baseUrl = "http://"+dbHost+":"+dbPort+"/users";
        Span clientSpan = tracer.spanBuilder("find cost").setSpanKind(Span.Kind.CLIENT).startSpan();
        try(Scope scope = clientSpan.makeCurrent()) {
            clientSpan.getSpanContext().getTraceState();

//            String baseUrl = "http://" + dbHost + ":" + dbPort + "/users";
            clientSpan.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
            clientSpan.setAttribute(SemanticAttributes.HTTP_URL, baseUrl);
            clientSpan.setAttribute("processing.system","System A");
            URL url = new URL(baseUrl);

            return httpUtils.get(baseUrl, List.class);
        } finally {
            clientSpan.end();
        }

    }

    public String createUser(User user) throws JsonProcessingException {
        String baseUrl = "http://"+dbHost+":"+dbPort+"/users";
       String response = restTemplate.postForObject(baseUrl, user, String.class);
       return "created "+ response;
    }

    public User updateUser(User user, String id) throws URISyntaxException {
        String baseUrl = "http://"+dbHost+":"+dbPort+"/users"+"/"+id;
        try {
            restTemplate.put(new URI(baseUrl), user);
        }catch(Exception e){
            return null;
        }

        return user;

    }

    public void deleteUser(String id){
        String baseUrl = "http://"+dbHost+":"+dbPort+"/users"+"/"+id;
        restTemplate.delete(baseUrl);
    }



}
