package com.bethelreport.worker.service;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

	@Mock
	private MinioClient minioClient;

	@Test
	void shouldUploadFileSuccessfully() throws Exception {
		StorageService storageService = new StorageService(minioClient, "test-bucket");
		when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

		storageService.upload("reports/test.pdf", new byte[]{1, 2, 3}, "application/pdf");

		verify(minioClient).putObject(any(PutObjectArgs.class));
	}

	@Test
	void shouldThrowOnUploadFailure() throws Exception {
		StorageService storageService = new StorageService(minioClient, "test-bucket");
		when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
		when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new RuntimeException("Connection failed"));

		assertThatThrownBy(() -> storageService.upload("reports/test.pdf", new byte[]{1}, "application/pdf"))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("Falha ao enviar arquivo para MinIO");
	}
}
