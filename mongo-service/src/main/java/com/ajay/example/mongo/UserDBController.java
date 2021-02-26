package com.ajay.example.mongo;


import com.sun.net.httpserver.HttpExchange;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.attributes.SemanticAttributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpRequest;
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

@RestController
@RequestMapping(value = "/users")
public class UserDBController {

    @Autowired
    private UserRepo userRepo;

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    // SDK Tracer Management interface
//    private SdkTracerManagement sdkTracerManagement;

    // The Tracer we'll use for the example
    @Autowired
    private Tracer tracer;

    @Autowired
    private OpenTelemetrySdk openTelemetrySdk;

    @Autowired
    private TextMapPropagator.Getter<HttpServletRequest> textMapPropagatorGetter;

    @GetMapping
    public List<User> getAllUsers(HttpServletRequest request) {
//        return userRepo.findAll();
       Context extractedContext = openTelemetrySdk.getPropagators().getTextMapPropagator().extract(Context.current(), request, textMapPropagatorGetter);
        Span serverSpan = null;
        try(Scope scope = extractedContext.makeCurrent()){
            LOG.info("Finding all users from mongo-service");
            serverSpan = tracer.spanBuilder("generate invoice").setSpanKind(Span.Kind.SERVER).startSpan();
            try{
//                serverSpan.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
//                serverSpan.setAttribute(SemanticAttributes.HTTP_HOST, "localhost:8102");
                serverSpan.setAttribute("processing.system","System B");
            } finally {
                serverSpan.end();
            }
            return getAllUsers();
        } finally {
            serverSpan.end();
        }
    }

    public List<User> getAllUsers(){
        Span span = tracer.spanBuilder("confirm order").startSpan();
        try (Scope scope = span.makeCurrent()){
            span.setAttribute("processing.system","System B");
            span.setAttribute("email notification sent", true);
        } finally {
            span.end();
        }
        return userRepo.findAll();
    }

    @GetMapping(value = "{id}")
    public Optional<User> getUser(@PathVariable String id) {
        return userRepo.findById(id);
    }

    @PostMapping
    public ResponseEntity addUser(@RequestBody User user){
        User user1;
        try {
             user1 = userRepo.save(user);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(user1);
    }

    @PutMapping(value = "{id}")
    public ResponseEntity updateUser(@PathVariable String id, @RequestBody User userReq){
        Optional<User> entity = userRepo.findById(id);
        if(entity.isPresent()){
            entity.get().setName(userReq.getName());
            userRepo.save(entity.get());
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }
        else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with given id not present");
        }
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity deleteUser(@PathVariable String id){
        try {
            userRepo.deleteById(id);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Exception throws");
        }

        return ResponseEntity.ok("Deleted user");
    }
}
