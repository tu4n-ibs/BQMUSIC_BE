package com.example.demo.controller;
import com.example.demo.entity.RoleEntity;
import com.example.demo.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/role")
public class RoleController {
    private final RoleService roleService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public void save(@RequestBody RoleEntity role) {
        roleService.save(role);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<RoleEntity> findAll(@ParameterObject Pageable pageable) {
        return roleService.findAll(pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("{id}")
    public RoleEntity findById(@PathVariable String id) {
        return roleService.findById(id);
    }
}