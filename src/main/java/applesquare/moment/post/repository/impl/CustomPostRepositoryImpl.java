package applesquare.moment.post.repository.impl;

import applesquare.moment.post.repository.CustomPostRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static applesquare.moment.file.model.QStorageFile.storageFile;
import static applesquare.moment.post.model.QPost.post;
import static applesquare.moment.tag.model.QTag.tag;
import static applesquare.moment.user.model.QUserInfo.userInfo;

@Transactional
@RequiredArgsConstructor
public class CustomPostRepositoryImpl implements CustomPostRepository {
    private final JPAQueryFactory queryFactory;

    /**
     * 키워드로 게시물 검색
     * - 검색 속성 : 게시물 내용, 작성자 닉네임, 태그명
     * - 정렬 기준 : 최신순
     *
     * @param keyword 검색 키워드
     * @param cursor 페이지 커서
     * @param size 페이지 크기
     * @return  검색 조건에 부합하는 Post ID 목록
     */
    @Override
    public List<Long> searchPostIdsByKeyword(String keyword, Long cursor, int size){
        // 검색 조건에 부합하는 게시물 ID 조회
        BooleanExpression cursorCondition=null;
        if(cursor!=null){
            cursorCondition=post.id.lt(cursor);
        }
        BooleanExpression keywordCondition=post.content.contains(keyword)
                .or(userInfo.nickname.contains(keyword))
                .or(tag.name.contains(keyword));

        return queryFactory
                .selectDistinct(post.id)
                .from(post)
                .leftJoin(post.tags, tag)
                .leftJoin(post.writer, userInfo)
                .where(keywordCondition.and(cursorCondition))
                .orderBy(post.id.desc())
                .limit(size)
                .fetch();
    }

    /**
     * 키워드로 모먼트 검색
     * - 검색 속성 : 게시물 내용, 작성자 닉네임, 태그명
     * - 정렬 기준 : 최신순
     *
     * @param keyword 검색 키워드
     * @param cursor 페이지 커서
     * @param size 페이지 크기
     * @return 검색 조건에 부합하는 모먼트 목록
     */
    @Override
    public List<Long> searchMomentIdsByKeyword(String keyword, Long cursor, int size){
        // 검색 조건에 부합하는 게시물 ID 조회
        BooleanExpression cursorCondition=null;
        if(cursor!=null){
            cursorCondition=post.id.lt(cursor);
        }

        return queryFactory
                .selectDistinct(post.id)
                .from(post)
                .leftJoin(post.tags, tag)
                .leftJoin(post.files, storageFile)
                .leftJoin(post.writer, userInfo)
                .where(storageFile.contentType.startsWith("video")
                        .and(
                                post.content.contains(keyword)
                                        .or(userInfo.nickname.contains(keyword))
                                        .or(tag.name.contains(keyword))
                        )
                        .and(cursorCondition)
                )
                .orderBy(post.id.desc())
                .limit(size)
                .fetch();
    }
}
