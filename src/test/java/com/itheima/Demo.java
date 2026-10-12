import com.aliyun.sdk.service.oss2.OSSClient;
import com.aliyun.sdk.service.oss2.credentials.CredentialsProvider;
import com.aliyun.sdk.service.oss2.credentials.StaticCredentialsProvider;
import com.aliyun.sdk.service.oss2.models.*;
import com.aliyun.sdk.service.oss2.transport.BinaryData;

import java.io.File;
import java.io.FileInputStream;

public class Demo {

    public static void main(String[] args) throws Exception {

        String endpoint = "https://oss-cn-beijing.aliyuncs.com";
        String region = "cn-beijing";
        String accessKeyId = System.getenv("OSS_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("OSS_ACCESS_KEY_SECRET");
        String bucketName = "big-event82378273";
        String localFilePath = "D:\\test\\001.jpg";
        String objectKey = "avatar.jpg";

        // 创建凭证提供者（使用构造器）
        CredentialsProvider credentialsProvider = new StaticCredentialsProvider(accessKeyId, accessKeySecret);

        // 创建 OSSClient 实例
        OSSClient ossClient = OSSClient.newBuilder()
                .endpoint(endpoint)
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        try {
            File file = new File(localFilePath);
            byte[] fileContent = new FileInputStream(file).readAllBytes();
            BinaryData binaryData = BinaryData.fromBytes(fileContent);

            PutObjectRequest putObjectRequest = PutObjectRequest.newBuilder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .body(binaryData)
                    .build();

            PutObjectResult result = ossClient.putObject(putObjectRequest);
            System.out.println("上传成功，ETag: " + result.eTag());

        } catch (Exception e) {
            System.out.println("上传失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (ossClient != null) {
                ossClient.close();
            }
        }
    }
}