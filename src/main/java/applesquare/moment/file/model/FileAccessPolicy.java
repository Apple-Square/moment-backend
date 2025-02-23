package applesquare.moment.file.model;

public enum FileAccessPolicy {
    PUBLIC,         // 모든 사용자 (로그인 여부와 관계없이 접근 가능)
    AUTHENTICATED,  // 인증된 사용자만 접근 가능
    OWNER,          // 업로더 본인만 접근 가능
    GROUP          // 특정 그룹 멤버만 접근 가능
}
