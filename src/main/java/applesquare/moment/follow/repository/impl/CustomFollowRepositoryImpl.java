package applesquare.moment.follow.repository.impl;

import applesquare.moment.follow.repository.CustomFollowRepository;
import applesquare.moment.user.model.UserInfo;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static applesquare.moment.file.model.QStorageFile.storageFile;
import static applesquare.moment.follow.model.QFollow.follow;
import static applesquare.moment.user.model.QUserInfo.userInfo;

@Transactional
@RequiredArgsConstructor
public class CustomFollowRepositoryImpl implements CustomFollowRepository {
    private final JPAQueryFactory queryFactory;

    /**
     * 키워드에 따라 특정 사용자의 팔로워 검색
     * @param userId 특정 사용자의 ID
     * @param keyword 검색 키워드
     * @param cursor 페이지 커서
     * @param pageSize 페이지 크기
     * @return Follow 목록
     */
    public List<Tuple> searchFollowerByKeyword(String userId, String keyword, Long cursor, int pageSize){
        // Followee 조건
        BooleanExpression followeeEqUserId=follow.followee.id.eq(userId);

        // 키워드에 따른 검색 조건
        BooleanExpression followerIdEqOrNicknameLikeKeyword=null;
        if(keyword!=null && !keyword.isBlank()){
            followerIdEqOrNicknameLikeKeyword=follow.follower.id.eq(keyword)
                    .or(follow.follower.nickname.containsIgnoreCase(keyword));
        }

        // 커서 페이징 조건
        BooleanExpression cursorCondition=null;
        if(cursor!=null){
            cursorCondition=follow.id.lt(cursor);
        }

        // 메인 쿼리 작성
        return queryFactory
                .select(follow.id,
                        follow.follower.id,
                        follow.follower.nickname,
                        follow.follower.profileImage.filename)
                .from(follow)
                .leftJoin(follow.follower, userInfo)
                .leftJoin(userInfo.profileImage, storageFile)
                .where(followeeEqUserId, followerIdEqOrNicknameLikeKeyword, cursorCondition)
                .orderBy(follow.id.desc())
                .limit(pageSize)
                .fetch();
    }

    /**
     * 키워드에 따라 특정 사용자의 팔로잉 검색
     * @param userId 특정 사용자의 ID
     * @param keyword 검색 키워드
     * @param cursor 페이지 커서
     * @param pageSize 페이지 크기
     * @return Follow 목록
     */
    public List<Tuple> searchFolloweeByKeyword(String userId, String keyword, Long cursor, int pageSize){
        // Follower 조건
        BooleanExpression followerEqUserId=follow.follower.id.eq(userId);

        // 키워드에 따른 검색 조건
        BooleanExpression followeeIdEqOrNicknameLikeKeyword=null;
        if(keyword!=null && !keyword.isBlank()){
            followeeIdEqOrNicknameLikeKeyword=follow.followee.id.eq(keyword)
                    .or(follow.followee.nickname.containsIgnoreCase(keyword));
        }

        // 커서 페이징 조건
        BooleanExpression cursorCondition=null;
        if(cursor!=null){
            cursorCondition=follow.id.lt(cursor);
        }

        // 메인 쿼리 작성
        return queryFactory
                .select(follow.id,
                        follow.followee.id,
                        follow.followee.nickname,
                        follow.followee.profileImage.filename)
                .from(follow)
                .leftJoin(follow.followee, userInfo)
                .leftJoin(userInfo.profileImage, storageFile)
                .where(followerEqUserId, followeeIdEqOrNicknameLikeKeyword, cursorCondition)
                .orderBy(follow.id.desc())
                .limit(pageSize)
                .fetch();
    }


    /**
     * 내가 멤버 ID 목록에 있는 사용자를 모두 팔로우했는지 여부
     * @param myUserId 내 사용자 ID
     * @param memberIds 멤버 ID 목록
     * @return 모두 팔로우했는지 여부
     */
    public boolean isAllFollow(String myUserId, List<String> memberIds) {
        // 멤버 목록에서 나 자신을 제외
        memberIds=memberIds.stream()
                .filter((memberId)->!memberId.equals(myUserId))
                .collect(Collectors.toList());

        // 나를 제외한 멤버의 수가 0명이면 바로 true 반환
        if(memberIds.size()==0) return true;

        // myUserId가 follower인 Follow 엔티티에서, followee ID가 memberIds에 포함된 개수 조회
        Long count = queryFactory
                .select(follow.followee.id.countDistinct())
                .from(follow)
                .where(
                        follow.follower.id.eq(myUserId),  // 내가 팔로우한 관계만 확인
                        follow.followee.id.in(memberIds)  // 내가 팔로우한 사람이 memberIds에 포함되는지
                )
                .fetchOne();

        // 내가 팔로우한 사람이 입력받은 memberIds의 크기와 같다면 모든 조건 충족
        return count != null && count == memberIds.size();
    }

    /**
     * 멤버 ID 목록에 있는 모든 사용자가 나를 팔로우했는지 여부
     * @param myUserId 내 사용자 ID
     * @param memberIds 멤버 ID 목록
     * @return 모든 멤버가 나를 팔로우했는지 여부
     */
    public boolean isAllFollowed(String myUserId, List<String> memberIds) {
        // 멤버 목록에서 나 자신을 제외
        memberIds=memberIds.stream()
                .filter((memberId)->!memberId.equals(myUserId))
                .collect(Collectors.toList());

        // 나를 제외한 멤버의 수가 0명이면 바로 true 반환
        if(memberIds.size()==0) return true;

        // myUserId가 followee인 Follow 엔티티에서, follower ID가 memberIds에 포함된 개수 조회
        Long count = queryFactory
                .select(follow.follower.id.countDistinct())
                .from(follow)
                .where(
                        follow.followee.id.eq(myUserId),  // 내가 팔로우한 관계만 확인
                        follow.follower.id.in(memberIds)  // 내가 팔로우한 사람이 memberIds에 포함되는지
                )
                .fetchOne();

        // 내가 팔로우한 사람이 입력받은 memberIds의 크기와 같다면 모든 조건 충족
        return count != null && count == memberIds.size();
    }

    /**
     * 특정 사용자와 상호 팔로우 관계에 있는 사용자 목록 검색
     * - 검색 요소 : 상호 팔로워의 ID/닉네임
     * - 정렬 기준 : 상호 팔로워의 닉네임 사전순
     *
     * @param userId 사용자 ID
     * @param keyword 검색 키워드
     * @param cursor 페이지 커서
     * @param pageSize 페이지 크기
     * @return 상호 팔로우 사용자 목록
     */
    @Override
    public List<UserInfo> searchMutualFollowers(String userId,String keyword, Long cursor, int pageSize) {
        // 키워드 조건
        BooleanExpression keywordCondition=null;
        if(keyword!=null && !keyword.isBlank()){
            // 상대의 사용자 ID가 keyword와 일치하거나, 상대의 닉네임이 keyword를 포함하는 경우
            keywordCondition=follow.follower.id.eq(keyword)
                    .or(follow.follower.nickname.containsIgnoreCase(keyword));
        }

        // 특정 사용자(userId)의 팔로워 ID 목록
        JPQLQuery<String> myFollowerIds=JPAExpressions
                .select(follow.follower.id)
                .from(follow)
                .where(follow.followee.id.eq(userId)
                        .and(keywordCondition));

        // 커서 페이징 조건
        BooleanExpression cursorCondition=null;
        if(cursor!=null){
            cursorCondition=follow.id.lt(cursor);
        }

        // 상호 팔로우한 사용자 ID 목록
        BooleanExpression mutualFollowCondition=follow.followee.id.in(myFollowerIds);
        JPQLQuery<String> mutualFollowUserIds=JPAExpressions
                .select(follow.followee.id)
                .from(follow)
                .where(follow.follower.id.eq(userId)
                        .and(mutualFollowCondition)
                        .and(cursorCondition))
                .orderBy(follow.followee.nickname.asc())
                .limit(pageSize);

        // 상호 팔로우 사용자 정보 조회
        return queryFactory
                .select(userInfo)
                .from(userInfo)
                .where(userInfo.id.in(mutualFollowUserIds))
                .fetch();
    }

    /**
     * 상대방 ID 목록 중에서 특정 사용자와 상호 팔로우 관계에 있는 사용자 ID 목록 조회
     * @param userId 특정 사용자 ID
     * @param otherUserIds 상대방 ID 목록
     * @return 상호 팔로우 관계에 있는 사용자 ID 목록
     */
    @Override
    public List<String> findMutualFollowersInUserIds(String userId, List<String> otherUserIds){
        // 상대방 ID 목록이 null이거나 비어있다면, 상호 팔로워 목록으로 빈 리스트 반환
        if(otherUserIds==null || otherUserIds.size()==0){
            return List.of();
        }

        // 상대방 ID 목록 중 특정 사용자(userId)의 팔로워 ID 목록
        JPQLQuery<String> myFollowerIds=JPAExpressions
                .select(follow.follower.id)
                .from(follow)
                .where(follow.follower.id.in(otherUserIds)
                        .and(follow.followee.id.eq(userId)));

        // 상호 팔로우한 사용자 ID 목록
        BooleanExpression mutualFollowCondition=follow.followee.id.in(myFollowerIds);

        // 상호 팔로우 사용자 정보 조회
        return queryFactory
                .select(follow.followee.id)
                .from(follow)
                .where(follow.follower.id.eq(userId)
                        .and(mutualFollowCondition))
                .fetch();
    }
}
