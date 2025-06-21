package com.ecommerce.project.service;

import com.ecommerce.project.interfaces.FileInterface;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService implements FileInterface {


    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        // File name of current/original file

        String originalFileName = file.getOriginalFilename();

        // Generate unique file name

        String randomId = UUID.randomUUID().toString();
        String fileName = randomId + originalFileName.substring(originalFileName.lastIndexOf("."));
        Path filePath = Paths.get(path + fileName);

        // check if path exist and create
        File folder = new File(path);
        if (!folder.exists()){
            folder.mkdirs();
        }

        System.out.println("Saving file to" + filePath.toAbsolutePath().toString());
        // Upload to server
        Files.copy(file.getInputStream(), filePath);

        // return file name
        return fileName;
    }
}
