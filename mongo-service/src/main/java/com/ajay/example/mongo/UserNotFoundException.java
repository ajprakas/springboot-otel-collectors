package com.ajay.example.mongo;

public class UserNotFoundException extends  RuntimeException {
    public UserNotFoundException() {
        super("Users not found Exception");
    }
}
