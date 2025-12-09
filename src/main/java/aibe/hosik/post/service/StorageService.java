package aibe.hosik.post.service;

import aibe.hosik.handler.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    @Value("${supabase.url}")
    private String url;

    @Value("${supabase.access-key}")
    private String accessKey;

    @Value("${supabase.bucket-name}")
    private String bucketName;

    public String upload(MultipartFile file) {
        String uuid = UUID.randomUUID().toString();
        String extension = Optional.ofNullable(file.getContentType())
                .map(ct -> ct.split("/")[1])
                .orElse("jpg");

        String boundary = "Boundary-%s".formatted(uuid);
        String filename = "%s.%s".formatted(uuid, extension);

        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create("%s/storage/v1/object/%s/%s".formatted(url, bucketName, filename)))
                    .header("Authorization", "Bearer " + accessKey)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(ofMimeMultipartData(file, boundary))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Supabase upload error: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return "%s/storage/v1/object/public/%s/%s".formatted(url, bucketName, filename);
    }

    private HttpRequest.BodyPublisher ofMimeMultipartData(MultipartFile file, String boundary) throws IOException {
        return HttpRequest.BodyPublishers.ofByteArrays(List.of(
                ("--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getOriginalFilename() + "\"\r\n" +
                        "Content-Type: " + file.getContentType() + "\r\n\r\n").getBytes(),
                file.getBytes(),
                ("\r\n--" + boundary + "--\r\n").getBytes()
        ));
    }
}

