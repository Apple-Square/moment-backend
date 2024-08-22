package applesquare.moment.file.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;


public interface FileService {
    String CONTENT_TYPE_IMAGE_PREFIX="image/";
    String CONTENT_TYPE_VIDEO_PREFIX="video/";

    /**
     * 이미지 파일인지 검사
     * @param file 파일
     * @return 이미지 파일인지 여부
     */
    static boolean isImage(MultipartFile file){
        String contentType=file.getContentType();
        return contentType!=null && contentType.startsWith(CONTENT_TYPE_IMAGE_PREFIX);
    }

    /**
     * 동영상 파일인지 검사
     * @param file 파일
     * @return 동영상 파일인지 여부
     */
    static boolean isVideo(MultipartFile file){
        String contentType=file.getContentType();
        return contentType!=null && contentType.startsWith(CONTENT_TYPE_VIDEO_PREFIX);
    }


    String upload(MultipartFile file);
    Resource read(String filename) throws FileNotFoundException;
    void delete(String filename) throws IOException;

    String getResourceContentType(Resource resource) throws IOException;

    String convertUrlToFilename(String url);
    String convertFilenameToUrl(String filename);
}
