package applesquare.moment.tag.repository.impl;

import applesquare.moment.tag.dto.TagReadResponseDTO;
import applesquare.moment.tag.repository.CustomTagRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static applesquare.moment.tag.model.QTag.tag;

@RequiredArgsConstructor
@Transactional
public class CustomTagRepositoryImpl implements CustomTagRepository {
    private final JPQLQueryFactory queryFactory;

    @Override
    public List<TagReadResponseDTO> searchByKeyword(String keyword, Long cursor, int pageSize){
        // 키워드에 따른 검색 조건
        BooleanExpression tagNameLikeKeyword=null;
        if(keyword!=null && !keyword.isBlank()){
            tagNameLikeKeyword=tag.name.contains(keyword);
        }

        // 커서 페이징 조건
        BooleanExpression cursorCondition=null;
        if(cursor!=null){
            Integer cursorUsageCount=queryFactory
                    .select(tag.posts.size())
                    .from(tag)
                    .where(tag.id.eq(cursor))
                    .fetchOne();

            // 사용된 회수가 cursor의 usageCount보다 작거나, 같으면서 태그 ID가 더 작으면
            if(cursorUsageCount!=null) {
                cursorCondition = tag.posts.size().lt(cursorUsageCount)
                        .or(tag.posts.size().eq(cursorUsageCount).and(tag.id.lt(cursor)));
            }else{
                throw new IllegalArgumentException("존재하지 않는 커서입니다. (cursor="+cursor+")");
            }
        }

        // 메인 쿼리 작성
        return queryFactory
                .select(Projections.fields(TagReadResponseDTO.class,
                        tag.id.as("id"),
                        tag.name.as("name"),
                        tag.posts.size().longValue().as("usageCount")))  // usageCount 필드가 long 타입이라 캐스팅
                .from(tag)
                .where(tagNameLikeKeyword.and(cursorCondition))
                .orderBy(tag.posts.size().desc())
                .limit(pageSize)
                .fetch();
    }
}
