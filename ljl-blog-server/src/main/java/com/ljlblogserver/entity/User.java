package com.ljlblogserver.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class User {

    private Long id;
    private String username;
    private String password;
    private LocalDateTime createdAt;
}
