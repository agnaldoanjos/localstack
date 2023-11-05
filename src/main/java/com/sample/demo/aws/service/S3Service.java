package com.sample.demo.aws.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sample.demo.aws.dto.BucketObject;
import com.sample.demo.aws.dto.RequestData;
import com.sample.demo.aws.dto.S3Object;
import com.sample.demo.aws.exception.InternalException;
import lombok.RequiredArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private static final int MAX_SIZE = 1024;  // 1K
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());


    public List<S3Object> listBucketObjects(final String region, final String bucketName) {

        try {
            ListObjectsRequest listObjects = ListObjectsRequest
                    .builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsResponse res = s3Client.listObjects(listObjects);

            List<S3Object> s3ObjectList = res.contents().stream()
                    .map(obj -> new S3Object(obj.key(), obj.eTag(), obj.size(), obj.storageClassAsString()))
                    .collect(Collectors.toList());

            return s3ObjectList;
        } catch (Exception e) {
            throw new InternalException(e.getMessage(), HttpStatus.BAD_REQUEST.value(), e);
        }
    }

    public List<BucketObject> listBuckets() {

        try {
            ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();

            List<BucketObject> bucketObjectList = s3Client.listBuckets(listBucketsRequest).buckets().stream()
                    .map(obj -> new BucketObject(obj.name(), obj.creationDate()))
                    .collect(Collectors.toList());

            return bucketObjectList;

        } catch (S3Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public S3Object putObject(String bucketName, String key, byte[] content) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            RequestBody requestBody = RequestBody.fromBytes(content);

            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, requestBody);

            return S3Object.builder().key(key).storageClass("STANDARD").eTag(putObjectResponse.eTag()).size(requestBody.optionalContentLength().get()).build();
        } catch (S3Exception e) {
            throw new InternalException(e.getMessage(), HttpStatus.BAD_REQUEST.value(), e);
        }
    }

    public void deleteBucket(String bucketName) {
        try {
            DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.deleteBucket(deleteBucketRequest);
        } catch (S3Exception e) {
            throw new InternalException(e.getMessage(), HttpStatus.BAD_REQUEST.value(), e);
        }
    }

    public void createBucket(String bucketName) {
        try {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.createBucket(createBucketRequest);
        } catch (S3Exception e) {
            throw new InternalException(e.getMessage(), HttpStatus.BAD_REQUEST.value(), e);
        }
    }

    public void putBufferedObject(String bucketName, RequestData requestData) {
        try {

            byte[] data = objectMapper.writeValueAsBytes(requestData);

            if ((buffer.size() + data.length) > MAX_SIZE) {
                writeBufferToS3(
                        bucketName,
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/mm/ss"))
                );
                buffer.reset();
            }

            if (buffer.size() > 0) buffer.write(",".getBytes());
            buffer.write(data);

        } catch (S3Exception | IOException e) {
            e.printStackTrace();
            throw new InternalException(e.getMessage(), HttpStatus.BAD_REQUEST.value(), e);
        }
    }

    public static Path generateTempFilePath(String prefix, String suffix) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = prefix + UUID.randomUUID().toString() + suffix;
        return Paths.get(tempDir, fileName);
    }
    private void writeBufferToS3(final String bucketName, final String path) throws IOException {

        Path tempFile = generateTempFilePath("data", ".parquet");

        try (ParquetWriter<GenericData.Record> writer = AvroParquetWriter
                .<GenericData.Record>builder(new org.apache.hadoop.fs.Path(tempFile.toString()))
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withSchema(getSchema())
                .build()) {

            String value = new String(buffer.toByteArray());

            value = "[" + value + "]";

            System.out.println("Data: ");
            System.out.println(value);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(value.getBytes());
            List<RequestData> requestDataList = objectMapper.readValue(inputStream,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, RequestData.class));

            for (RequestData requestData : requestDataList) {
                //String valueJson = requestData.getValue();
                //GenericData.Record valueRecord = objectMapper.readValue(valueJson, GenericData.Record.class);
                GenericData.Record record = new GenericData.Record(getSchema());
                record.put("id", requestData.getId());
                record.put("user", requestData.getUser());
                record.put("value", requestData.getValue());
                record.put("createdDate", requestData.getCreatedDate().toString());
                writer.write(record);
            }

        }

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(path + "/data.parquet.snappy")
                        .build(),
                RequestBody.fromFile(tempFile)
        );
    }

    private Schema getSchema() {
        String schemaStr = "{"
                + "\"type\": \"record\","
                + "\"name\": \"RequestData\","
                + "\"fields\": ["
                + "{\"name\": \"id\", \"type\": \"string\"},"
                + "{\"name\": \"user\", \"type\": \"string\"},"
                + "{\"name\": \"value\", \"type\": \"string\"},"
//                + "{\"name\": \"value\", \"type\": {"
//                + "\"type\": \"record\","
//                + "\"name\": \"Value\","
//                + "\"fields\": ["
//                + "{\"name\": \"id\", \"type\": \"int\"},"
//                + "{\"name\": \"nome\", \"type\": \"string\"},"
//                + "{\"name\": \"dataNasc\", \"type\": \"string\"}"
//                + "]"
//                + "}},"
                + "{\"name\": \"createdDate\", \"type\": \"string\"}"
                + "]"
                + "}";
        return new Schema.Parser().parse(schemaStr);
    }

}
