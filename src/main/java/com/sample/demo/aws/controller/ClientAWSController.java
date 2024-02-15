package com.sample.demo.aws.controller;

import com.sample.demo.aws.dto.BucketObject;
import com.sample.demo.aws.dto.RequestData;
import com.sample.demo.aws.dto.ResponseData;
import com.sample.demo.aws.dto.S3Object;
import com.sample.demo.aws.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/was/s3")
@RequiredArgsConstructor
public class ClientAWSController {

    private final S3Service s3Service;

    String schemaStr = "{ \"type\": \"record\", \"name\": \"Person\", \"fields\": [ "
            + "{ \"name\": \"id\", \"type\": \"string\" }, "
            + "{ \"name\": \"name\", \"type\": \"string\" }, "
            + "{ \"name\": \"dob\", \"type\": \"string\" } ] }";

    @GetMapping("/region/{region}/bucket/{bucketName}")
    public ResponseEntity<ResponseData> listObjects(@PathVariable String region, @PathVariable String bucketName) {

        List<S3Object> s3ObjectList = s3Service.listBucketObjects(region, bucketName);

        return ResponseEntity.ok(ResponseData.builder()
                .message("Content listing successfully executed")
                .data(s3ObjectList)
                .build());
    }

    @GetMapping("/bucket")
    public ResponseEntity<ResponseData> listBuckets() {

        List<BucketObject> bucketObjectList = s3Service.listBuckets();

        return ResponseEntity.ok(ResponseData.builder()
                .data(bucketObjectList)
                .build());
    }

    @PostMapping("/bucket/{bucket}")
    public ResponseEntity<ResponseData> createBuckets(@PathVariable("bucket") String bucket) {

        s3Service.createBucket(bucket);

        ResponseData responseData = ResponseData.builder()
                .message("Bucket created successfully")
                .build();

        return ResponseEntity.ok(responseData);
    }

    @PutMapping("/bucket/{bucket}/key/{key}")
    public ResponseEntity<ResponseData> createObject(
            @PathVariable("bucket") String bucket,
            @PathVariable("key") String key,
            @RequestBody RequestData data
    ) {

        s3Service.putObject(bucket, key, data != null && data.getValue() != null  ? data.getValue().getBytes() : new byte[0]);

        ResponseData responseData = ResponseData.builder()
                .id(data.getId())
                .message("Object successfully loaded into bucket")
                .build();
        return ResponseEntity.ok(responseData);

    }
    @PutMapping("/buferred/bucket/{bucket}")
    public ResponseEntity<String> createBuferredObject(
            @PathVariable("bucket") String bucket,
            @RequestBody RequestData data
    ) {

        s3Service.putBufferedObject(bucket, data);

        return new ResponseEntity("accepted",HttpStatusCode.valueOf(202));
    }

    @DeleteMapping("/bucket/{bucket}")
    public ResponseEntity<ResponseData> deleteBuckets(@PathVariable("bucket") String bucket) {

        s3Service.deleteBucket(bucket);

        ResponseData responseData = ResponseData.builder()
                .message("Bucket successfully deleted")
                .build();

        return ResponseEntity.ok(responseData);
    }


}
