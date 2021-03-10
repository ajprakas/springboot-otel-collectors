package com.ajay.example.mongo;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class UserRepoService {
    @Autowired
    UserRepo userRepo;
    @Autowired
    MongoTemplate mongoTemplate;
    /*User findUserByName(String name){
        Query query  = new Query();
        query.addCriteria(Criteria.where("name").is(name));
        List<User> users = mongoTemplate.find(query, User.class);
    }*/
}
