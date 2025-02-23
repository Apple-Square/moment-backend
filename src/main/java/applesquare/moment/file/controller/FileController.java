package applesquare.moment.file.controller;

import applesquare.moment.chat.service.ChatRoomService;
import applesquare.moment.common.security.SecurityService;
import applesquare.moment.file.StorageFileReadResponseDTO;
import applesquare.moment.file.model.FileAccessGroupType;
import applesquare.moment.file.model.FileAccessPolicy;
import applesquare.moment.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
    private final SecurityService securityService;
    private final ChatRoomService chatRoomService;


    /**
     * 파일 조회 API
     * @param filename 파일명
     * @return  (status) 200,
     *          (contentType) 파일 형식
     *          (body) 파일 자원
     * @throws IOException IOException
     */
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> read(@PathVariable(name = "filename") String filename) throws IOException {
        // 파일 정보 가져오기
        StorageFileReadResponseDTO storageFileDTO=fileService.readStorageFile(filename);

        // 파일의 접근 정책에 따라 권한 검사
        String myUserId;
        FileAccessPolicy accessPolicy=storageFileDTO.getAccessPolicy();
        switch (accessPolicy){
            // 권한 검사할 필요 없음
            case PUBLIC:
                break;
            // 로그인한 유저만 조회 가능
            case AUTHENTICATED:
                // 로그인 상태가 아닐 경우, getUserId()에서 에러 던짐
                securityService.getUserId();
                break;
            // 업로더만 조회 가능
            case OWNER:
                myUserId=securityService.getUserId();
                if(!storageFileDTO.getUploaderId().equals(myUserId)) {
                    throw new AccessDeniedException("파일의 업로더만 조회할 수 있습니다.");
                }
                break;
            case GROUP: // 특정 그룹만 조회 가능
                myUserId=securityService.getUserId();
                FileAccessGroupType groupType=storageFileDTO.getGroupType();
                if(groupType==FileAccessGroupType.CHAT){
                    String chatRoomIdStr=storageFileDTO.getGroupId();
                    if(chatRoomIdStr!=null){
                        Long chatRoomId=Long.parseLong(chatRoomIdStr);
                        if(!chatRoomService.isMember(chatRoomId, myUserId)){
                            // 채팅방 멤버가 아닌 사람이 파일을 조회하려고 하면 거부
                            throw new AccessDeniedException("채팅방 멤버만 접근할 수 있습니다.");
                        }
                    }
                }
                break;
        }

        // 파일 조회
        Resource resource=fileService.readResource(filename);

        // 파일 형식 조회
        String contentType=fileService.getResourceContentType(resource);
        MediaType mediaType=MediaType.parseMediaType(contentType);

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(mediaType)
                .body(resource);
    }
}
