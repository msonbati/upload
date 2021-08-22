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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {


    private final Path signatures;
    private final Path invoices;
    private final Path signed;

    @Autowired
    private PdfImageMergerService merger;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.signatures = Paths.get(properties.getSignatures());
        this.invoices = Paths.get(properties.getInvoices());
        this.signed = Paths.get(properties.getSigned());
    }
    @Override
    public Path getSignaturesPath(){return signatures;}
    @Override
    public Path getSignedPath(){return signed;}
    @Override
    public Path getInvoicesPath(){return invoices;}

    @Override
    public void store(MultipartFile file,Path path) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            Path destinationFile = path.resolve(
                    Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(path.toAbsolutePath())) {
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
    public void merge(final String invoiceId) throws Exception {
        final Path invoicesPath = getInvoicesPath();
        final Path signedPath = getSignedPath();
       final String  src = invoicesPath.toAbsolutePath()+"/"+invoiceId+".pdf";
       final String  dest = signedPath.toAbsolutePath()+"/"+invoiceId+".pdf";
      //  List<String> iamges= null;
        try (Stream<Path> stream = Files.walk(this.signatures, 1) ){
            List<String>   images =    stream
                       .filter(path -> !path.equals(this.signatures))
                       .filter(path -> path.getFileName().toString().contains(invoiceId))
                    //   .map(Path::getFileName)
                       .map(Path::toString)
                       .collect(Collectors.toList());

            merger.signPdf(src,dest,images);
           }


    }

    @Override
    public void mergeAll() {

    }

    @Override
    public Path loadUnsignedInvoice(String filename) {
        return invoices.resolve(filename);
    }

    @Override
    public Path loadSignedInvoice(String filename) {
        return signed.resolve(filename);
    }

    @Override
    public Path loadImageSignature(String filename) {
        return signatures.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename,TYPE resourceType) {
        try{
            Path file = null;

                if(resourceType==TYPE.SIGNED)
                     file = loadSignedInvoice(filename);

                else if(resourceType==TYPE.UNSIGNED)
                     file = loadUnsignedInvoice(filename);
                 else
                    file = loadImageSignature(filename);



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

