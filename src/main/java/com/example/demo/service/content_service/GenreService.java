package com.example.demo.service.content_service;

import com.example.demo.common.AppException;
import com.example.demo.entity.GenreEntity;
import com.example.demo.model.content_dto.GenreModel;
import com.example.demo.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;
    public void create (GenreModel genreModel) {
        genreRepository.save(new GenreEntity(genreModel.getName(),genreModel.getDescription()));
    }
    public List<GenreModel> getAll() {
        return genreRepository.findAll().stream().map(genreEntity -> new GenreModel(genreEntity.getName(),genreEntity.getDescription(),genreEntity.getId()))
                .toList();
    }
    public GenreModel get(String genreName) {
        GenreEntity genreEntity = genreRepository.findByName(genreName);
        return new GenreModel(genreEntity.getName(),genreEntity.getDescription(),genreEntity.getId());
    }
    public void update(String id,GenreModel genreModel) {
        GenreEntity genreEntity = genreRepository.findById(id).orElseThrow(()->new AppException(HttpStatus.NOT_FOUND, "GENER_NF_001", "Cannot find this genre"));
        genreEntity.setDescription(genreModel.getDescription());
        genreEntity.setName(genreModel.getName());
        genreRepository.save(genreEntity);
    }
}
