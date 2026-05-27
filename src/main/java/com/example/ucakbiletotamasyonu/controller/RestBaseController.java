package com.example.ucakbiletotamasyonu.controller;

public class RestBaseController {

    public <T> RootEntity<T> ok(T payload) {
        return RootEntity.ok(payload);
    }

    public <T> RootEntity<T> error(String errorMessage){
        return RootEntity.error(errorMessage);
    }

    public <T> RootEntity<T> error(int status, String errorMessage){
        return RootEntity.error(status, errorMessage);
    }
}

