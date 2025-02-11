package applesquare.moment.post.service.impl;

import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.file.service.FileService;
import applesquare.moment.post.dto.PostDetailReadAllResponseDTO;
import applesquare.moment.post.dto.PostThumbnailReadAllResponseDTO;
import applesquare.moment.post.model.Post;
import applesquare.moment.post.repository.PostRepository;
import applesquare.moment.post.service.PostReadService;
import applesquare.moment.post.service.PostReadSupport;
import jakarta.persistence.EntityNotFoundException;
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
public class PostReadServiceImpl implements PostReadService {
    private final PostReadSupport postReadSupport;
    private final PostRepository postRepository;
    private final FileService fileService;


    /**
     * 특정 게시물 조회
     * @param postId 게시물 ID
     * @return 게시물 세부사항
     */
    @Override
    public PostDetailReadAllResponseDTO read(Long postId){
        Post post=postRepository.findById(postId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 게시글입니다. (id = "+postId+")"));

        // 조회된 게시물 기반으로, 게시물 정보 이외에 태그, 댓글, 좋아요 정보 가져오기
        PostDetailReadAllResponseDTO postDetailDTO=postReadSupport.readPostDetailAllByPosts(List.of(post)).get(0);

        return postDetailDTO;
    }

    /**
     * 게시물 세부사항 목록 조회 (커서 페이징)
     * @param pageRequestDTO 페이지 요청 정보
     * @return 게시물 세부사항 목록
     */
    @Override
    public PageResponseDTO<PostDetailReadAllResponseDTO> readDetailAll(PageRequestDTO pageRequestDTO){
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize= pageRequestDTO.getSize()+1;
        Sort sort= Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable= PageRequest.of(0, pageSize, sort);

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor= Long.parseLong(pageRequestDTO.getCursor());
        }

        // 게시물 목록 조회
        List<Post> posts=postRepository.findAll(cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(posts.size()>pageRequestDTO.getSize()){
            posts.remove(posts.size()-1);
            hasNext=true;
        }

        // 조회된 게시물 목록을 기반으로, 게시물 정보 이외에 태그, 댓글, 좋아요 정보 가져오기
        List<PostDetailReadAllResponseDTO> postDetailReadAllResponseDTOS=postReadSupport.readPostDetailAllByPosts(posts);

        // 게시글 세부사항 페이지 반환
        return PageResponseDTO.<PostDetailReadAllResponseDTO>builder()
                .content(postDetailReadAllResponseDTOS)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 게시물 썸네일 목록 조회 (커서 페이징)
     * @param pageRequestDTO 페이지 요청 정보
     * @return 게시물 썸네일 목록
     */
    @Override
    public PageResponseDTO<PostThumbnailReadAllResponseDTO> readThumbnailAll(PageRequestDTO pageRequestDTO){
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize= pageRequestDTO.getSize()+1;
        Sort sort= Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable= PageRequest.of(0, pageSize, sort);

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor= Long.parseLong(pageRequestDTO.getCursor());
        }

        // 게시글 썸네일 목록 조회
        List<Tuple> tuples=postRepository.findAllWithFirstFile(cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(tuples.size()>pageRequestDTO.getSize()){
            tuples.remove(tuples.size()-1);
            hasNext=true;
        }

        // DTO 변환
        List<PostThumbnailReadAllResponseDTO> postThumbnailReadAllResponseDTOS=tuples.stream().map((tuple)->{
            String thumbFilename=fileService.convertFilenameToThumbFilename((String) tuple.get("filename"));
            return PostThumbnailReadAllResponseDTO.builder()
                    .id((Long) tuple.get("postId"))
                    .url(fileService.convertFilenameToUrl(thumbFilename))
                    .mediaType(FileService.convertContentTypeToMediaType((String) tuple.get("contentType")))
                    .build();
        }).toList();

        // 게시글 세부사항 페이지 반환
        return PageResponseDTO.<PostThumbnailReadAllResponseDTO>builder()
                .content(postThumbnailReadAllResponseDTOS)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 특정 유저가 작성한 게시물 세부사항 목록 조회 (커서 페이징)
     * @param userId 작성자 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 게시물 세부사항 목록
     */
    @Override
    public PageResponseDTO<PostDetailReadAllResponseDTO> readDetailAllByUser(String userId, PageRequestDTO pageRequestDTO){
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize= pageRequestDTO.getSize()+1;
        Sort sort= Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable= PageRequest.of(0, pageSize, sort);

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor= Long.parseLong(pageRequestDTO.getCursor());
        }

        // 특정 유저가 작성한 게시물 목록 조회
        List<Post> posts=postRepository.findAllByWriterId(userId, cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(posts.size()>pageRequestDTO.getSize()){
            posts.remove(posts.size()-1);
            hasNext=true;
        }

        // 조회된 게시물 목록을 기반으로, 게시물 정보 이외에 태그, 댓글, 좋아요 정보 가져오기
        List<PostDetailReadAllResponseDTO> postDetailReadAllResponseDTOS=postReadSupport.readPostDetailAllByPosts(posts);

        // 게시글 세부사항 페이지 반환
        return PageResponseDTO.<PostDetailReadAllResponseDTO>builder()
                .content(postDetailReadAllResponseDTOS)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 특정 유저가 작성한 게시물 썸네일 목록 조회 (커서 페이징)
     * @param userId 작성자 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 게시물 썸네일 목록
     */
    @Override
    public PageResponseDTO<PostThumbnailReadAllResponseDTO> readThumbnailAllByUser(String userId, PageRequestDTO pageRequestDTO){
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize= pageRequestDTO.getSize()+1;
        Sort sort= Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable= PageRequest.of(0, pageSize, sort);

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor= Long.parseLong(pageRequestDTO.getCursor());
        }

        // 특정 유저가 작성한 게시물 썸네일 목록 조회
        List<Tuple> tuples=postRepository.findAllWithFirstFileByWriterId(userId, cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(tuples.size()>pageRequestDTO.getSize()){
            tuples.remove(tuples.size()-1);
            hasNext=true;
        }

        // DTO 변환
        List<PostThumbnailReadAllResponseDTO> postThumbnailReadAllResponseDTOS=tuples.stream().map((tuple)->{
            String thumbFilename=fileService.convertFilenameToThumbFilename((String) tuple.get("filename"));
            return PostThumbnailReadAllResponseDTO.builder()
                    .id((Long) tuple.get("postId"))
                    .url(fileService.convertFilenameToUrl(thumbFilename))
                    .mediaType(FileService.convertContentTypeToMediaType((String) tuple.get("contentType")))
                    .build();
        }).toList();

        // 게시글 세부사항 페이지 반환
        return PageResponseDTO.<PostThumbnailReadAllResponseDTO>builder()
                .content(postThumbnailReadAllResponseDTOS)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 특정 유저가 좋아요 누른 게시물 세부사항 목록 조회 (커서 페이징)
     * @param userId 사용자 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 게시물 세부사항 목록
     */
    @Override
    public PageResponseDTO<PostDetailReadAllResponseDTO> readLikedDetailAllByUser(String userId, PageRequestDTO pageRequestDTO){
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize= pageRequestDTO.getSize()+1;
        Sort sort= Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable= PageRequest.of(0, pageSize, sort);

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor= Long.parseLong(pageRequestDTO.getCursor());
        }

        // 특정 유저가 좋아요 누른 게시물 목록 조회
        List<Post> posts=postRepository.findLikedPostAllByUserId(userId, cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(posts.size()>pageRequestDTO.getSize()){
            posts.remove(posts.size()-1);
            hasNext=true;
        }

        // 조회된 게시물 목록을 기반으로, 게시물 정보 이외에 태그, 댓글, 좋아요 정보 가져오기
        List<PostDetailReadAllResponseDTO> postDetailReadAllResponseDTOS=postReadSupport.readPostDetailAllByPosts(posts);

        // 게시글 세부사항 페이지 반환
        return PageResponseDTO.<PostDetailReadAllResponseDTO>builder()
                .content(postDetailReadAllResponseDTOS)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 특정 유저가 좋아요 누른 게시물 썸네일 목록 조회 (커서 페이징)
     * @param userId 사용자 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 게시물 썸네일 목록
     */
    @Override
    public PageResponseDTO<PostThumbnailReadAllResponseDTO> readLikedThumbnailAllByUser(String userId, PageRequestDTO pageRequestDTO){
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize= pageRequestDTO.getSize()+1;
        Sort sort= Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable= PageRequest.of(0, pageSize, sort);

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor= Long.parseLong(pageRequestDTO.getCursor());
        }

        // 특정 유저가 좋아요 누른 게시물 썸네일 목록 조회
        List<Tuple> tuples=postRepository.findLikedPostAllWithFirstFileByUserId(userId, cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(tuples.size()>pageRequestDTO.getSize()){
            tuples.remove(tuples.size()-1);
            hasNext=true;
        }

        // DTO 변환
        List<PostThumbnailReadAllResponseDTO> postThumbnailReadAllResponseDTOS=tuples.stream().map((tuple)->{
            String thumbFilename=fileService.convertFilenameToThumbFilename((String) tuple.get("filename"));
            return PostThumbnailReadAllResponseDTO.builder()
                    .id((Long) tuple.get("postId"))
                    .url(fileService.convertFilenameToUrl(thumbFilename))
                    .mediaType(FileService.convertContentTypeToMediaType((String) tuple.get("contentType")))
                    .build();
        }).toList();

        // 게시글 세부사항 페이지 반환
        return PageResponseDTO.<PostThumbnailReadAllResponseDTO>builder()
                .content(postThumbnailReadAllResponseDTOS)
                .hasNext(hasNext)
                .build();
    }

    @Override
    public String readThumbnailImageUrl(Long postId){
        String thumbFilename= postRepository.findFirstFileById(postId);
        return fileService.convertFilenameToUrl(thumbFilename);
    }
}
