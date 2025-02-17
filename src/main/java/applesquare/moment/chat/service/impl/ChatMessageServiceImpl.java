package applesquare.moment.chat.service.impl;

import applesquare.moment.chat.dto.*;
import applesquare.moment.chat.model.ChatMessage;
import applesquare.moment.chat.model.ChatMessageType;
import applesquare.moment.chat.model.ChatRoomMember;
import applesquare.moment.chat.repository.ChatMessageRepository;
import applesquare.moment.chat.repository.ChatRoomMemberRepository;
import applesquare.moment.chat.service.ChatMessageService;
import applesquare.moment.chat.service.ChatRoomService;
import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.file.model.MediaType;
import applesquare.moment.file.model.StorageFile;
import applesquare.moment.file.service.FileService;
import applesquare.moment.post.model.Post;
import applesquare.moment.post.repository.PostRepository;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.user.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final PostRepository postRepository;
    private final UserProfileService userProfileService;
    private final ChatRoomService chatRoomService;
    private final FileService fileService;
    private final ModelMapper modelMapper;


    /**
     * 특정 채팅방의 메시지 목록 조회
     * @param myUserId 사용자 ID
     * @param roomId 채팅방 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 메시지 목록 페이지
     */
    @Override
    public PageResponseDTO<ChatMessageReadResponseDTO> readAll(String myUserId, Long roomId, PageRequestDTO pageRequestDTO){
        // 권한 검사 : 사용자가 해당 채팅방의 멤버가 맞는지 확인
        if(!chatRoomService.isMember(roomId, myUserId)){
            throw new AccessDeniedException("채팅방의 멤버만 조회할 수 있습니다.");
        }

        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize= pageRequestDTO.getSize()+1;
        Sort sort= Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable= PageRequest.of(0, pageSize, sort);

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor= Long.parseLong(pageRequestDTO.getCursor());
        }

        // 채팅방 목록 조회 (isDeleted가 true인 것도 가져온 후 DTO에서 가공)
        List<ChatMessage> chatMessages=chatMessageRepository.findChatMessageAllByRoomId(roomId, cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(chatMessages.size()>pageRequestDTO.getSize()){
            chatMessages.remove(chatMessages.size()-1);
            hasNext=true;
        }

        // DTO 변환
        List<ChatMessageReadResponseDTO> chatMessageDTOs=chatMessages.stream()
                .map((chatMessage)->{
                    // 메시지 삭제 여부 확인
                    if(chatMessage.isDeleted()){
                        // 삭제된 메시지라면 필드를 일부만 채워서 반환
                        return ChatMessageReadResponseDTO.builder()
                                .id(chatMessage.getId())
                                .regDate(chatMessage.getRegDate())
                                .senderId(chatMessage.getSenderId())
                                .unreadCount(chatMessage.getUnreadCount())
                                .isDeleted(chatMessage.isDeleted())
                                .build();
                    }
                    else{
                        // 삭제되지 않은 메시지라면 DTO 변환해서 반환

                        // 게시물 공유 메시지라면, 게시물 공유 정보를 DTO로 변환
                        SharedPostReadResponseDTO sharedPostDTO=null;
                        if(chatMessage.getType()== ChatMessageType.POST_SHARE){
                            // 공유하려는 게시물 정보 조회
                            Post sharedPost=chatMessage.getSharedPost();

                            // 게시물 작성자 프로필 DTO 생성
                            UserProfileReadResponseDTO userProfileDTO=userProfileService.toUserProfileDTO(sharedPost.getWriter());

                            // 게시물 썸네일 URL 생성
                            StorageFile firstFile= postRepository.findFirstFileById(sharedPost.getId());
                            String thumbFilename=fileService.convertFilenameToThumbFilename(firstFile.getFilename());
                            String thumbFileUrl=fileService.convertFilenameToUrl(thumbFilename);

                            // 공유하려는 게시물의 미디어 타입에 따라서 DTO 생성
                            MediaType mediaType= FileService.convertContentTypeToMediaType(firstFile.getContentType());
                            if(mediaType==MediaType.IMAGE){
                                sharedPostDTO= SharedImagePostReadResponseDTO.builder()
                                        .id(sharedPost.getId())
                                        .writer(userProfileDTO)
                                        .mediaType(mediaType)
                                        .thumbnailUrl(thumbFileUrl)
                                        .content(sharedPost.getContent())
                                        .build();
                            } else if(mediaType==MediaType.VIDEO){
                                sharedPostDTO= SharedVideoPostReadResponseDTO.builder()
                                        .id(sharedPost.getId())
                                        .writer(userProfileDTO)
                                        .mediaType(mediaType)
                                        .thumbnailUrl(thumbFileUrl)
                                        .build();
                            }
                        }

                        // 파일 URL 목록 생성
                        List<StorageFile> files=chatMessage.getFiles();
                        List<String> fileUrls=files.stream()
                                .map((file)-> fileService.convertFilenameToUrl(file.getFilename()))
                                .toList();

                        // 채팅 메시지 DTO 구성
                        ChatMessageReadResponseDTO dto=modelMapper.map(chatMessage, ChatMessageReadResponseDTO.class);
                        return dto.toBuilder()
                                .fileUrls(fileUrls)
                                .sharedPost(sharedPostDTO)
                                .build();

                    }
                })
                .toList();

        // 알림 페이지 반환
        return PageResponseDTO.<ChatMessageReadResponseDTO>builder()
                .content(chatMessageDTOs)
                .hasNext(hasNext)
                .build();
    }

    // 특정 채팅방에 메시지 생성 & 전송
    @Override
    public ChatMessageReadResponseDTO createAndSend(String myUserId, Long roomId, ChatMessageCreateRequestDTO chatMessageCreateRequestDTO){
        // TO DO : 구현 예정
        return null;
    }

    /**
     * 특정 메시지 삭제 (소프트 삭제)
     * @param myUserId 사용자 ID
     * @param messageId 메시지 ID
     */
    @Override
    public void setAsDelete(String myUserId, Long messageId){
        // 메시지 조회
        ChatMessage chatMessage=chatMessageRepository.findById(messageId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 메시지입니다."));

        // 권한 검사 : 해당 메시지의 송신자인지 확인
        if(!chatMessage.getSenderId().equals(myUserId)){
            throw new AccessDeniedException("메시지를 보낸 사람만 삭제할 수 있습니다.");
        }

        chatMessageRepository.markMessageAsDelete(messageId);
    }

    /**
     * 특정 메시지까지 읽기 (해당 메시지 포함 이전에 전송됐던 '다른 사람이 작성한' 메시지의 un
     * => (messageId)번 포함, 그 이전에 있는 메시지는 모두 읽은 것으로 처리
     * @param myUserId 사용자 ID
     * @param messageId 메시지 ID
     */
    @Override
    public void setAsRead(String myUserId, Long messageId){
        // 메시지 조회
        ChatMessage chatMessage=chatMessageRepository.findById(messageId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 메시지입니다."));
        Long roomId=chatMessage.getChatRoom().getId();

        // 권한 검사 : 사용자가 해당 채팅방의 멤버가 맞는지 확인
        Optional<ChatRoomMember> memberOptional=chatRoomMemberRepository.findByChatRoom_IdAndUser_Id(roomId, myUserId);
        if(memberOptional.isEmpty()){
            throw new AccessDeniedException("채팅방의 멤버만 읽을 수 있습니다.");
        }

        // 메시지 읽음 처리
        ChatRoomMember member=memberOptional.get();
        chatMessageRepository.markMessagesAsRead(myUserId, roomId, messageId, member.getLastReadMessageId());

        // 가장 최근에 읽은 메시지 ID 갱신
        ChatRoomMember newChatRoomMember=member.toBuilder()
                .lastReadMessageId(messageId)
                .build();
        chatRoomMemberRepository.save(newChatRoomMember);
    }

    /**
     * 채팅방 ID 기반으로 채팅 메시지 일괄 삭제
     * @param roomId 채팅방 ID
     */
    @Async("taskExecutor")
    @Override
    public void deleteBatchByRoomId(Long roomId){

        // 채팅 메시지에 사용된 파일을 저장소와 DB에서 삭제하기
        int batchSize=1000;
        while(true){
            // 채팅 메시지에서 사용된 파일명 목록 조회
            Pageable pageable=PageRequest.of(0, batchSize);
            List<String> chatMessageFilenames=chatMessageRepository.findFilenamesByChatRoomId(roomId, pageable);
            if(chatMessageFilenames.size()==0){
                break;
            }

            // 저장소, DB에서 파일 목록 삭제
            fileService.deleteByFilenames(chatMessageFilenames);
        }

        // 채팅방 메시지 삭제하기 (ChatMessage)
        chatMessageRepository.deleteByChatRoom_Id(roomId);
    }
}
