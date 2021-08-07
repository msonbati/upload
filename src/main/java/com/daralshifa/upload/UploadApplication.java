package com.daralshifa.upload;

import com.daralshifa.upload.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class UploadApplication {

	public static void main(String[] args) {
		SpringApplication.run(UploadApplication.class, args);
	}

}
