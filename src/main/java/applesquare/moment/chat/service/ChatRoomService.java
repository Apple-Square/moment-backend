package applesquare.moment.chat.service;

import applesquare.moment.chat.dto.ChatRoomCreateRequestDTO;
import applesquare.moment.chat.dto.ChatRoomInviteRequestDTO;
import applesquare.moment.chat.dto.ChatRoomReadAllResponseDTO;
import applesquare.moment.chat.dto.ChatRoomReadResponseDTO;
import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;

import java.util.List;

public interface ChatRoomService {
    // 채팅방을 처음 생성할 때 2명 이상이어야 한다는 거지, 멤버가 1명인 채팅방이 없다는 의미는 아니다.
    // 중간에 멤버가 한 명 빼고 다 나가면, 멤버 1명인 채팅방도 존재할 수 있다.
    int MIN_MEMBER_COUNT=2;
    int MAX_MEMBER_COUNT=100;

    // 채팅방 목록에서 각 채팅방 별로 보여지는 썸네일 멤버 프로필 사진의 최대 개수
    int MAX_MEMBER_PROFILE_IMAGE_COUNT=4;


    // 특정 사용자의 채팅방 목록 검색
    PageResponseDTO<ChatRoomReadAllResponseDTO> search(String myUserId, PageRequestDTO pageRequestDTO);
    // 특정 채팅방 멤버 목록 조회
    PageResponseDTO<UserProfileReadResponseDTO> readMemberProfileAllByRoomId(String myUserId, Long roomId, PageRequestDTO pageRequestDTO);

    // 채팅방 생성 (모든 유형의 채팅방 생성 가능)
    ChatRoomReadResponseDTO create(String myUserId, ChatRoomCreateRequestDTO chatRoomCreateRequestDTO);
    // 1:1 채팅방 생성
    ChatRoomReadResponseDTO createPrivateRoom(String myUserId, String otherUserId);

    // 채팅방 ID를 기반으로 조회
    ChatRoomReadResponseDTO readById(String myUserId, Long roomId);
    // 멤버 ID를 기반으로 1:1 채팅방 조회
    ChatRoomReadResponseDTO readPrivateRoomByMemberId(String myUserId, String otherUserId);

    // 채팅방 알림 수신 여부 설정
    void setNotificationEnabled(String myUserId, Long roomId, boolean enabled);

    // 채팅방 초대
    List<UserProfileReadResponseDTO> invite(String myUserId, Long roomId, ChatRoomInviteRequestDTO chatRoomInviteRequestDTO);
    // 채팅방 나가기
    void leave(String myUserId, Long roomId);

    // 채팅방 멤버 여부 검사
    boolean isMember(Long chatRoomId, String userId);
}
