package applesquare.moment.file.service;

import applesquare.moment.file.model.MediaType;
import applesquare.moment.file.model.StorageFile;
import applesquare.moment.user.model.UserInfo;
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

    /**
     * 파일의 contentType을 MediaType 열거형으로 변환
     * @param contentType 파일 타입 문자열
     * @return MediaType 열거형
     */
    static MediaType convertContentTypeToMediaType(String contentType){
        if(contentType==null) throw new IllegalArgumentException("contentType이 null입니다.");
        if(contentType.startsWith(CONTENT_TYPE_IMAGE_PREFIX)) return MediaType.IMAGE;
        else if(contentType.startsWith(CONTENT_TYPE_VIDEO_PREFIX)) return MediaType.VIDEO;
        else throw new IllegalArgumentException("mediaType으로 변경할 수 없는 contentType입니다. (contentType="+contentType+")");
    }


    StorageFile upload(MultipartFile file, UserInfo writer) throws IOException;
    StorageFile uploadThumbnail(MultipartFile file, String filename, UserInfo writer) throws IOException;
    Resource read(String filename) throws FileNotFoundException;
    void delete(String filename) throws IOException;
    void deleteThumbnail(String filename) throws IOException;

    String getResourceContentType(Resource resource) throws IOException;

    String convertUrlToFilename(String url);
    String convertFilenameToUrl(String filename);

    String convertFilenameToThumbFilename(String filename);
}
