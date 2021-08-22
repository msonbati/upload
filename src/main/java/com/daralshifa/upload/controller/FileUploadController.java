package com.daralshifa.upload.controller;

import com.daralshifa.upload.exceptions.StorageFileNotFoundException;
import com.daralshifa.upload.service.FileSystemStorageService;
import com.daralshifa.upload.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@RestController
@RequestMapping("/files")
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadUnsigned(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename,StorageService.TYPE.UNSIGNED);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }
    @GetMapping("/downloadsigned/{filename:.+}")
    public ResponseEntity<Resource> downloadSigned(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename,StorageService.TYPE.SIGNED);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        storageService.store(file, storageService.getSignaturesPath());
        return "ok";
    }
    @GetMapping("/merge/{invoiceNumber}")
    public String merge(@PathVariable String invoiceNumber) {
        try {
            storageService.merge(invoiceNumber);
        } catch (Exception e) {
            e.printStackTrace();
            return  "NO";
        }
        return "ok";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}