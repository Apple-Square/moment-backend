package applesquare.moment.chat.service.impl;

import applesquare.moment.chat.dto.*;
import applesquare.moment.chat.model.ChatMessage;
import applesquare.moment.chat.model.ChatMessageType;
import applesquare.moment.chat.model.ChatRoom;
import applesquare.moment.chat.model.ChatRoomMember;
import applesquare.moment.chat.repository.ChatMessageRepository;
import applesquare.moment.chat.repository.ChatRoomMemberRepository;
import applesquare.moment.chat.repository.ChatRoomRepository;
import applesquare.moment.chat.service.ChatMessageService;
import applesquare.moment.chat.service.ChatRoomService;
import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.file.model.FileAccessGroupType;
import applesquare.moment.file.model.FileAccessPolicy;
import applesquare.moment.file.model.MediaType;
import applesquare.moment.file.model.StorageFile;
import applesquare.moment.file.repository.StorageFileRepository;
import applesquare.moment.file.service.FileService;
import applesquare.moment.notification.dto.NotificationRequestDTO;
import applesquare.moment.notification.model.NotificationType;
import applesquare.moment.notification.service.NotificationSendService;
import applesquare.moment.post.model.Post;
import applesquare.moment.post.repository.PostRepository;
import applesquare.moment.post.service.PostReadService;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import applesquare.moment.user.service.UserProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final StorageFileRepository storageFileRepository;
    private final UserInfoRepository userInfoRepository;
    private final PostRepository postRepository;
    private final UserProfileService userProfileService;
    private final ChatRoomService chatRoomService;
    private final FileService fileService;
    private final PostReadService postReadService;
    private final NotificationSendService notificationSendService;
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
            throw new AccessDeniedException("채팅방의 멤버만 메시지를 조회할 수 있습니다.");
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

    /**
     * 특정 채팅방에 메시지 생성 & 전송
     * @param myUserId 나의 사용자 ID
     * @param roomId 채팅방 ID
     * @param chatMessageCreateRequestDTO 채팅 메시지 생성 요청 정보
     * @return 전송한 메시지 정보
     */
    @Override
    public ChatMessageReadResponseDTO createAndSend(String myUserId, Long roomId, ChatMessageCreateRequestDTO chatMessageCreateRequestDTO){
        // 송신자가 채팅방의 멤버인지 확인하기
        if(!chatRoomService.isMember(roomId, myUserId)){
            throw new AccessDeniedException("채팅방의 멤버만 메시지를 전송할 수 있습니다.");
        }

        // 채팅방 정보 가져오기
        ChatRoom chatRoom=chatRoomRepository.findById(roomId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 채팅방입니다."));

        // 채팅방 인원 조회
        long chatRoomMemberCount=chatRoomMemberRepository.countByChatRoom_Id(roomId);

        // ChatMessage 엔티티 생성하기
        Long postId=null;
        Post sharedPost=null;
        List<StorageFile> storageFiles=new ArrayList<>();

        ChatMessageType messageType=chatMessageCreateRequestDTO.getType();
        ChatMessage chatMessage=switch (messageType){
            case TEXT -> ChatMessage.builder()
                    .chatRoom(chatRoom)
                    .senderId(myUserId)
                    .type(ChatMessageType.TEXT)
                    .content(chatMessageCreateRequestDTO.getContent())
                    .unreadCount(chatRoomMemberCount)
                    .build();
            case IMAGE, VIDEO-> {
                List<Long> storageFileIds=chatMessageCreateRequestDTO.getFileIds();
                if(storageFileIds.isEmpty()){
                    throw new IllegalArgumentException("파일을 1개 이상 입력해주세요.");
                }
                storageFiles=storageFileRepository.findAllById(storageFileIds);
                for(StorageFile storageFile:storageFiles){
                    if(!storageFile.getUploader().getId().equals(myUserId)){
                        throw new AccessDeniedException("해당 파일은 업로더만 메시지로 전송할 수 있습니다.");
                    }
                }
                yield ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .senderId(myUserId)
                        .type(messageType)
                        .files(storageFiles)
                        .unreadCount(chatRoomMemberCount)
                        .build();
            }
            case POST_SHARE -> {
                postId=chatMessageCreateRequestDTO.getPostShareId();
                if(postId==null){
                    throw new IllegalArgumentException("공유할 게시물 ID를 입력해주세요.");
                }
                sharedPost=postRepository.findById(postId)
                        .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 게시물입니다."));
                yield ChatMessage.builder()
                        .chatRoom(chatRoom)
                        .senderId(myUserId)
                        .type(messageType)
                        .sharedPost(sharedPost)
                        .unreadCount(chatRoomMemberCount)
                        .build();
            }
        };

        // ChatMessage 엔티티 DB 저장
        ChatMessage savedChatMessage=chatMessageRepository.save(chatMessage);

        // 채팅방의 최신 메시지 갱신하기 (이걸 해야 채팅방이 목록에서 위로 올라감)
        ChatRoom newChatRoom=chatRoom.toBuilder()
                .lastMessage(savedChatMessage)
                .build();
        chatRoomRepository.save(newChatRoom);

        // 채팅 DTO 변환
        ChatMessageReadResponseDTO chatMessageDTO=switch (messageType){
            case TEXT -> modelMapper.map(savedChatMessage, ChatMessageReadResponseDTO.class);
            case IMAGE, VIDEO -> {
                // 파일 URL 설정
                List<String> fileUrls=storageFiles.stream()
                        .map((storageFile)-> fileService.convertFilenameToUrl(storageFile.getFilename()))
                        .toList();

                // DTO 변환
                yield modelMapper
                        .map(savedChatMessage, ChatMessageReadResponseDTO.class)
                        .toBuilder()
                        .fileUrls(fileUrls)
                        .build();
            }
            case POST_SHARE -> {
                SharedPostReadResponseDTO sharedPostDTO=null;
                if(sharedPost!=null){
                    // 게시물의 미디어 타입 가져오기
                    String contentType= postRepository.findContentTypeByPostId(postId)
                            .orElseThrow(()-> new RuntimeException("게시물의 contentType 조회에 실패했습니다."));
                    MediaType mediaType=FileService.convertContentTypeToMediaType(contentType);

                    // 게시물 작성자 프로필 가져오기
                    UserProfileReadResponseDTO writerProfileDTO=userProfileService.readProfileById(sharedPost.getWriter().getId());

                    // 게시물 썸네일 URL 가져오기
                    String thumbFileUrl= postReadService.readThumbnailFileUrl(postId);

                    if(mediaType==MediaType.IMAGE){
                        sharedPostDTO=SharedImagePostReadResponseDTO.builder()
                                .id(sharedPost.getId())
                                .writer(writerProfileDTO)
                                .mediaType(mediaType)
                                .thumbnailUrl(thumbFileUrl)
                                .content(sharedPost.getContent())
                                .build();
                    }
                    else if(mediaType==MediaType.VIDEO){
                        sharedPostDTO=SharedVideoPostReadResponseDTO.builder()
                                .id(sharedPost.getId())
                                .writer(writerProfileDTO)
                                .mediaType(mediaType)
                                .thumbnailUrl(thumbFileUrl)
                                .build();
                    }
                }

                yield modelMapper
                        .map(savedChatMessage, ChatMessageReadResponseDTO.class)
                        .toBuilder()
                        .sharedPost(sharedPostDTO)
                        .build();
            }
        };

        // 채팅방 멤버들에게 채팅 알림 전송
        UserInfo myUserInfo=userInfoRepository.findById(myUserId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다."));
        NotificationRequestDTO<ChatMessage> notificationRequestDTO=NotificationRequestDTO.<ChatMessage>builder()
                .type(NotificationType.CHAT)
                .sender(myUserInfo)  // 송신자 == 채팅 메시지 보낸 사람
                .referenceId(roomId.toString())  // 래퍼런스 ID == 채팅방 ID
                .referenceObject(savedChatMessage)  // 래퍼런스 객체 == 채팅 메시지
                .build();

        notificationSendService.notify(notificationRequestDTO);

        // 채팅 DTO 반환
        return chatMessageDTO;
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
     * 특정 채팅방에 파일 업로드
     * @param myUserId 나의 사용자 ID
     * @param roomId 채팅방 ID
     * @param files 업로드할 파일 목록
     * @return 업로드한 파일 목록
     */
    @Override
    public List<Long> uploadFiles(String myUserId, Long roomId, List<MultipartFile> files){
        if(files.isEmpty()){
            throw new IllegalArgumentException("파일을 1개 이상 입력해주세요.");
        }

        // 권한 검사 : 사용자가 해당 채팅방의 멤버가 맞는지 확인
        if(!chatRoomService.isMember(roomId, myUserId)){
            throw new AccessDeniedException("채팅방의 멤버만 파일을 업로드할 수 있습니다.");
        }

        // 채팅방에 파일 업로드하기
        List<StorageFile> storageFiles=new LinkedList<>();
        try {
            // 나의 정보 찾기
            UserInfo myUserInfo=userInfoRepository.findById(myUserId)
                    .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다."));

            // 저장소에 파일 목록 저장
            for(MultipartFile file : files){
                StorageFile storageFile = fileService.upload(file, myUserInfo, FileAccessPolicy.GROUP, FileAccessGroupType.CHAT, roomId.toString());
                storageFiles.add(storageFile);
            }

            List<Long> storageFileIds=storageFiles.stream()
                    .map((storageFile)-> storageFile.getId())
                    .toList();

            // 업로드된 파일 목록 반환
            return storageFileIds;

        } catch (Exception exception){
            // 파일을 업로드하던 도중 문제가 생긴다면, 저장소에 업로드한 파일을 삭제해야 한다.
            log.error(exception.getMessage());
            try{
                // 저장소에 업로드한 파일 삭제
                for(StorageFile storageFile : storageFiles){
                    String uploadedFilename=storageFile.getFilename();
                    fileService.delete(uploadedFilename);
                }
            } catch (IOException ioException){
                // 만약 삭제하는 것도 실패했다면, 로그를 남긴다.
                log.error(ioException.getMessage());
            }

            throw new RuntimeException("파일 업로드에 실패했습니다.");
        }
    }
}
