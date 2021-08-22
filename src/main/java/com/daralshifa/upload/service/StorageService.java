package com.daralshifa.upload.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {
    void init();

    void store(MultipartFile file ,Path path);

    void store(String fileName, byte[] bytes);

    Stream<Path> loadAll();

    void merge(String invoiceId) throws IOException, Exception;
    void mergeAll();

    Path loadUnsignedInvoice(String filename);
    Path loadSignedInvoice(String filename);
    Path loadImageSignature(String filename);

    Resource loadAsResource(String filename,TYPE resourceType);

    enum TYPE  {UNSIGNED,SIGNED,SIGNATURE}

    void deleteAll();

     Path getSignaturesPath();
     Path getSignedPath();
     Path getInvoicesPath();
}
