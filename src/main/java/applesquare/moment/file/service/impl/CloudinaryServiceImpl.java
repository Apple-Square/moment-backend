package applesquare.moment.file.service.impl;

import applesquare.moment.exception.FileTransferException;
import applesquare.moment.file.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.EagerTransformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Log4j2
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    private final Cloudinary cloudinary;

    @Override
    public byte[] convertVideoToThumbnail(MultipartFile video, String thumbFilename, int width, int videoLength) throws IOException {
        // 입력 동영상을 임시로 저장할 경로 정의
        Path tmpFile = Files.createTempFile("tmp_", video.getOriginalFilename());

        // 임시 파일에 원본 영상 저장
        try {
            video.transferTo(tmpFile.toFile());
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileTransferException();
        }

        // 변환 옵션 설정
        EagerTransformation eagerTransformation = new EagerTransformation()
                .duration(videoLength) // 영상 길이를 videoLength 초로 자름
                .width(width) // 너비를 width 변수만큼 설정
                .crop("scale"); // 높이는 원본 비율에 맞게 자동으로 설정

        Map<String, Object> uploadOptions = ObjectUtils.asMap(
                "resource_type", "video",
                "public_id", thumbFilename,
                "eager", Arrays.asList(eagerTransformation),
                "eager_async", false // 동기적으로 변환 수행
        );

        try {
            // Cloudinary 업로드 및 변환
            Map<String, Object> uploadResult = cloudinary.uploader().upload(tmpFile.toFile(), uploadOptions);

            // 변환된 파일 URL 추출
            List<Map<String, Object>> eagerResults = (List<Map<String, Object>>) uploadResult.get("eager");
            String transformedUrl = (String) eagerResults.get(0).get("secure_url");

            // 파일 다운로드 및 저장
            byte[] fileBytes = new RestTemplate().getForObject(transformedUrl, byte[].class);

            // 썸네일 영상의 비트 배열 반환
            return fileBytes;

        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("영상 편집에 실패했습니다.");
        } finally {
            // 임시 파일 삭제
            try {
                Files.deleteIfExists(tmpFile);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("임시 파일 삭제에 실패했습니다. (path=" + tmpFile + ")");
            }

            // 비동기로 Cloudinary 파일 삭제
            CompletableFuture.runAsync(() -> {
                try {
                    // 삭제 옵션 설정
                    Map<String, Object> deleteOptions = ObjectUtils.asMap(
                            "resource_type", "video",  // 비디오 파일 삭제
                            "type", "upload"  // 업로드된 파일 삭제
                    );

                    // Cloudinary에 업로드된 파일 삭제
                    cloudinary.uploader().destroy(thumbFilename, deleteOptions);
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("Cloudinary 파일 삭제에 실패했습니다. (public_id=" + thumbFilename + ")");
                }
            });
        }
    }
}
