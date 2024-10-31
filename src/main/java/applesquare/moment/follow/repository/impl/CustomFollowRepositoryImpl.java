package applesquare.moment.follow.repository.impl;

import applesquare.moment.follow.repository.CustomFollowRepository;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}
