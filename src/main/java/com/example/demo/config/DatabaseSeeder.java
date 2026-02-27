package com.example.demo.config;

import com.example.demo.entity.RoleEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TransactionTemplate transactionTemplate;

    @Value("${admin.password.value}")
    private  String adminPassword;
    @Override
    public void run(String... args) {
        transactionTemplate.execute(status -> {
            createRoleIfNotFound("ADMIN");
            createRoleIfNotFound("USER");
            return null;
        });

        transactionTemplate.execute(status -> {
            RoleEntity adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Error: Role ADMIN not found."));

            if (!userRepository.existsByEmail("admin@bqmusic.com")) {
                UserEntity adminUser = new UserEntity();
                adminUser.setEmail("admin@bqmusic.com");
                adminUser.setName("Administrator");
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
                adminUser.setPassword(encoder.encode(adminPassword));
                adminUser.setIsActive(true);

                // Save user first without roles
                adminUser = userRepository.saveAndFlush(adminUser);

                // Now add roles and save again
                adminUser.getRoles().add(adminRole);
                userRepository.saveAndFlush(adminUser);
                log.info("Đã tạo mới User Admin mặc định");
            } else {
                log.info("Đã tồn tại User admin@bqmusic.com");
            }
            return null;
        });

        log.info("Hoàn tất kiểm tra dữ liệu khởi tạo!");
    }

    private void createRoleIfNotFound(String roleName) {
        roleRepository.findByName(roleName).orElseGet(() -> {
            RoleEntity role = new RoleEntity();
            role.setName(roleName);
            role.setIsActive(true);
            log.info("Đã tạo mới Role: {}", roleName);
            return roleRepository.saveAndFlush(role);
        });
    }
}