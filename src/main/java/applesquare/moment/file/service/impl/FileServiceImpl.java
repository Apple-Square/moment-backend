package applesquare.moment.file.service.impl;

import applesquare.moment.exception.FileTransferException;
import applesquare.moment.file.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    private static final String FILE_NAME_SEPARATOR="_";
    @Value("${applesquare.moment.file.upload-path}")
    private String uploadDirectory;
    @Value("${applesquare.moment.file.base-url}")
    private String baseUrl;


    /**
     * 파일을 저장소에 업로드
     * @param file 업로드할 파일
     * @return 파일 URL
     */
    @Override
    public String upload(MultipartFile file){
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

        return convertFilenameToUrl(filename);
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
        return baseUrl+"/"+filename;
    }

    /**
     * 파일 URL에서 파일명을 추출
     * @param url 파일 URL
     * @return 파일명
     */
    @Override
    public String convertUrlToFilename(String url){
        // filename 앞에 baseURL 제거
        String prefix=baseUrl+"/";
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
}
