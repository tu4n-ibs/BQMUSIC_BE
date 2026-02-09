package com.example.demo.model;

import com.example.demo.entity.RoleEntity;
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
    private String name;
    private String email;
    private String imageUrl;
    private Set<String> roles = new HashSet<>();
}
