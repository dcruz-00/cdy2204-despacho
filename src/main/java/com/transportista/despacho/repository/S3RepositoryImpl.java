package com.transportista.despacho.repository;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class S3RepositoryImpl implements S3Repository {

    private static final Logger log = LoggerFactory.getLogger(S3RepositoryImpl.class);

    private final AmazonS3 s3Client;

    @Autowired
    public S3RepositoryImpl(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String uploadFile(String bucketName, String fileName, File fileObj) {
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
        fileObj.delete();
        return fileName;
    }

    @Override
    public S3ObjectInputStream getObject(String bucketName, String fileName) throws IOException {
        if (!s3Client.doesBucketExistV2(bucketName)) {
            log.error("No Bucket Found");
            return null;
        }
        return s3Client.getObject(bucketName, fileName).getObjectContent();
    }

    @Override
    public byte[] downloadFile(String bucketName, String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        try {
            return IOUtils.toByteArray(s3Object.getObjectContent());
        } catch (IOException e) {
            log.error("Error descargando archivo de S3", e);
        }
        return null;
    }

    @Override
    public void moveObject(String bucketName, String fileKey, String destinationFileKey) {
        s3Client.copyObject(new CopyObjectRequest(bucketName, fileKey, bucketName, destinationFileKey));
        deleteObject(bucketName, fileKey);
    }

    @Override
    public void deleteObject(String bucketName, String fileKey) {
        s3Client.deleteObject(bucketName, fileKey);
    }

    @Override
    public List<String> listObjectsByPrefix(String bucketName, String prefix) {
        return s3Client.listObjectsV2(bucketName, prefix)
                .getObjectSummaries()
                .stream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
    }
}