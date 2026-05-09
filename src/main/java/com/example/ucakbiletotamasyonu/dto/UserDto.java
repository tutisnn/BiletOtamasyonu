package com.example.ucakbiletotamasyonu.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto extends DtoBase {

    private String email;
    private Boolean enabled;
}

