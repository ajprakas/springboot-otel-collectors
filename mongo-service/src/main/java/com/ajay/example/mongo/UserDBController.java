package com.ajay.example.mongo;


import com.sun.net.httpserver.HttpExchange;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
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
import javax.swing.text.html.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.query.Query;
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
    public List<User> getAllUsers(HttpServletRequest request) throws Exception {
       Context extractedContext = openTelemetrySdk.getPropagators().getTextMapPropagator().extract(Context.current(), request, textMapPropagatorGetter);
        Span serverSpan = null;
        try(Scope scope = extractedContext.makeCurrent()){
            LOG.info("Finding all users from mongo-service");
            serverSpan = tracer.spanBuilder("generate invoice").setSpanKind(Span.Kind.SERVER).startSpan();
            serverSpan.setAttribute(SemanticAttributes.HTTP_METHOD, "GET");
            serverSpan.setAttribute(SemanticAttributes.HTTP_HOST, request.getRemoteHost());
            serverSpan.setAttribute("processing.system","System B");
            try {
                return getAllUsers();
            }  catch (Exception e){
                serverSpan.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
                serverSpan.addEvent("Exception happened while getting all users");
                serverSpan.setStatus(StatusCode.ERROR);
                throw e;
            }
        } finally {
            serverSpan.end();
        }
    }

    public List<User> getAllUsers() throws Exception {
        Span span = tracer.spanBuilder("confirm order").startSpan();
        try (Scope scope = span.makeCurrent()){
            span.setAttribute("processing.system","System B");
            span.setAttribute("email notification sent", true);
            return userRepo.findAll();
        } finally {
            span.end();
        }
    }

    @GetMapping(value = "{id}")
    public ResponseEntity getUser(@PathVariable String id, HttpServletRequest request) {
        Context extractedContext = openTelemetrySdk.getPropagators().getTextMapPropagator().extract(Context.current(), request, textMapPropagatorGetter);
        Span span =  null;
        try (Scope scope = extractedContext.makeCurrent()){
            span = tracer.spanBuilder("get user").setSpanKind(Span.Kind.SERVER).startSpan();
            try {
                Optional<User> entity =  userRepo.findById(id);
                if(entity.isPresent()){
                    return ResponseEntity.ok(entity.get());
                } else{
                    span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.NOT_FOUND.value());
                    span.addEvent("User with id: "+id+"not found");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
            } catch (Exception e){
                span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
                span.addEvent("Exception happened while getting user with id: "+id);
                span.setStatus(StatusCode.ERROR);
                throw e;
            }
        } finally {
            span.end();
        }
    }


    @PostMapping
    public ResponseEntity addUser(@RequestBody User user, HttpServletRequest request) throws Exception {
        Context extractedContext = openTelemetrySdk.getPropagators().getTextMapPropagator().extract(Context.current(), request, textMapPropagatorGetter);
        Span span =  null;
        try (Scope scope = extractedContext.makeCurrent()){
            span = tracer.spanBuilder("add user").setSpanKind(Span.Kind.SERVER).startSpan();
            User user1;
            try {
                List<User> users = userRepo.findByName(user.getName());
                if(users.size()!=0){
                    throw new Exception("User with name: "+user.getName()+" already exits");
                }
                user1 = userRepo.save(user);
            } catch (Exception e){
                span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
                span.addEvent("Exception happened while adding user: "+user);
                span.setStatus(StatusCode.ERROR);
                throw e;
            }
            return ResponseEntity.ok(user1);
        } finally {
            span.end();
        }

    }

    @PutMapping(value = "{id}")
    public ResponseEntity updateUser(@PathVariable String id, @RequestBody User userReq, HttpServletRequest request) throws Exception {
        Context extractedContext = openTelemetrySdk.getPropagators().getTextMapPropagator().extract(Context.current(), request, textMapPropagatorGetter);
        Optional<User> entity = userRepo.findById(id);
        Span span = null;
        if(entity.isPresent()){
            User user = null;
            try (Scope scope = extractedContext.makeCurrent()){
                span = tracer.spanBuilder("update user").setSpanKind(Span.Kind.SERVER).startSpan();
                    List<User> usersList = userRepo.findByName(userReq.getName());
                    if(usersList.size()!=0){
                        throw new Exception("User with name "+userReq.getName()+" already present");
                    }
                    Optional<User> user1 = userRepo.findById(id);
                    if(!user1.isPresent()){
                       throw new UserNotFoundException();
                    }
                    userReq.setUserId(id);
                    user = userRepo.save(userReq);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(user);
            } catch (UserNotFoundException e){
                span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.NOT_FOUND.value());
                span.addEvent("Not Found user with id :"+id);
                throw e;
            }
            catch (Exception e){
                span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
                span.addEvent("Exception happened while updating user with error :"+e.getMessage());
                span.setStatus(StatusCode.ERROR);
                throw e;
            } finally {
                span.end();
            }
        }
        else{
            try (Scope scope = extractedContext.makeCurrent()) {
                span = tracer.spanBuilder("update user").setSpanKind(Span.Kind.SERVER).startSpan();
                span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.NOT_FOUND.value());
                span.addEvent("User with id: " + id + " not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with given id not present");
            } finally {
                span.end();
            }
        }
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity deleteUser(@PathVariable String id, HttpServletRequest request) throws Exception{
        Context extractedContext = openTelemetrySdk.getPropagators().getTextMapPropagator().extract(Context.current(), request, textMapPropagatorGetter);
        Span span = null;
        try (Scope scope = extractedContext.makeCurrent()) {
            span = tracer.spanBuilder("delete user").setSpanKind(Span.Kind.SERVER).startSpan();
            try {
                Optional<User> user = userRepo.findById(id);
                if (!user.isPresent()) {
                    span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, HttpStatus.NOT_FOUND.value());
                    span.addEvent("user with id:" + id + " not found");
                    throw new UserNotFoundException();
                }
                userRepo.deleteById(id);
            }  finally {
                span.end();
            }
        }
        return ResponseEntity.ok("Deleted user");
    }
}
