package com.transportista.despacho.repository;

import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface S3Repository {
    String uploadFile(String bucketName, String fileName, java.io.File fileObj);
    S3ObjectInputStream getObject(String bucketName, String fileName) throws IOException;
    byte[] downloadFile(String bucketName, String fileName);
    void moveObject(String bucketName, String fileKey, String destinationFileKey);
    void deleteObject(String bucketName, String fileKey);
    List<String> listObjectsByPrefix(String bucketName, String prefix);
}