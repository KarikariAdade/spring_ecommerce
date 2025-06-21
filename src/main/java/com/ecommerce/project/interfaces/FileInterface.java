package com.ecommerce.project.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileInterface {
    String uploadImage (String path, MultipartFile file) throws IOException;
}
