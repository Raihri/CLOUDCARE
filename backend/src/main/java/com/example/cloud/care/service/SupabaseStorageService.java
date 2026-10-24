package com.example.cloud.care.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Supabase Storage implementation.
 *
 * <p>Uses the Storage REST API directly (no SDK dependency). The service-role
 * key is sent as both {@code apikey} and a bearer token, which is what the
 * Supabase gateway expects for server-side writes. RLS is bypassed by the
 * service role, so the bucket itself only needs to be public for the returned
 * URLs to render in the browser.
 *
 * <p>Configuration is intentionally lazy: the application starts without
 * Supabase credentials so the rest of the demo works, and an upload fails with
 * a clear message until the env vars are filled in.
 */
@Service
public class SupabaseStorageService implements StorageService {

    private static final Pattern DATA_URL =
            Pattern.compile("^data:([^;,]+)(?:;[^,]*)?;base64,(.*)$", Pattern.DOTALL);

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key:}")
    private String serviceRoleKey;

    @Value("${supabase.storage-bucket:cloudcare}")
    private String bucket;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public String upload(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("No file was supplied.");
        }
        return upload(file.getBytes(), file.getOriginalFilename(), file.getContentType(), folder);
    }

    @Override
    public String upload(byte[] bytes, String contentType, String folder) throws IOException {
        String safeType = (contentType == null || contentType.isBlank())
                ? "application/octet-stream"
                : contentType;
        return upload(bytes, "upload." + extensionFor(safeType), safeType, folder);
    }

    @Override
    public String upload(byte[] bytes, String originalFilename, String contentType, String folder)
            throws IOException {
        if (bytes == null || bytes.length == 0) {
            throw new IOException("The file is empty.");
        }

        String base = configuredBaseUrl();
        String objectPath = safeSegment(folder, "uploads") + "/" + objectName(originalFilename, contentType);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(base + "/storage/v1/object/" + bucket + "/" + objectPath))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("apikey", serviceRoleKey)
                .header("Content-Type", contentType == null || contentType.isBlank()
                        ? "application/octet-stream"
                        : contentType)
                .header("x-upsert", "true")
                .POST(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();

        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Supabase upload was interrupted.", e);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String detail = response.body() == null ? "" : response.body();
            if (detail.length() > 300) {
                detail = detail.substring(0, 300);
            }
            throw new IOException("Supabase upload failed (" + response.statusCode() + "): " + detail);
        }

        return base + "/storage/v1/object/public/" + bucket + "/" + objectPath;
    }

    @Override
    public String uploadBase64(String dataUrl, String folder) throws IOException {
        if (dataUrl == null || dataUrl.isBlank()) {
            throw new IOException("No image was supplied.");
        }

        String contentType = "image/jpeg";
        String payload = dataUrl;

        Matcher matcher = DATA_URL.matcher(dataUrl);
        if (matcher.matches()) {
            contentType = matcher.group(1);
            payload = matcher.group(2);
        } else if (dataUrl.startsWith("data:")) {
            throw new IOException("The uploaded image is not a supported base64 data URL.");
        }

        byte[] bytes;
        try {
            bytes = Base64.getMimeDecoder().decode(payload);
        } catch (IllegalArgumentException e) {
            throw new IOException("The uploaded image is not valid base64.", e);
        }

        return upload(bytes, contentType, folder);
    }

    /**
     * Validate configuration and return the normalised project URL.
     *
     * <p>Throws {@link IOException} rather than IllegalStateException because
     * every existing upload caller already handles IOException and turns it
     * into a user-facing error.
     */
    private String configuredBaseUrl() throws IOException {
        if (supabaseUrl == null || supabaseUrl.isBlank()) {
            throw new IOException(
                    "Supabase storage is not configured. Set SUPABASE_URL in .env.");
        }
        if (serviceRoleKey == null || serviceRoleKey.isBlank()) {
            throw new IOException(
                    "Supabase storage is not configured. Set SUPABASE_SERVICE_ROLE_KEY in .env.");
        }
        if (bucket == null || bucket.isBlank()) {
            throw new IOException(
                    "Supabase storage is not configured. Set SUPABASE_STORAGE_BUCKET in .env.");
        }
        return supabaseUrl.replaceAll("/+$", "");
    }

    private static String objectName(String originalFilename, String contentType) {
        String extension = extensionFrom(originalFilename);
        if (extension.isEmpty()) {
            extension = extensionFor(contentType == null ? "" : contentType);
        }
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private static String extensionFrom(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        String ext = filename.substring(dot).replaceAll("[^A-Za-z0-9.]", "");
        return ext.length() > 12 ? "" : ext.toLowerCase();
    }

    private static String extensionFor(String contentType) {
        String type = contentType == null ? "" : contentType.toLowerCase();
        return switch (type) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "image/svg+xml" -> ".svg";
            case "application/pdf" -> ".pdf";
            default -> ".bin";
        };
    }

    /** Keep only characters that are safe in a storage object path. */
    private static String safeSegment(String value, String fallback) {
        if (value == null) return fallback;
        String cleaned = value.trim()
                .replaceAll("[^A-Za-z0-9._-]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^[._]+|[._]+$", "");
        return cleaned.isBlank() ? fallback : cleaned;
    }
}
