package com.example.demo.entity;

import com.example.demo.common.BaseEntity;
import com.example.demo.model.enum_object.Provider;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity extends BaseEntity {
    private String name;
    private String password;
    @Enumerated(EnumType.STRING)
    private Provider provider;
    private String providerUserId;
    private String email;
    private String imageUrl;
    private String imageHash;
    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();
}
