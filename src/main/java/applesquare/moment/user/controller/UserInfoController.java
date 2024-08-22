package applesquare.moment.user.controller;

import applesquare.moment.exception.ResponseMap;
import applesquare.moment.user.dto.UserInfoUpdateRequestDTO;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.user.service.UserInfoService;
import jakarta.validation.Valid;
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
public class UserInfoController {
    private final UserInfoService userInfoService;


    /**
     * 나의 프로필 조회 API
     * @return  (status) 200
     *          (body)  조회 성공 메세지,
     *                  나의 프로필
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> readMe(){
        // 나의 프로필 조회
        UserProfileReadResponseDTO userProfileReadResponseDTO=userInfoService.readMyProfile();

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "나의 프로필 조회에 성공했습니다.");
        responseMap.put("user", userProfileReadResponseDTO);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }


    /**
     * 사용자 정보 수정 API
     * @param userInfoUpdateRequestDTO 수정된 사용자 정보 입력
     * @return  (status) 200,
     *          (body)  수정 성공 메세지,
     *                  수정된 사용자의 ID
     */
    @PatchMapping(value = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updateUserInfo(@PathVariable String userId, @Valid @RequestBody UserInfoUpdateRequestDTO userInfoUpdateRequestDTO){
        // 사용자 정보 수정
        String result=userInfoService.updateUserInfo(userId, userInfoUpdateRequestDTO);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "사용자 정보 수정에 성공했습니다.");
        responseMap.put("userId", result);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }

    /**
     * 사용자 프로필 사진 설정 API
     * @param profileImage 프로필 사진
     * @return 사진이 설정된 사용자의 ID
     */
    @PutMapping(value = "/{userId}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updateProfileImage(@PathVariable String userId,
                                                                  @RequestParam(value = "profileImage") MultipartFile profileImage){
        // 사용자 프로필 사진 설정
        String result=userInfoService.updateProfileImage(userId, profileImage);

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
    public ResponseEntity<Map<String, Object>> deleteMyProfileImage(@PathVariable String userId) throws IOException {
        // 사용자 프로필 사진 삭제
        userInfoService.deleteProfileImage(userId);

        // 응답 생성
        ResponseMap responseMap=new ResponseMap();
        responseMap.put("message", "사용자 프로필 사진 설정 해제에 성공했습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(responseMap.getMap());
    }
}
