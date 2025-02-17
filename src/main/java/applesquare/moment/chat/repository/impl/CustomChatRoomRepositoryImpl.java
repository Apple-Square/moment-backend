package applesquare.moment.chat.repository.impl;

import applesquare.moment.chat.model.ChatRoom;
import applesquare.moment.chat.model.ChatRoomType;
import applesquare.moment.chat.repository.CustomChatRoomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static applesquare.moment.chat.model.QChatMessage.chatMessage;
import static applesquare.moment.chat.model.QChatRoom.chatRoom;
import static applesquare.moment.chat.model.QChatRoomMember.chatRoomMember;

@Transactional
@RequiredArgsConstructor
public class CustomChatRoomRepositoryImpl implements CustomChatRoomRepository {
    private final JPAQueryFactory queryFactory;


    /**
     * 키워드로 특정 사용자의 채팅방 목록 검색
     * - 검색 속성 : 채팅방 이름, 참여 멤버 ID, 참여 멤버 닉네임
     * - 정렬 기준 : 최근 대화한 시간이 가까운 순
     *
     * @param userId 사용자 ID
     * @param keyword 검색 키워드
     * @param cursor 페이지 커서
     * @param size 페이지 크기
     * @return 검색 조건에 부합하는 ChatRoom 목록
     */
    @Override
    public List<ChatRoom> search(String userId, String keyword, Long cursor, int size){
        // 커서 페이징 조건
        BooleanExpression cursorCondition=null;
        if(cursor!=null){
            cursorCondition=chatRoom.id.lt(cursor);
        }

        // 키워드 조건
        BooleanExpression keywordCondition=null;
        if(keyword!=null && !keyword.isBlank()){
            // 검색 요소 : 참여 멤버 ID, 참여 멤버 닉네임
            keywordCondition=chatRoom.roomName.contains(keyword)
                    .or(chatRoomMember.user.id.eq(keyword))
                    .or(chatRoomMember.user.nickname.contains(keyword));
        }

        // 채팅방 멤버 조건
        BooleanExpression roomMemberCondition = queryFactory
                .selectOne()
                .from(chatRoomMember)
                .where(chatRoomMember.chatRoom.id.eq(chatRoom.id), // 메인 쿼리의 chatRoom.id와 매칭
                        chatRoomMember.user.id.eq(userId))   // 특정 사용자가 속한 채팅방인지
                .exists();

        // 쿼리 실행
        List<ChatRoom> chatRooms=queryFactory
                .selectDistinct(chatRoom)
                .from(chatRoom)
                .leftJoin(chatRoom.members, chatRoomMember).fetchJoin()
                .leftJoin(chatRoom.lastMessage, chatMessage).fetchJoin()
                .where(roomMemberCondition.and(cursorCondition).and(keywordCondition))
                .orderBy(chatRoom.modDate.desc())
                .limit(size)
                .fetch();

        return chatRooms;
    }

    /**
     * 멤버 ID를 기반으로 1:1 채팅방 조회
     * @param myUserId 나의 사용자 ID
     * @param otherUserId 상대의 사용자 ID
     * @return 1:1 채팅방 정보
     */
    @Override
    public Optional<ChatRoom> findPrivateChatRoomByMemberId(String myUserId, String otherUserId){
        // 1:1 채팅방 조건
        BooleanExpression privateRoomCondition=chatRoom.roomType.stringValue().lower().eq(ChatRoomType.PRIVATE.name().toLowerCase());

        // 쿼리 실행
        List<String> memberIds=List.of(myUserId, otherUserId);
        List<ChatRoom> chatRooms=queryFactory
                .selectDistinct(chatRoom)
                .from(chatRoom)
                .innerJoin(chatRoom.members, chatRoomMember).fetchJoin()
                .where(privateRoomCondition.and(chatRoomMember.user.id.in(memberIds)))
                .groupBy(chatRoom.id)
                .having(chatRoomMember.user.id.countDistinct().eq((long) memberIds.size()))
                .fetch();

        if(chatRooms.size()>0){
            // 조회 결과가 존재하면 Optional에 감싸서 반환
            return Optional.of(chatRooms.get(0));
        }
        else{
            // 조회 결과가 존재하지 않으면 빈 Optional 반환
            return Optional.empty();
        }

    }
}
