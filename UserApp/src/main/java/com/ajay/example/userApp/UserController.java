package com.ajay.example.userApp;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import io.opentelemetry.context.Scope;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private MongoServiceClient mongoServiceClient;

    @Autowired
    private ObjectMapper mapper;

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Tracer tracer;


    @GetMapping
    public ResponseEntity<List<Object>> getAllUsers() throws Exception {
        LOG.info("hitting user-service to find all users");
        Span span = tracer.spanBuilder("validate sales order").startSpan();
        try (Scope scope = span.makeCurrent()){
            span.setAttribute("mongo-client-api","get all");
            span.setAttribute("processing.system","System A");
            try {
                return ResponseEntity.ok(mongoServiceClient.getAllUsers());
            } catch (HttpClientErrorException.NotFound e){
                throw new UserNotFoundException();
            }
        } finally {
            span.end();
        }
    }


    @GetMapping(value = "{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") String id) throws Exception{
        LOG.info("hitting user-service to find all users");
        Span span = tracer.spanBuilder("find user").startSpan();
        try (Scope scope = span.makeCurrent()){
            return ResponseEntity.ok(mongoServiceClient.getUser(id));
        } finally {
            span.end();
        }

    }

    @PostMapping
    public ResponseEntity createUser(@RequestBody User user) throws Exception {
        LOG.info("hitting user-service to create user: " + user);
        Span span = tracer.spanBuilder("create user").startSpan();
        User res;
        try (Scope scope = span.makeCurrent()) {
            res = mongoServiceClient.createUser(user);
        } finally {
            span.end();
        }
        return ResponseEntity.ok(res);
    }

    @PutMapping(value = "{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) throws Exception {
        Span span = tracer.spanBuilder("update user").startSpan();
        User res;
        try (Scope scope = span.makeCurrent()) {
            res = mongoServiceClient.updateUser(user, id);
        } finally {
            span.end();
        }
        return ResponseEntity.ok(res);
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String id) throws Exception {
        Span span = tracer.spanBuilder("delete user").startSpan();
        try (Scope scope = span.makeCurrent()) {
            mongoServiceClient.deleteUser(id);
        } finally {
            span.end();
        }
        return ResponseEntity.ok("deleted successfully");
    }
}
