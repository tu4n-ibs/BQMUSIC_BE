package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChromeDevToolsController {

    @GetMapping("/.well-known/appspecific/com.chrome.devtools.json")
    public ResponseEntity<Void> ignoreChromeDevTools() {
        // Trả về 404 Not Found một cách "lặng lẽ" mà không in ra log lỗi
        return ResponseEntity.notFound().build();
    }
}