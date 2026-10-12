package com.itheima.utils;

import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.credentials.CredentialsProvider;
import com.aliyun.sdk.service.oss2.credentials.StaticCredentialsProvider;
import com.aliyun.sdk.service.oss2.models.PutObjectRequest;
import com.aliyun.sdk.service.oss2.models.PutObjectResult;
import com.aliyun.sdk.service.oss2.transport.BinaryData;

import java.io.InputStream;

public class AliOssUtil {
    private static String ENDPOINT = "https://oss-cn-beijing.aliyuncs.com";
    private static String REGION = "cn-beijing";
    String accessKeyId = System.getenv("OSS_ACCESS_KEY_ID");
    String accessKeySecret = System.getenv("OSS_ACCESS_KEY_SECRET");
    private static String BUCKET_NAME = "big-event82378273";

    /**
     * 上传文件到阿里云 OSS
     *
     * @param objectName 上传到 OSS 后的对象名（即 key）
     * @param in         文件输入流
     * @return 上传成功后的文件访问地址
     */
    public static String upLoadFile(String objectName, InputStream in) throws Exception {
        // 创建凭证提供者
        CredentialsProvider credentialsProvider =
                new StaticCredentialsProvider(ACCESS_KEY_ID, ACCESS_KEY_SECRET);

        // 创建 OSSClient 实例
        OSSClient ossClient = OSSClient.newBuilder()
                .endpoint(ENDPOINT)
                .region(REGION)
                .credentialsProvider(credentialsProvider)
                .build();

        try {
            // 将输入流读取为字节数组
            byte[] fileContent = in.readAllBytes();
            BinaryData binaryData = BinaryData.fromBytes(fileContent);

            PutObjectRequest putObjectRequest = PutObjectRequest.newBuilder()
                    .bucket(BUCKET_NAME)
                    .key(objectName)
                    .body(binaryData)
                    .build();

            PutObjectResult result = ossClient.putObject(putObjectRequest);
            System.out.println("上传成功，ETag: " + result.eTag());

            // 返回文件访问地址
            return "https://" + BUCKET_NAME + "." + ENDPOINT.substring(ENDPOINT.indexOf("//") + 2) + "/" + objectName;
        } catch (Exception e) {
            System.out.println("上传失败: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (ossClient != null) {
                ossClient.close();
            }
        }
    }
}