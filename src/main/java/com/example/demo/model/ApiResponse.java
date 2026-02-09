package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // Bỏ qua các field null khi trả về JSON
public class ApiResponse<T> {
    @Builder.Default
    private boolean success = true;
    private int statusCode;
    private String message;
    private T data;
    private String errorCode;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();


    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(200)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int statusCode, String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .statusCode(statusCode)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
