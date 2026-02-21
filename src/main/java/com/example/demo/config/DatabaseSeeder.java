package com.example.demo.config;

import com.example.demo.entity.RoleEntity;
import com.example.demo.entity.UserEntity;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {

        // SỬA: Dùng saveAndFlush thay vì save
        RoleEntity adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    RoleEntity role = new RoleEntity();
                    role.setName("ADMIN");
                    role.setIsActive(true);
                    log.info("Đã tạo mới Role: ADMIN");
                    return roleRepository.saveAndFlush(role);
                });

        // SỬA: Dùng saveAndFlush thay vì save
        roleRepository.findByName("USER")
                .orElseGet(() -> {
                    RoleEntity role = new RoleEntity();
                    role.setName("USER");
                    role.setIsActive(true);
                    log.info("Đã tạo mới Role: USER");
                    return roleRepository.saveAndFlush(role);
                });

        // Lấy lại Role ADMIN (bước này adminRole ở trên đã có thể dùng trực tiếp luôn,
        // nhưng query lại cũng không sao vì đã được flush xuống DB)
        RoleEntity roleEntity = roleRepository.findByName("ADMIN").orElseThrow();
        boolean existsAdminUser = userRepository.existsByRoles(Set.of(roleEntity));

        if (!existsAdminUser) {
            UserEntity adminUser = new UserEntity();
            adminUser.setEmail("admin@bqmusic.com");
            adminUser.setName("Administrator");
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
            adminUser.setPassword(encoder.encode("admin"));
            adminUser.setIsActive(true);

            adminUser.getRoles().add(adminRole);

            // Ghi User và User_Role xuống DB
            userRepository.saveAndFlush(adminUser);
            log.info("Đã tạo mới User Admin mặc định");
        } else {
            log.info("Đã tồn tại User có Role ADMIN → Không cần tạo nữa");
        }

        log.info("Hoàn tất kiểm tra dữ liệu khởi tạo!");
    }
}