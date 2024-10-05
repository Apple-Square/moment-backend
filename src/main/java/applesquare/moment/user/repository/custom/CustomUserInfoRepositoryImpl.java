package applesquare.moment.user.repository.custom;

import applesquare.moment.user.model.UserInfo;
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
public class CustomUserInfoRepositoryImpl implements CustomUserInfoRepository {
    private final JPAQueryFactory queryFactory;
    @Override
    public List<UserInfo> searchByKeyword(String keyword, String cursor, int pageSize){
        // 키워드에 따른 검색 조건
        BooleanExpression userIdEqOrNicknameLikeKeyword=null;
        if(keyword!=null && !keyword.isBlank()){
            // 사용자 ID가 keyword와 같거나, 닉네임이 keyword를 포함하고 있다면
            userIdEqOrNicknameLikeKeyword=userInfo.id.eq(keyword)
                    .or(userInfo.nickname.containsIgnoreCase(keyword));
        }

        // 커서 페이징 조건
        BooleanExpression cursorCondition=null;
        if(cursor!=null){
            // 서브 쿼리 정의
            long cursorFollowCount = queryFactory
                    .select(follow.count())
                    .from(userInfo)
                    .leftJoin(follow).on(follow.followee.id.eq(userInfo.id))
                    .where(userInfo.id.eq(cursor))
                    .fetchOne();

            // 팔로워 수가 커서 데이터의 팔로워 수보다 작거나, 같으면서 사용자 ID가 더 크다면
            cursorCondition=follow.count().lt(cursorFollowCount)
                    .or(follow.count().eq(cursorFollowCount).and(userInfo.id.gt(cursor)));
        }

        // 메인 쿼리 작성
        return queryFactory
                .select(userInfo)
                .from(userInfo)
                .leftJoin(userInfo.profileImage, storageFile)
                .leftJoin(follow).on(follow.followee.id.eq(userInfo.id))
                .where(userIdEqOrNicknameLikeKeyword)
                .groupBy(userInfo.id)
                .having(cursorCondition)
                .orderBy(follow.count().desc(), userInfo.id.asc())
                .limit(pageSize)
                .fetch();
    }
}