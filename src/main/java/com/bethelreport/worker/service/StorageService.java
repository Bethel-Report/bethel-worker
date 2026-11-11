package com.bethelreport.worker.service;

import java.io.ByteArrayInputStream;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.SignatureDeclareThrowsException"})
public class StorageService {

	private final MinioClient minioClient;
	private final String bucketName;

	public StorageService(MinioClient minioClient, @Value("${minio.bucket}") String bucketName) {
		this.minioClient = minioClient;
		this.bucketName = bucketName;
	}

	public void upload(String key, byte[] content, String contentType) {
		try {
			ensureBucketExists();
			try (ByteArrayInputStream stream = new ByteArrayInputStream(content)) {
				minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(key)
						.stream(stream, content.length, -1).contentType(contentType).build());
			}
			log.info("Arquivo enviado para MinIO: {}", key);
		} catch (Exception ex) {
			throw new IllegalStateException("Falha ao enviar arquivo para MinIO: " + key, ex);
		}
	}

	private void ensureBucketExists() throws Exception {
		boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
		if (!exists) {
			minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
		}
	}
}
