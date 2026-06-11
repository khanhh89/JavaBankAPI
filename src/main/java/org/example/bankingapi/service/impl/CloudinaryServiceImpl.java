package org.example.bankingapi.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.example.bankingapi.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        validateFile(file);

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "rikkei-bank/" + folder,
                            "resource_type", "image",
                            "use_filename", true,
                            "unique_filename", true
                    )
            );
            String secureUrl = (String) result.get("secure_url");
            log.info("[CLOUDINARY] File uploaded successfully: {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("[CLOUDINARY] Upload failed: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to cloud storage", e);
        }
    }

    @Override
    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("[CLOUDINARY] File deleted: {}", publicId);
        } catch (IOException e) {
            log.error("[CLOUDINARY] Delete failed: {}", e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: JPEG, PNG, WEBP");
        }
        // Max 5MB check (also enforced at Spring level in application.yml)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }
    }
}
