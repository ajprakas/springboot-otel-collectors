package com.ajay.example.userApp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Attributes;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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

    public User getUser(String id) throws Exception {
        String baseUrl = "http://"+dbHost+":"+dbPort+"/users";
        String url = baseUrl+"/"+id;
        Span clientSpan = tracer.spanBuilder("getUser from mongoClient").setSpanKind(Span.Kind.CLIENT).startSpan();
        try(Scope scope = clientSpan.makeCurrent()) {
            clientSpan.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
            clientSpan.setAttribute(SemanticAttributes.HTTP_URL, url);
            try{
                 return httpUtils.get(url, User.class);
            } catch (UserNotFoundException e){
                clientSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.NOT_FOUND.value());
                clientSpan.addEvent("Exception happened while getting user with id: "+id);
                clientSpan.setStatus(StatusCode.ERROR);
                throw e;
            } catch (Exception e){
                clientSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
                clientSpan.addEvent("Exception happened while getting user with id: "+id);
                clientSpan.setStatus(StatusCode.ERROR);
                throw e;
            }
        } finally {
            clientSpan.end();
        }
    }

    public List<Object> getAllUsers() throws Exception {
        String baseUrl = "http://"+dbHost+":"+dbPort+"/users";
        Span clientSpan = tracer.spanBuilder("find cost").setSpanKind(Span.Kind.CLIENT).startSpan();
        try(Scope scope = clientSpan.makeCurrent()) {
            clientSpan.getSpanContext().getTraceState();

//            String baseUrl = "http://" + dbHost + ":" + dbPort + "/users";
            clientSpan.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
            clientSpan.setAttribute(SemanticAttributes.HTTP_URL, baseUrl);
            clientSpan.setAttribute("processing.system","System A");
            URL url = new URL(baseUrl);

            try {
                return httpUtils.get(baseUrl, List.class);
            } catch (Exception e){
                clientSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.NOT_FOUND.value());
                clientSpan.addEvent("Exception happened while getting all users");
                clientSpan.setStatus(StatusCode.ERROR);
                throw e;
            }

        } finally {
            clientSpan.end();
        }

    }

    public User createUser(User user) throws Exception {
        String baseUrl = "http://"+dbHost+":"+dbPort+"/users";
        Span clientSpan = tracer.spanBuilder("create user").setSpanKind(Span.Kind.CLIENT).startSpan();
        User user1 = null;
        try(Scope scope = clientSpan.makeCurrent()) {
            clientSpan.setAttribute(SemanticAttributes.HTTP_METHOD, HttpMethod.POST.name());
            clientSpan.setAttribute(SemanticAttributes.HTTP_URL, baseUrl);
            clientSpan.setAttribute("Payload", user.toString());
            try {
                 user1 = httpUtils.create(baseUrl, user, User.class);
            } catch (Exception e){
                clientSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
                clientSpan.addEvent("Exception happened while adding user: "+user);
                clientSpan.setStatus(StatusCode.ERROR);
                throw e;
            }
        } finally {
            clientSpan.end();
        }
       return user1;
    }

    public User updateUser(User user, String id) throws Exception {
        String baseUrl = "http://"+dbHost+":"+dbPort+"/users"+"/"+id;
        Span clientSpan = tracer.spanBuilder("update user").setSpanKind(Span.Kind.CLIENT).startSpan();
        try(Scope scope = clientSpan.makeCurrent()) {
            clientSpan.setAttribute(SemanticAttributes.HTTP_METHOD, HttpMethod.PUT.name());
            clientSpan.setAttribute(SemanticAttributes.HTTP_URL, baseUrl);
            clientSpan.setAttribute("Payload", user.toString());
            httpUtils.update(baseUrl, user, User.class);
        } catch (UserNotFoundException e){
            clientSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.NOT_FOUND.value());
            clientSpan.addEvent("Not found user with id: " +id);
            clientSpan.setStatus(StatusCode.ERROR);
            throw e;
        }
        catch(Exception e) {
            clientSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
            clientSpan.addEvent("Exception happened while updating user: " + user);
            clientSpan.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            clientSpan.end();
        }
        return user;

    }

    public void deleteUser(String id) throws Exception {
        String baseUrl = "http://"+dbHost+":"+dbPort+"/users"+"/"+id;
        Span clientSpan = tracer.spanBuilder("delete user").setSpanKind(Span.Kind.CLIENT).startSpan();
        try(Scope scope = clientSpan.makeCurrent()) {
            clientSpan.setAttribute(SemanticAttributes.HTTP_METHOD, HttpMethod.DELETE.name());
            clientSpan.setAttribute(SemanticAttributes.HTTP_URL, baseUrl);
            clientSpan.setAttribute("userId", id);
            httpUtils.delete(baseUrl, String.class);
        } catch (UserNotFoundException e) {
            clientSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.NOT_FOUND.value());
            clientSpan.addEvent("not found user with id: " +id+" for deletion");
            clientSpan.setStatus(StatusCode.ERROR);
            throw e;
        }
        catch (Exception e) {
            clientSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
            clientSpan.addEvent("Exception happened while deleting user: " + id);
            clientSpan.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            clientSpan.end();
        }
    }



}
