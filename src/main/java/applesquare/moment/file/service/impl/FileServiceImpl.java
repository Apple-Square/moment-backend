package applesquare.moment.file.service.impl;

import applesquare.moment.common.url.UrlManager;
import applesquare.moment.common.url.UrlPath;
import applesquare.moment.file.exception.FileTransferException;
import applesquare.moment.file.model.StorageFile;
import applesquare.moment.file.repository.StorageFileRepository;
import applesquare.moment.file.service.CloudinaryService;
import applesquare.moment.file.service.FileService;
import applesquare.moment.user.model.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private static final String FILE_NAME_SEPARATOR="_";
    private final String THUMBNAIL_FILE_PREFIX="thumb_";
    private final int THUMBNAIL_WIDTH_SIZE=500;  // 500px
    private final int THUMBNAIL_VIDEO_SEC=7;  // 7초

    private final UrlManager urlManager;
    private final CloudinaryService cloudinaryService;
    private final StorageFileRepository storageFileRepository;

    @Value("${applesquare.moment.file.upload-path}")
    private String uploadDirectory;


    /**
     * 파일을 저장소에 업로드
     * @param file 업로드할 파일
     * @return 업로드한 파일명
     */
    @Override
    public StorageFile upload(MultipartFile file, UserInfo writer) throws IOException{
        // file이 null이라면 예외 던지기
        if(file==null){
            throw new IllegalArgumentException("지원하지 않는 형식의 파일입니다. (type = null)");
        }

        // 파일명 정하기
        String filename=generateFilename(file);
        Path uploadPath=generateFilePath(filename);

        try{
            // 임시 저장된 파일을 업로드 경로로 전송
            file.transferTo(uploadPath);
        }catch(IOException e){
            e.printStackTrace();
            throw new FileTransferException();
        }

        // 저장소 파일 정보를 담고 있는 StorageFile 생성
        StorageFile storageFile=StorageFile.builder()
                .filename(filename)
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .uploader(writer)
                .build();

        try{
            // StorageFile을 DB에 저장
            storageFileRepository.save(storageFile);

        } catch (Exception e){
            e.printStackTrace();

            // StorageFile 엔티티 저장 도중 문제가 생겼다면, 업로드한 파일 삭제
            Files.deleteIfExists(uploadPath);

            throw e;
        }

        return storageFile;
    }

    /**
     * 썸네일 파일을 저장소에 업로드
     * @param filename 업로드된 원본 파일 이름
     * @param file 원본 파일
     * @return 업로드한 썸네일 파일명
     */
    @Override
    public StorageFile uploadThumbnail(MultipartFile file, String filename, UserInfo writer) throws IOException {
        if(FileService.isImage(file)) return uploadThumbnailImage(file, filename, writer, THUMBNAIL_WIDTH_SIZE);
        else if(FileService.isVideo(file)) return uploadThumbnailVideo(file, filename, writer, THUMBNAIL_WIDTH_SIZE, THUMBNAIL_VIDEO_SEC);
        else throw new IllegalArgumentException("지원하지 않는 형식의 파일입니다. (type="+file.getContentType()+")");
    }

    /**
     * 파일 조회
     * @param filename 파일명
     * @return 파일 자원
     * @throws FileNotFoundException 파일이 없는 경우 발생하는 예외
     */
    @Override
    public Resource read(String filename) throws FileNotFoundException {
        // 자원 찾기
        Path filePath=generateFilePath(filename);
        Resource resource=new FileSystemResource(filePath);

        // 자원이 존재하는지 검사
        if(resource.exists()){
            // 자원이 존재한다면 반환
            return resource;
        }
        else{
            // 자원이 존재하지 않는다면 예외 던지기
            throw new FileNotFoundException("존재하지 않는 파일입니다. (filename = "+filename+")");
        }
    }

    /**
     * 파일 삭제
     * @param filename 파일명
     * @throws IOException 파일 삭제에 실패한 경우, 혹은 파일이 이미 삭제된 경우 발생하는 예외
     */
    @Override
    public void delete(String filename) throws IOException {
        // 파일 찾기
        Path filePath=generateFilePath(filename);
        File file=filePath.toFile();

        // 파일이 존재하는지 검사
        if(file.exists()){
            // 파일이 존재한다면 삭제
            boolean deleted=file.delete();
            if(!deleted){
                // 파일 삭제에 실패했다면 예외 던지기
                throw new IOException("파일 삭제에 실패했습니다. (filename = " + filename + ")");
            }
        }
        else{
            // 파일이 이미 삭제된 경우 예외 던지기
            throw new FileNotFoundException("이미 삭제된 파일입니다. (filename = "+filename+")");
        }
    }

    /**
     * 썸네일 파일 삭제
     * @param filename 파일명
     */
    @Override
    public void deleteThumbnail(String filename) throws IOException {
        // 파일 찾기
        String thumbFilename=convertFilenameToThumbFilename(filename);
        Path filePath=generateFilePath(thumbFilename);
        File file=filePath.toFile();

        // 파일이 존재하는지 검사
        if(file.exists()){
            // 파일이 존재한다면 삭제
            boolean deleted=file.delete();
            if(!deleted){
                // 파일 삭제에 실패했다면 예외 던지기
                throw new IOException("파일 삭제에 실패했습니다. (filename = " + thumbFilename + ")");
            }

            // DB에서 StorageFile 삭제
            storageFileRepository.deleteByFilename(thumbFilename);
        }
        else{
            // 파일이 이미 삭제된 경우 예외 던지기
            throw new FileNotFoundException("이미 삭제된 파일입니다. (filename = "+thumbFilename+")");
        }
    }

    /**
     * 파일명 목록에 따라 저장소와 DB에서 파일 삭제
     * @param filenames 파일명 목록
     */
    @Override
    public void deleteByFilenames(List<String> filenames){
        // 저장소에서 파일 삭제
        for(String filename : filenames){
            try{
                delete(filename);
            } catch (IOException e){
                // TO DO : 삭제에 실패한 파일은 고아 리소스가 발생하지 않도록 DB나 로그 파일에 기록해둬야 한다.
                log.error("파일 삭제 실패 : "+filename);
            }
        }

        // DB에서 StorageFile 일괄 삭제
        storageFileRepository.deleteByFilenames(filenames);
    }

    /**
     * 파일의 ContentType 추출
     * @param resource 자원
     * @return ContentType
     * @throws IOException IOException
     */
    @Override
    public String getResourceContentType(Resource resource) throws IOException {
        if(resource.exists()){
            Path filePath=Paths.get(resource.getURI());
            String contentType=Files.probeContentType(filePath);

            // contentType이 null이면 기본 값으로 설정
            if(contentType==null){
                contentType="application/octet-stream";
            }

            return contentType;
        }
        else{
            throw new FileNotFoundException("존재하지 않는 자원입니다.");
        }
    }

    /**
     * 파일명으로 파일 URL 생성
     * @param filename 파일명
     * @return 파일 URL
     */
    @Override
    public String convertFilenameToUrl(String filename){
        return urlManager.getUrl(UrlPath.BACK_FILE_BASE_URL)+"/"+filename;
    }

    /**
     * 파일 URL에서 파일명을 추출
     * @param url 파일 URL
     * @return 파일명
     */
    @Override
    public String convertUrlToFilename(String url){
        // filename 앞에 baseURL 제거
        String prefix=urlManager.getUrl(UrlPath.BACK_FILE_BASE_URL)+"/";
        if(url.startsWith(prefix)){
            String filename=url.substring(prefix.length());

            // filename 뒤에 쿼리 스트링이 붙어있으면 제거
            int queryIdx=filename.indexOf('?');
            if(queryIdx != -1){
                filename=filename.substring(0, queryIdx);
            }

            return filename;
        }
        else{
            throw new IllegalArgumentException("파일 URL이 Base URL로 시작하지 않습니다. (url = "+url+")");
        }
    }

    @Override
    public String convertFilenameToThumbFilename(String filename){
        return THUMBNAIL_FILE_PREFIX+filename;
    }


    /**
     * 업로드에 사용할 파일명을 생성
     * @param file 파일
     * @return 파일명
     */
    private String generateFilename(MultipartFile file){
        String uuid= UUID.randomUUID().toString();
        String originalFilename=file.getOriginalFilename();

        // 원본 파일명이 null인 경우
        if(originalFilename==null){
            throw new IllegalArgumentException("파일명을 읽어올 수 없습니다.");
        }

        // 원본 파일명에 구분자가 포함되어 있다면 제거
        originalFilename=originalFilename.replace(FILE_NAME_SEPARATOR, "");

        // 파일명 앞에 UUID를 추가해서 중복 문제 방지
        return uuid+FILE_NAME_SEPARATOR+originalFilename;
    }

    private Path generateFilePath(String filename){
        return Paths.get(uploadDirectory, filename);
    }

    private StorageFile uploadThumbnailImage(MultipartFile originalImage, String filename, UserInfo writer, int width) throws IOException{
        // 파일 형식 검사
        if(originalImage==null || !FileService.isImage(originalImage)){
            throw new IllegalArgumentException("이미지 파일을 넣어주세요.");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // 원본 비율에 맞게 높이 조정
        String thumbFilename=convertFilenameToThumbFilename(filename);
        Path uploadPath=generateFilePath(thumbFilename);

        try{
            ByteArrayInputStream inputStream=new ByteArrayInputStream(originalImage.getBytes());
            Thumbnails.of(inputStream)
                    .width(width)
                    .keepAspectRatio(true)
                    .toOutputStream(outputStream);

            // 파일 이름과 경로 생성
            ByteArrayInputStream thumbnailInputStream = new ByteArrayInputStream(outputStream.toByteArray());

            // 썸네일 파일 저장 (ByteArrayOutputStream을 파일로 저장)
            Files.copy(thumbnailInputStream, uploadPath);

        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("썸네일 업로드에 실패했습니다.");
        }

        // StorageFile 생성
        StorageFile storageFile=StorageFile.builder()
                .filename(thumbFilename)
                .originalFilename(originalImage.getOriginalFilename())
                .contentType(originalImage.getContentType())
                .fileSize(originalImage.getSize())
                .uploader(writer)
                .build();

        try{
            // StorageFile을 DB에 저장
            storageFileRepository.save(storageFile);

        }catch (Exception e){
            e.printStackTrace();
            log.error("썸네일 사진 저장에 실패했습니다. (StorageFile 저장 실패)");

            // 썸네일 파일 삭제
            Files.delete(uploadPath);

            throw new IOException("썸네일 사진 저장에 실패했습니다.");
        }

        return storageFile;
    }

    /**
     * 주어진 너비와 길이에 따라 동영상을 자르고 업로드
     *
     * @param originalVideo 원본 동영상 파일
     * @param width 동영상의 너비
     * @param videoLength 동영상의 길이 (초 단위)
     * @throws IOException 입출력 오류가 발생한 경우
     */
    private StorageFile uploadThumbnailVideo(MultipartFile originalVideo, String filename, UserInfo writer, int width, int videoLength) throws IOException {
        // 파일 형식 검사
        if (originalVideo == null || !FileService.isVideo(originalVideo)) {
            throw new IllegalArgumentException("동영상 파일을 넣어주세요.");
        }

        // 파일 이름 생성
        String thumbFilename = convertFilenameToThumbFilename(filename);

        // 원본 영상을 편집해서 썸네일 파일 제작하기
        byte[] thumbFileBytes= cloudinaryService.convertVideoToThumbnail(originalVideo, thumbFilename, width, videoLength);

        // 썸네일 영상을 파일에 저장
        Path uploadPath = generateFilePath(thumbFilename);
        Files.write(uploadPath, thumbFileBytes);

        // StorageFile 생성
        StorageFile storageFile=StorageFile.builder()
                .filename(thumbFilename)
                .originalFilename(originalVideo.getOriginalFilename())
                .contentType(originalVideo.getContentType())
                .fileSize(originalVideo.getSize())
                .uploader(writer)
                .build();

        try{
            // StorageFile을 DB에 저장
            storageFileRepository.save(storageFile);

        }catch (Exception e){
            e.printStackTrace();
            log.error("썸네일 영상 저장에 실패했습니다. (StorageFile 저장 실패)");

            // 썸네일 파일 삭제
            Files.delete(uploadPath);

            throw new IOException("썸네일 영상 저장에 실패했습니다.");
        }

        return storageFile;
    }
}
