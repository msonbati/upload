package com.daralshifa.upload.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("storage")
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    private String signatures = "c:/medical-signature/signatures";
    private String invoices = "c:/medical-signature/invoices";
    private String signed = "c:/medical-signature/signed";

}