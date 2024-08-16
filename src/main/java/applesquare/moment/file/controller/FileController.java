package applesquare.moment.file.controller;

import applesquare.moment.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {
    private final FileService fileService;

    /**
     * 파일 조회 API
     * @param filename 파일명
     * @return  (status) 200,
     *          (contentType) 파일 형식
     *          (body) 파일 자원
     * @throws IOException IOException
     */
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> read(@PathVariable String filename) throws IOException {
        // 파일 조회
        Resource resource=fileService.read(filename);

        // 파일 형식 조회
        String contentType=fileService.getContentType(resource);
        MediaType mediaType=MediaType.parseMediaType(contentType);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(mediaType)
                .body(resource);
    }
}
