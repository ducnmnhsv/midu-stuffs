package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.ekycadmin.config.AppConf;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final MinioClient minioClient;

    public FileService(AppConf appConf) {
        AppConf.FileStorage.Minio minioConfig = appConf.getFileStorage().getMinio();
        
        this.minioClient = MinioClient.builder()
            .endpoint(minioConfig.getBaseUrl())
            .credentials(minioConfig.getAccessKey(), minioConfig.getPrivateKey())
            .build();
        
        log.info("MinioClient initialized with baseUrl: {}", minioConfig.getBaseUrl());
    }

    public String getPresignedURLForDownloadFile(String bucketName, String key, int durationInSeconds) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(key)
                    .expiry(durationInSeconds, TimeUnit.SECONDS)
                    .build()
            );
            
            log.debug("Generated presigned URL for bucket: {}, key: {}, duration: {}s", 
                bucketName, key, durationInSeconds);
            
            return url;
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for bucket: {}, key: {}", bucketName, key, e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }
}
