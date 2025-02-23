package applesquare.moment.chat.service.impl;

import applesquare.moment.chat.repository.ChatMessageRepository;
import applesquare.moment.chat.service.ChatMessageDeleteService;
import applesquare.moment.file.model.FileAccessGroupType;
import applesquare.moment.file.repository.StorageFileRepository;
import applesquare.moment.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageDeleteServiceImpl implements ChatMessageDeleteService {
    private final ChatMessageRepository chatMessageRepository;
    private final StorageFileRepository storageFileRepository;
    private final FileService fileService;


    /**
     * 채팅방 ID 기반으로 채팅 메시지 일괄 삭제
     * @param roomId 채팅방 ID
     */
    @Async("taskExecutor")
    @Override
    public void deleteBatchByRoomId(Long roomId){
        // DB에서 특정 채팅방의 StorageFile 목록 제거
        storageFileRepository.deleteByGroupTypeAndGroupId(FileAccessGroupType.CHAT, roomId.toString());

        // 채팅 메시지에 사용된 파일을 저장소와 DB에서 삭제하기
        int batchSize=1000;
        while(true){
            // 채팅 메시지에서 사용된 파일명 목록 조회
            Pageable pageable= PageRequest.of(0, batchSize);
            List<String> chatMessageFilenames=chatMessageRepository.findFilenamesByChatRoomId(roomId, pageable);
            if(chatMessageFilenames.size()==0){
                break;
            }

            // 저장소에서 파일 목록 삭제
            fileService.deleteByFilenames(chatMessageFilenames);
        }

        // 채팅방 메시지 삭제하기 (ChatMessage)
        chatMessageRepository.deleteByChatRoom_Id(roomId);
    }
}
