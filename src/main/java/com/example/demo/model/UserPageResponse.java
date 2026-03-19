package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserPageResponse {
    private String id;
    private String name;
    private String email;
    private String imageUrl;
    private Boolean isActive;
    private Set<String> roles = new HashSet<>();
}
