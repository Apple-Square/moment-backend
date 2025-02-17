package applesquare.moment.chat.service.impl;

import applesquare.moment.chat.dto.*;
import applesquare.moment.chat.model.ChatMessage;
import applesquare.moment.chat.model.ChatRoom;
import applesquare.moment.chat.model.ChatRoomMember;
import applesquare.moment.chat.model.ChatRoomType;
import applesquare.moment.chat.repository.ChatRoomMemberRepository;
import applesquare.moment.chat.repository.ChatRoomRepository;
import applesquare.moment.chat.service.ChatMessageService;
import applesquare.moment.chat.service.ChatRoomService;
import applesquare.moment.common.exception.DuplicateDataException;
import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.file.service.FileService;
import applesquare.moment.follow.repository.FollowRepository;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import applesquare.moment.user.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserInfoRepository userInfoRepository;
    private final FollowRepository followRepository;
    private final ChatMessageService chatMessageService;
    private final UserProfileService userProfileService;
    private final FileService fileService;


    /**
     * 특정 사용자의 채팅방 목록 검색
     * @param myUserId 사용자 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 채팅방 목록 페이지
     */
    @Override
    public PageResponseDTO<ChatRoomReadAllResponseDTO> search(String myUserId, PageRequestDTO pageRequestDTO){
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize= pageRequestDTO.getSize()+1;

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor= Long.parseLong(pageRequestDTO.getCursor());
        }

        // 채팅방 목록 검색
        List<ChatRoom> chatRooms=chatRoomRepository.search(myUserId, pageRequestDTO.getKeyword(), cursor, pageSize);

        // hasNext 설정
        boolean hasNext=false;
        if(chatRooms.size()>pageRequestDTO.getSize()){
            chatRooms.remove(chatRooms.size()-1);
            hasNext=true;
        }

        // DTO 변환
        List<ChatRoomReadAllResponseDTO> chatRoomDTOs=chatRooms.stream()
                .map((chatRoom)->{
                    // 최근 메시지 DTO 변환
                    ChatMessage lastMessage=chatRoom.getLastMessage();
                    ChatRoomMessageThumbnailDTO lastMessageThumbnailDTO=null;
                    if(lastMessage!=null){
                        if(lastMessage.isDeleted()){
                            // 삭제된 메시지라면 일부 정보만 포함해서 DTO 변환
                            lastMessageThumbnailDTO=ChatRoomMessageThumbnailDTO.builder()
                                    .regDate(lastMessage.getRegDate())
                                    .isDeleted(lastMessage.isDeleted())
                                    .build();
                        }
                        else{
                            // 삭제된 메시지가 아니라면 DTO 변환
                            lastMessageThumbnailDTO=ChatRoomMessageThumbnailDTO.builder()
                                    .regDate(lastMessage.getRegDate())
                                    .type(lastMessage.getType())
                                    .content(lastMessage.getContent())
                                    .isDeleted(lastMessage.isDeleted())
                                    .build();
                        }
                    }

                    // (채팅방 목록에서 각 채팅방 별로 보여지는) 썸네일 멤버 프로필 사진 URL 목록 생성하기
                    Set<ChatRoomMember> chatRoomMembers=chatRoom.getMembers();

                    // 주의 : 멤버 인원 수 계산할 때, Lazy Loading해서 가져온 멤버 리스트의 크기를 대입하면 안 된다.
                    //      => 이유 : Batch Size 10으로 설정해서 10개씩 끊어가져오기 때문이다.
                    long memberCount=chatRoomMemberRepository.countByChatRoom_Id(chatRoom.getId());
                    List<ChatRoomMember> memberList=new ArrayList<>(chatRoomMembers);
                    memberList=memberList.subList(0, Math.min(memberList.size(), MAX_MEMBER_PROFILE_IMAGE_COUNT));

                    List<String> memberProfileImageUrls=memberList.stream()
                            .map((chatRoomMember)->{
                                String profileName=chatRoomMember.getUser().getProfileImage().getFilename();
                                return fileService.convertFilenameToUrl(profileName);
                            }).collect(Collectors.toList());

                    // 채팅방 DTO 생성
                    return ChatRoomReadAllResponseDTO.builder()
                            .id(chatRoom.getId())
                            .memberCount(memberCount)
                            .memberProfileImageUrls(memberProfileImageUrls)
                            .lastMessage(lastMessageThumbnailDTO)
                            .build();
                }).toList();

        // 알림 페이지 반환
        return PageResponseDTO.<ChatRoomReadAllResponseDTO>builder()
                .content(chatRoomDTOs)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 채팅방 생성 (모든 유형의 채팅방 생성 가능)
     * @param myUserId 내 사용자 ID
     * @param chatRoomCreateRequestDTO 채팅방 생성 요청 정보
     * @return 채팅방 정보
     */
    @Override
    public ChatRoomReadResponseDTO create(String myUserId, ChatRoomCreateRequestDTO chatRoomCreateRequestDTO){
        ChatRoomType roomType=chatRoomCreateRequestDTO.getRoomType();

        // 유형에 따라 채팅방 생성
        if(roomType==ChatRoomType.PRIVATE){
            // 1:1 채팅방 생성
            Optional<String> otherUserIdOptional=chatRoomCreateRequestDTO.getMemberIds().stream()
                    .filter((memberId)-> !memberId.equals(myUserId))
                    .findFirst();

            // 채팅 멤버 ID 목록에서 상대방의 ID를 찾을 수 없다면, 에러 던지기
            if(otherUserIdOptional.isEmpty()){
                throw new IllegalArgumentException("채팅방의 멤버 목록을 입력해주세요.");
            }

            String otherUserId=otherUserIdOptional.get();
            return createPrivateRoom(myUserId, otherUserId);
        } else if (roomType==ChatRoomType.GROUP) {
            // 그룹 채팅방 생성
            return createGroupRoom(myUserId, chatRoomCreateRequestDTO);
        }
        else{
            throw new IllegalArgumentException("채팅방 유형을 입력해주세요.");
        }
    }

    /**
     * 특정 유저와의 1:1 채팅방 생성
     * @param myUserId 나의 사용자 ID
     * @param otherUserId 상대의 사용자 ID
     * @return 채팅방 정보
     */
    @Override
    public ChatRoomReadResponseDTO createPrivateRoom(String myUserId, String otherUserId){
        // 채팅 상대의 정보가 유효한지 확인
        if(otherUserId==null){
            throw new IllegalArgumentException("채팅하고 싶은 상대의 ID를 입력해주세요.");
        }
        // 존재하는 사용자인지 확인
        UserInfo otherUser=userInfoRepository.findById(otherUserId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다."));

        // 1:1 채팅방 존재 여부 검사
        Optional<ChatRoom> chatRoomOptional=chatRoomRepository.findPrivateChatRoomByMemberId(myUserId, otherUserId);

        // 이미 1:1 채팅방이 존재한다면, 에러 던지기
        if(chatRoomOptional.isPresent()){
            throw new DuplicateDataException("이미 해당 사용자와의 1:1 채팅방이 존재합니다.");
        }

        // 아직 1:1 채팅방이 존재하지 않는다면, 채팅방 생성
        UserInfo me=userInfoRepository.findById(myUserId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다."));

        ChatRoom newChatRoom=ChatRoom.builder()
                .roomName(otherUser.getNickname())
                .roomType(ChatRoomType.PRIVATE)
                .build();
        newChatRoom.addMember(me);
        newChatRoom.addMember(otherUser);

        ChatRoom savedChatRoom=chatRoomRepository.save(newChatRoom);

        // DTO 변환
        List<UserProfileReadResponseDTO> memberProfiles=List.of(
                userProfileService.toUserProfileDTO(me),
                userProfileService.toUserProfileDTO(otherUser)
        );
        ChatRoomReadResponseDTO chatRoomDTO=ChatRoomReadResponseDTO.builder()
                .id(savedChatRoom.getId())
                .memberProfiles(memberProfiles)
                .build();

        // DTO 반환
        return chatRoomDTO;
    }

    /**
     * 그룹 채팅방 생성
     * (권한 : 본인과 맞팔로우된 사람만 그룹 채팅방에 초대 가능)
     * @param myUserId 사용자 ID
     * @param chatRoomCreateRequestDTO 채팅방 생성 요청 정보
     * @return 채팅방 정보
     */
    private ChatRoomReadResponseDTO createGroupRoom(String myUserId, ChatRoomCreateRequestDTO chatRoomCreateRequestDTO){
        // 멤버 목록에서 나 자신을 제외
        List<String> memberIds=chatRoomCreateRequestDTO.getMemberIds();
        memberIds=memberIds.stream()
                .filter((memberId)->!memberId.equals(myUserId))
                .collect(Collectors.toList());

        // 멤버 인원수 검사
        int totalMemberCount=memberIds.size()+1;
        if(totalMemberCount < MIN_MEMBER_COUNT){
            throw new IllegalArgumentException("최소 "+MIN_MEMBER_COUNT+"명의 멤버가 필요합니다.");
        }
        if(totalMemberCount > MAX_MEMBER_COUNT){
           throw new IllegalArgumentException("최대 "+MAX_MEMBER_COUNT+"명까지만 수용 가능합니다.");
        }

        // 멤버들이 모두 존재하는 사용자인지 확인
        List<UserInfo> members=memberIds.stream()
                .map((memberId)->userInfoRepository.findById(memberId)
                        .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다.")))
                .toList();

        UserInfo me=userInfoRepository.findById(myUserId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다."));

        // 권한 검사 : 멤버들이 모두 나와 맞팔로우 상태인지 확인
        boolean isFollowAll=followRepository.isAllFollow(myUserId, memberIds);  // 내가 모두를 팔로우 중인지 여부
        boolean isFollowedAll=followRepository.isAllFollowed(myUserId, memberIds);  // 모두가 나를 팔로우 중인지 여부
        if(!isFollowAll || !isFollowedAll){
            throw new AccessDeniedException("그룹 채팅 초대는 상호 팔로우된 사용자만 가능합니다.");
        }

        // 채팅방 이름 생성
        String roomNameInput= chatRoomCreateRequestDTO.getRoomName();
        String chatRoomName;
        if(roomNameInput!=null && !roomNameInput.isBlank()){
            // 채팅방 이름이 입력으로 들어왔으면, 그대로 사용
            chatRoomName=roomNameInput;
        }
        else{
            // 채팅방 이름이 입력으로 들어오지 않았으면, 기본 값 생성
            chatRoomName=me.getNickname();

            int memberCount= members.size();
            if(memberCount+1<=3){
                // 전체 멤버가 3명 이하이면, 모든 멤버의 이름 표시
                for(int i=0;i<2&&i<memberCount;i++){
                    chatRoomName+=", "+members.get(i).getNickname();
                }
            }
            else{
                // 전체 멤버가 4명 이상이면, 나 포함 2명까지만 이름 표시
                chatRoomName+=", "+members.get(0).getNickname();
                chatRoomName+=" 외 "+(memberCount-1)+"명";
            }
        }

        // 그룹 채팅방 생성
        ChatRoom newChatRoom=ChatRoom.builder()
                .roomName(chatRoomName)
                .roomType(ChatRoomType.GROUP)
                .build();
        newChatRoom.addMember(me);
        members.forEach((member)-> newChatRoom.addMember(member));

        ChatRoom savedChatRoom=chatRoomRepository.save(newChatRoom);

        // DTO 변환
        List<UserProfileReadResponseDTO> memberProfiles=members.stream()
                .map((member)-> userProfileService.toUserProfileDTO(member))
                .collect(Collectors.toList());
        memberProfiles.add(userProfileService.toUserProfileDTO(me));  // 멤버 프로필 목록에 내 프로필도 추가

        ChatRoomReadResponseDTO chatRoomDTO=ChatRoomReadResponseDTO.builder()
                .id(savedChatRoom.getId())
                .memberProfiles(memberProfiles)
                .build();

        // DTO 반환
        return chatRoomDTO;
    }

    /**
     * 멤버 ID를 기반으로 1:1 채팅방 조회
     * @param myUserId 나의 사용자 ID
     * @param otherUserId 상대의 사용자 ID
     * @return 채팅방 정보
     */
    @Override
    public ChatRoomReadResponseDTO readPrivateRoomByMemberId(String myUserId, String otherUserId){
        // 멤버 ID를 기반으로 1:1 채팅방 조회
        Optional<ChatRoom> chatRoomOptional=chatRoomRepository.findPrivateChatRoomByMemberId(myUserId, otherUserId);

        // 1:1 채팅방이 존재한다면, DTO 변환
        ChatRoomReadResponseDTO privateRoomDTO=null;
        if(chatRoomOptional.isPresent()){
            ChatRoom chatRoom=chatRoomOptional.get();
            Set<ChatRoomMember> chatRoomMembers=chatRoom.getMembers();
            List<UserProfileReadResponseDTO> memberProfiles=chatRoomMembers.stream()
                    .map((chatRoomMember) -> userProfileService.toUserProfileDTO(chatRoomMember.getUser()))
                    .collect(Collectors.toList());
            privateRoomDTO=ChatRoomReadResponseDTO.builder()
                    .id(chatRoom.getId())
                    .memberProfiles(memberProfiles)
                    .build();
        }

        return privateRoomDTO;
    }

    /**
     * 채팅방 ID를 기반으로 채팅방 조회
     * @param myUserId 사용자 ID
     * @param roomId 채팅방 ID
     * @return 특정 채팅방 정보
     */
    @Override
    public ChatRoomReadResponseDTO readById(String myUserId, Long roomId){
        // 채팅방 조회
        ChatRoom chatRoom=chatRoomRepository.findById(roomId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 채팅방입니다."));

        // 권한 검사 : 내가 채팅방의 멤버인지 확인
        if(!isMember(roomId, myUserId)){
            throw new AccessDeniedException("채팅방 멤버만 조회할 수 있습니다.");
        }

        // DTO 변환
        Set<ChatRoomMember> chatRoomMembers=chatRoom.getMembers();
        List<UserProfileReadResponseDTO> memberProfiles=chatRoomMembers.stream()
                .map((chatRoomMember)->userProfileService.toUserProfileDTO(chatRoomMember.getUser()))
                .collect(Collectors.toList());

        ChatRoomReadResponseDTO chatRoomDTO=ChatRoomReadResponseDTO.builder()
                .id(chatRoom.getId())
                .memberProfiles(memberProfiles)
                .build();

        // DTO 반환
        return chatRoomDTO;
    }

    /**
     * 특정 채팅방의 멤버 목록 조회
     * @param myUserId 사용자 ID
     * @param roomId 채팅방 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 채팅방 멤버 프로필 페이지
     */
    @Override
    public PageResponseDTO<UserProfileReadResponseDTO> readMemberProfileAllByRoomId(String myUserId, Long roomId, PageRequestDTO pageRequestDTO){
        // 권한 검사 : 내가 채팅방의 멤버인지 확인
        if(!isMember(roomId, myUserId)){
            throw new AccessDeniedException("채팅방 멤버만 조회할 수 있습니다.");
        }

        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize= pageRequestDTO.getSize()+1;
        Sort sort= Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable= PageRequest.of(0, pageSize, sort);

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor= Long.parseLong(pageRequestDTO.getCursor());
        }

        // 채팅방 멤버 목록 검색 (나 자신은 목록에서 제외하고 조회)
        List<ChatRoomMember> chatRoomMembers=chatRoomMemberRepository.findAllByRoomId(myUserId, roomId, cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(chatRoomMembers.size()>pageRequestDTO.getSize()){
            chatRoomMembers.remove(chatRoomMembers.size()-1);
            hasNext=true;
        }

        // DTO 변환
        List<UserProfileReadResponseDTO> memberProfileDTOs=chatRoomMembers.stream()
                .map((chatRoomMember)-> userProfileService.toUserProfileDTO(chatRoomMember.getUser()))
                .collect(Collectors.toList());

        // 채팅방 멤버 페이지 반환
        return PageResponseDTO.<UserProfileReadResponseDTO>builder()
                .content(memberProfileDTOs)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 채팅방 초대
     * @param myUserId 나의 사용자 ID
     * @param roomId 채팅방 ID
     * @param chatRoomInviteRequestDTO 채팅방 초대 요청 정보
     * @return 초대된 멤버의 프로필 목록
     */
    @Override
    public List<UserProfileReadResponseDTO> invite(String myUserId, Long roomId, ChatRoomInviteRequestDTO chatRoomInviteRequestDTO){
        // 채팅방 권한 검사 : 1:1 채팅방은 멤버 초대 불가
        ChatRoom chatRoom=chatRoomRepository.findById(roomId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        if(chatRoom.getRoomType()==ChatRoomType.PRIVATE){
            throw new IllegalArgumentException("1:1 채팅방에는 새로운 멤버를 초대할 수 없습니다.");
        }

        // 존재하는 사용자인지 검사
        List<String> memberIds=chatRoomInviteRequestDTO.getMemberIds();
        List<UserInfo> members=userInfoRepository.findAllById(memberIds);
        if(members.size()!=memberIds.size()){
            List<String> foundUserIds=members.stream()
                    .map((userInfo)-> userInfo.getId())
                    .toList();
            Optional<String> notFoundUserIdOptional=memberIds.stream()
                    .filter((element)-> !foundUserIds.contains(element))
                    .findFirst();

            if(notFoundUserIdOptional.isPresent()){
                throw new IllegalArgumentException("존재하지 않는 사용자입니다. (id = "+notFoundUserIdOptional.get()+")");
            }
        }

        // 사용자 권한 검사 : 상호 팔로우 여부 확인
        List<String> mutualFollowers=followRepository.findMutualFollowersInUserIds(myUserId, memberIds);
        if(mutualFollowers.size()!=memberIds.size()){
            Optional<String> notMutualFollowerIdOptional=memberIds.stream()
                    .filter((element)-> !mutualFollowers.contains(element))
                    .findFirst();

            if(notMutualFollowerIdOptional.isPresent()){
                throw new IllegalArgumentException("상호 팔로워만 초대할 수 있습니다. (id = "+notMutualFollowerIdOptional.get()+")");
            }
        }

        // 각 멤버를 채팅방에 초대
        members.forEach((member) -> chatRoom.addMember(member));
        chatRoomRepository.save(chatRoom);

        // 초대된 채팅방 멤버 프로필 생성
        List<UserProfileReadResponseDTO> invitedMemberProfiles=members.stream()
                .map(member-> userProfileService.toUserProfileDTO(member))
                .collect(Collectors.toList());

        // 초대된 채팅방 멤버 프로필 반환
        return invitedMemberProfiles;
    }

    // 채팅방 나가기
    @Override
    public void leave(String myUserId, Long roomId){
        // 채팅방 권한 검사 : 1:1 채팅방은 나가기 불가
        ChatRoom chatRoom=chatRoomRepository.findById(roomId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
        if(chatRoom.getRoomType()==ChatRoomType.PRIVATE){
            throw new IllegalArgumentException("1:1 채팅방은 나갈 수 없습니다.");
        }

        // 사용자 권한 검사 : 채팅방 멤버 여부 확인
        if(!isMember(roomId, myUserId)){
            throw new AccessDeniedException("채팅방 멤버만 나갈 수 있습니다.");
        }

        // 채팅방 나가기
        // 채팅방 멤버 삭제 (ChatRoomMember)
        Set<ChatRoomMember> members=chatRoom.getMembers();
        chatRoom.removeMember(myUserId);
        chatRoomRepository.save(chatRoom);

        // 남은 멤버가 0명이라면, 채팅방도 삭제 (ChatRoom)
        if(members.size()==0){
            deleteChatRoom(roomId);
        }
    }

    /**
     * 채팅방 삭제하기 (채팅방 멤버가 0명인 경우 실행)
     * @param roomId 채팅방 ID
     */
    private void deleteChatRoom(Long roomId){
        // 채팅 메시지 일괄 삭제 (ChatMessage)
        chatMessageService.deleteBatchByRoomId(roomId);

        // 채팅방 삭제하기 (ChatRoom)
        chatRoomRepository.deleteById(roomId);
    }

    /**
     * 특정 사용자가 채팅방의 멤버인지 검사
     * @param chatRoomID 채팅방 ID
     * @param userId 사용자 ID
     * @return 채팅방의 멤버인지 여부
     */
    @Override
    public boolean isMember(Long chatRoomID, String userId){
        return chatRoomMemberRepository.existsByChatRoom_IdAndUser_Id(chatRoomID, userId);
    }
}
