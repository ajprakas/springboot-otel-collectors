package com.ajay.example.userApp;


import lombok.Data;

@Data
public class TaskWrapper {
    private Object mangleTask;
    private String taskType;
    private String taskDescription;
    private String endpointName;
    private String taskStatus;
    private Long lastUpdated;

    public TaskWrapper() {
        this.endpointName = "hello";
    }
}