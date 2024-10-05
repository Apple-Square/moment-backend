package applesquare.moment.user.controller;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.exception.ResponseMap;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserProfileController {
    private final UserProfileService userProfileService;


    /**
     * 나의 프로필 조회 API
     * @return  (status) 200
     *          (body)  조회 성공 메세지,
     *                  나의 프로필
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> readMyProfile(){
        // 나의 프로필 조회
        UserProfileReadResponseDTO userProfileReadResponseDTO=userProfileService.readMyProfile();

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "나의 프로필 조회에 성공했습니다.");
        responseMap.put("user", userProfileReadResponseDTO);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 사용자 프로필 사진 설정 API
     * @param profileImage 프로필 사진
     * @return 사진이 설정된 사용자의 ID
     */
    @PutMapping(value = "/{userId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updateProfileImage(@PathVariable String userId,
                                                                  @RequestParam(value = "profileImage") MultipartFile profileImage) throws Exception{
        // 사용자 프로필 사진 설정
        String result=userProfileService.updateProfileImage(userId, profileImage);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "사용자 프로필 사진 설정에 성공했습니다.");
        responseMap.put("userId", result);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 사용자 프로필 사진 삭제 API
     * @param userId 사용자 ID
     * @return  (status) 200,
     *          (body) 프로필 삭제 성공 메세지
     */
    @DeleteMapping(value = "/{userId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> deleteProfileImage(@PathVariable String userId) throws IOException {
        // 사용자 프로필 사진 삭제
        userProfileService.deleteProfileImage(userId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "사용자 프로필 사진 설정 해제에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 사용자 검색 API
     * (키워드와 비슷한 ID, 닉네임을 가진 사용자의 프로필 조회)
     *
     * @param size 페이지 크기
     * @param cursor 페이지 커서
     * @param keyword 검색 키워드
     * @return  (status) 200,
     *          (body)  검색 성공 메세지,
     *                  사용자 프로필 목록 페이지
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchByKeyword(@RequestParam(value = "size", required = false, defaultValue = "10") int size,
                                                               @RequestParam(value = "cursor", required = false) String cursor,
                                                               @RequestParam(value = "keyword", required = false) String keyword){
        // 페이지 요청 설정
        PageRequestDTO pageRequestDTO=PageRequestDTO.builder()
                .size(size)
                .cursor(cursor)
                .keyword(keyword)
                .build();

        // 키워드로 사용자 프로필 검색
        PageResponseDTO<UserProfileReadResponseDTO> pageResponseDTO=userProfileService.search(pageRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "사용자 검색에 성공했습니다.");
        responseMap.put("content", pageResponseDTO.getContent());
        responseMap.put("hasNext", pageResponseDTO.isHasNext());

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
