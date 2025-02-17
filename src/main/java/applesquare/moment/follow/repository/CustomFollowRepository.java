package applesquare.moment.follow.repository;

import applesquare.moment.user.model.UserInfo;
import com.querydsl.core.Tuple;

import java.util.List;

public interface CustomFollowRepository {
    // 키워드 기반 팔로워 검색
    List<Tuple> searchFollowerByKeyword(String userId, String keyword, Long cursor, int pageSize);
    // 키워드 기반 팔로위 검색
    List<Tuple> searchFolloweeByKeyword(String userId, String keyword, Long cursor, int pageSize);
    // 내가 멤버 ID 목록에 있는 사용자를 모두 팔로우했는지 여부 반환
    boolean isAllFollow(String myUserId, List<String> memberIds);
    // 멤버 ID 목록에 있는 모든 사용자가 나를 팔로우했는지 여부 반환
    boolean isAllFollowed(String myUserId, List<String> memberIds);
    // 특정 사용자와 상호 팔로우 관계에 있는 사용자 목록 검색
    List<UserInfo> searchMutualFollowers(String userId, String keyword, Long cursor, int pageSize);
    // 상대방 ID 목록 중에서 특정 사용자와 상호 팔로우 관계에 있는 사용자 ID 목록 조회
    List<String> findMutualFollowersInUserIds(String userId, List<String> otherUserIds);
}
