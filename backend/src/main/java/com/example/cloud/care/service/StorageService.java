package com.example.cloud.care.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * File storage abstraction.
 *
 * <p>Uploads used to go straight to Cloudinary from every controller. That
 * meant eight copies of the same upload call and no way to point a local build
 * at a different provider. Everything now goes through this interface so the
 * backing store is a configuration concern, not a controller concern.
 */
public interface StorageService {

    /**
     * Upload a multipart file and return a browser-usable URL.
     *
     * @param file   the uploaded part
     * @param folder logical folder inside the bucket (e.g. {@code patients})
     */
    String upload(MultipartFile file, String folder) throws IOException;

    /**
     * Upload raw bytes with an explicit content type.
     */
    String upload(byte[] bytes, String contentType, String folder) throws IOException;

    /**
     * Upload raw bytes with a filename hint, used when the extension matters
     * for downloads (certificates, reports).
     */
    String upload(byte[] bytes, String originalFilename, String contentType, String folder)
            throws IOException;

    /**
     * Decode a data URL ({@code data:image/png;base64,...}) and upload it.
     */
    String uploadBase64(String dataUrl, String folder) throws IOException;
}
