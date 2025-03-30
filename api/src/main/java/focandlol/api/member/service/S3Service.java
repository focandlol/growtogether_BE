package focandlol.api.member.service;

import static focandlol.common.exception.response.ErrorCode.FILE_UPLOAD_FAILED;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import focandlol.common.exception.custom.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // 파일 업로드
    public String uploadFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

            // 파일 주소 반환
            return this.getFileUrl(fileName);

        } catch (IOException e) {
            throw new CustomException(FILE_UPLOAD_FAILED);

        }
    }

    public void deleteFile(String fileKey) {
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileKey));
        } catch (AmazonServiceException e) {
            throw new RuntimeException("S3에서 파일 삭제 실패: " + e.getMessage());
        }
    }

    // S3 URL 반환
    public String getFileUrl(String fileKey) {
        return amazonS3.getUrl(bucket, fileKey).toString();
    }


}