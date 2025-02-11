package applesquare.moment.post.service.impl;

import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.file.service.FileService;
import applesquare.moment.post.dto.MomentDetailReadAllResponseDTO;
import applesquare.moment.post.dto.MomentThumbnailReadAllResponseDTO;
import applesquare.moment.post.model.Post;
import applesquare.moment.post.repository.PostRepository;
import applesquare.moment.post.service.MomentReadService;
import applesquare.moment.post.service.PostReadSupport;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MomentReadServiceImpl implements MomentReadService {
    private final PostReadSupport postReadSupport;
    private final PostRepository postRepository;
    private final FileService fileService;


    /**
     * 모먼트 세부사항 목록 조회 (커서 페이징)
     * @param pageRequestDTO 페이지 요청 정보
     * @return 모먼트 세부사항 목록
     */
    @Override
    public PageResponseDTO<MomentDetailReadAllResponseDTO> readDetailAll(PageRequestDTO pageRequestDTO) {
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize = pageRequestDTO.getSize() + 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(0, pageSize, sort);

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor = Long.parseLong(pageRequestDTO.getCursor());
        }

        // 모먼츠 목록 조회
        List<Post> posts = postRepository.findMomentAll(cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(posts.size()>pageRequestDTO.getSize()){
            posts.remove(posts.size()-1);
            hasNext=true;
        }

        // 조회된 모먼츠 목록을 기반으로, 모먼츠 정보 이외에 댓글, 좋아요, 팔로우 정보 가져오기
        List<MomentDetailReadAllResponseDTO> momentDetailReadAllResponseDTOS=postReadSupport.readMomentDetailAllByPosts(posts);

        // 게시글 세부사항 페이지 반환
        return PageResponseDTO.<MomentDetailReadAllResponseDTO>builder()
                .content(momentDetailReadAllResponseDTOS)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 특정 유저가 작성한 모먼트 세부사항 목록 조회 (커서 페이징)
     * @param userId 작성자 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 모먼트 세부사항 목록
     */
    @Override
    public PageResponseDTO<MomentDetailReadAllResponseDTO> readDetailAllByUser(String userId, PageRequestDTO pageRequestDTO) {
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize = pageRequestDTO.getSize() + 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(0, pageSize, sort);

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor = Long.parseLong(pageRequestDTO.getCursor());
        }

        // 특정 유저가 작성한 모먼츠 목록 조회
        List<Post> posts=postRepository.findMomentAllByWriterId(userId, cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(posts.size()>pageRequestDTO.getSize()){
            posts.remove(posts.size()-1);
            hasNext=true;
        }

        // 조회된 모먼츠 목록을 기반으로, 모먼츠 정보 이외에 댓글, 좋아요, 팔로우 정보 가져오기
        List<MomentDetailReadAllResponseDTO> momentDetailReadAllResponseDTOS=postReadSupport.readMomentDetailAllByPosts(posts);

        // 게시글 세부사항 페이지 반환
        return PageResponseDTO.<MomentDetailReadAllResponseDTO>builder()
                .content(momentDetailReadAllResponseDTOS)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 특정 유저가 작성한 모먼트 썸네일 목록 조회 (커서 페이징)
     * @param userId 작성자 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 모먼트 썸네일 목록
     */
    @Override
    public PageResponseDTO<MomentThumbnailReadAllResponseDTO> readThumbnailAllByUser(String userId, PageRequestDTO pageRequestDTO) {
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize = pageRequestDTO.getSize() + 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(0, pageSize, sort);

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor = Long.parseLong(pageRequestDTO.getCursor());
        }

        // 특정 유저가 작성한 모먼츠 썸네일 목록 조회
        List<Tuple> tuples = postRepository.findMomentAllWithFirstFileByWriterId(userId, cursor, pageable);

        // hasNext 설정
        boolean hasNext = false;
        if (tuples.size() > pageRequestDTO.getSize()) {
            tuples.remove(tuples.size() - 1);
            hasNext = true;
        }

        // DTO 변환
        List<MomentThumbnailReadAllResponseDTO> momentThumbnailReadAllResponseDTOS = tuples.stream().map((tuple) -> {
            String thumbFilename = fileService.convertFilenameToThumbFilename((String) tuple.get("filename"));
            return MomentThumbnailReadAllResponseDTO.builder()
                    .id((Long) tuple.get("postId"))
                    .url(fileService.convertFilenameToUrl(thumbFilename))
                    .viewCount((long) tuple.get("viewCount"))
                    .build();
        }).toList();

        // 게시글 세부사항 페이지 반환
        return PageResponseDTO.<MomentThumbnailReadAllResponseDTO>builder()
                .content(momentThumbnailReadAllResponseDTOS)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 특정 유저가 좋아요 누른 모먼트 세부사항 목록 조회 (커서 페이징)
     * @param userId 사용자 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 모먼트 세부사항 목록
     */
    @Override
    public PageResponseDTO<MomentDetailReadAllResponseDTO> readLikedDetailAllByUser(String userId, PageRequestDTO pageRequestDTO) {
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize = pageRequestDTO.getSize() + 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(0, pageSize, sort);

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor = Long.parseLong(pageRequestDTO.getCursor());
        }

        // 특정 유저가 좋아요 누른 모먼트 목록 조회
        List<Post> posts=postRepository.findLikedMomentAllByUserId(userId, cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(posts.size()>pageRequestDTO.getSize()){
            posts.remove(posts.size()-1);
            hasNext=true;
        }

        // 조회된 모먼츠 목록을 기반으로, 모먼츠 정보 이외에 댓글, 좋아요, 팔로우 정보 가져오기
        List<MomentDetailReadAllResponseDTO> momentDetailReadAllResponseDTOS=postReadSupport.readMomentDetailAllByPosts(posts);

        // 게시글 세부사항 페이지 반환
        return PageResponseDTO.<MomentDetailReadAllResponseDTO>builder()
                .content(momentDetailReadAllResponseDTOS)
                .hasNext(hasNext)
                .build();
    }
}