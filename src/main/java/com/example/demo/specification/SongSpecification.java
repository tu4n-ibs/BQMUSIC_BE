package com.example.demo.specification;
import com.example.demo.entity.SongEntity;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;

public class SongSpecification {

    public static Specification<SongEntity> hasUserId(String userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null || userId.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            Join<Object, Object> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("id"), userId);
        };
    }
}