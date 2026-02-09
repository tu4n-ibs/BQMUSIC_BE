package com.example.demo.controller;

import com.example.demo.model.ApiResponse;
import com.example.demo.model.GenreModel;
import com.example.demo.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/genre")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;
    @PostMapping
    public ApiResponse<Void> create(@RequestBody GenreModel genreModel) {
        genreService.create(genreModel);
        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Genre created successfully")
                .build();
    }

    // GET ALL
    @GetMapping
    public ApiResponse<List<GenreModel>> getAll() {
        return ApiResponse.<List<GenreModel>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Get all genres successfully")
                .data(genreService.getAll())
                .build();
    }

    // GET BY NAME
    @GetMapping("/{name}")
    public ApiResponse<GenreModel> getByName(@PathVariable String name) {
        return ApiResponse.<GenreModel>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Get genre successfully")
                .data(genreService.get(name))
                .build();
    }

    // UPDATE
    @PutMapping("/{id}")
    public ApiResponse<Void> update(
            @PathVariable String id,
            @RequestBody GenreModel genreModel
    ) {
        genreService.update(id, genreModel);
        return ApiResponse.<Void>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Genre updated successfully")
                .build();
    }
}

