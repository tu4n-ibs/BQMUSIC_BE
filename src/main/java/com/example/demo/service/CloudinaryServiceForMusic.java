package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.common.AppException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceForMusic {
    private final Cloudinary cloudinary;
    public CloudinaryServiceForMusic(@Qualifier("cloudinaryMusic") Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST,"FILE_001", "File is empty");
        }
        Map<?,?> params = ObjectUtils.asMap(
                "resource_type", "auto",
                "folder", "bqmusic_uploads"
        );
        try {
        Map<?,?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

        return uploadResult.get("secure_url").toString();
        }
        catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR,"FILE_002",e.getMessage());
        }
    }
}
