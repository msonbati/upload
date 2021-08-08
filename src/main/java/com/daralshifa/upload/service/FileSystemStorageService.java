package com.daralshifa.upload.service;

import com.daralshifa.upload.config.StorageProperties;
import com.daralshifa.upload.exceptions.StorageException;
import com.daralshifa.upload.exceptions.StorageFileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path signatures;
    private final Path invoices;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.signatures = Paths.get(properties.getSignatures());
        this.invoices = Paths.get(properties.getInvoices());
    }

    @Override
    public void store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            Path destinationFile = this.signatures.resolve(
                    Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.signatures.toAbsolutePath())) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    public void store(String fileName,byte[] decodedByte) {

        Path destinationFile = this.signatures.resolve(
                Paths.get(fileName))
                .normalize().toAbsolutePath();
        try {
            FileOutputStream fos = new FileOutputStream(destinationFile.toString());
            fos.write(decodedByte);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //   fos.write(decodedByte);
        //   fos.close();

    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.invoices, 1)
                    .filter(path -> !path.equals(this.invoices))
                    .map(this.invoices::relativize);
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return invoices.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(signatures.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(signatures);
            Files.createDirectories(invoices);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}

