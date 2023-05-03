package com.example.demo;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@SpringBootApplication
public class FileStreamingApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileStreamingApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
class FileController {

    private static final String FILE_URL = "https://repo1.maven.org/maven2/org/springframework/spring-core/6.0.8/spring-core-6.0.8.jar";

    private final RestTemplate restTemplate;

    @GetMapping("/file")
    public void file(HttpServletResponse response) {

        restTemplate.execute(FILE_URL, HttpMethod.GET, null, fileSourceResponse -> {

            //Using setHeader as setContentLength takes integer instead of long
            response.setHeader(HttpHeaders.CONTENT_LENGTH,
                    String.valueOf(fileSourceResponse.getHeaders().getContentLength()));

            //Whether file should be downloaded or rendered
            String contentDisposition = fileSourceResponse.getHeaders().getContentDisposition().toString();

            if (StringUtils.hasLength(contentDisposition)) {
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
            } else {
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file.jar\"");
            }

            response.setContentType(fileSourceResponse.getHeaders().getContentType().toString());

            //Add other headers as needed

            InputStream input = fileSourceResponse.getBody();
            ServletOutputStream output = response.getOutputStream();

            copyInputStreamToOutputStream(input, output, 4096);

            return null;
        });
    }

    void copyInputStreamToOutputStream(InputStream input, ServletOutputStream output, int bufferSize)
            throws IOException {

        byte[] buffer = new byte[bufferSize];

        int readLength = -1;

        while ((readLength = input.read(buffer)) != -1) {
            output.write(buffer, 0, readLength);
        }
    }
}
