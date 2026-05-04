package com.example.ucakbiletotamasyonu.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DtoUser extends DtoBase {

    private String email;
    private Boolean emailVerified;
}

