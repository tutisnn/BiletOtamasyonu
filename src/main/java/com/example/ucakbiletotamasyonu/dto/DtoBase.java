package com.example.ucakbiletotamasyonu.dto;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DtoBase {

    private Integer id; // you can change back to long

    private Date createTime;
}

