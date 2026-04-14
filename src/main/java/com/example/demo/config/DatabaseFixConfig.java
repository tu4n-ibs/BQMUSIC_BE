package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseFixConfig {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Tự động sửa lỗi check constraint của PostgreSQL khi Enum TargetNotiType được mở rộng.
     * Hibernate's ddl-auto=update thường không tự cập nhật được Check Constraint của Enum.
     */
    @PostConstruct
    public void fixNotificationConstraints() {
        log.info(">>> Starting Database Constraint Fix for 'notification' table...");
        try {
            // 1. Xóa ràng buộc cũ (nếu tồn tại) vốn đang giới hạn target_type từ 0-3
            jdbcTemplate.execute("ALTER TABLE notification DROP CONSTRAINT IF EXISTS notification_target_type_check");
            
            // 2. Thêm lại ràng buộc mới cho phép giá trị từ 0 đến 4 (4 tương ứng với USER)
            jdbcTemplate.execute("ALTER TABLE notification ADD CONSTRAINT notification_target_type_check CHECK (target_type >= 0 AND target_type <= 4)");
            
            log.info(">>> Database notification constraint fixed successfully (TargetNotiType now supports USER - index 4).");
        } catch (Exception e) {
            log.warn(">>> Database notification constraint fix encountered an issue: {}. This is usually safe to ignore if the constraint is already updated.", e.getMessage());
        }
    }
}
