package applesquare.moment.oauth.naver.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NaverUserInfoReadResponseDTO {
    private String resultcode; // 응답 코드
    private String message; // 응답 메시지
    private Response response; // 실제 사용자 정보가 포함된 객체

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String id; // 사용자 고유 ID
        private String name; // 회원 이름
        private String nickname; // 별명
        private String profile_image; // 프로필 사진 URL
    }
}
