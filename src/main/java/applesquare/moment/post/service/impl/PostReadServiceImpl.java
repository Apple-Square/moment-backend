package applesquare.moment.post.service.impl;

import applesquare.moment.comment.repository.CommentRepository;
import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.common.service.SecurityService;
import applesquare.moment.exception.TokenException;
import applesquare.moment.file.model.MediaType;
import applesquare.moment.file.model.StorageFile;
import applesquare.moment.file.service.FileService;
import applesquare.moment.like.repository.PostLikeRepository;
import applesquare.moment.post.dto.PostDetailReadAllResponseDTO;
import applesquare.moment.post.dto.PostThumbnailReadAllResponseDTO;
import applesquare.moment.post.model.Post;
import applesquare.moment.post.repository.PostRepository;
import applesquare.moment.post.service.PostReadService;
import applesquare.moment.tag.repository.TagRepository;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.user.service.UserProfileService;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class PostReadServiceImpl implements PostReadService {
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final SecurityService securityService;
    private final UserProfileService userProfileService;
    private final FileService fileService;
    private final ModelMapper modelMapper;

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

        Long cursor= pageRequestDTO.getCursor();

        // 게시물 목록 조회
        List<Post> posts=postRepository.findAll(cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(posts.size()>pageRequestDTO.getSize()){
            posts.remove(posts.size()-1);
            hasNext=true;
        }

        // 조회된 게시물 목록을 기반으로, 게시물 정보 이외에 태그, 댓글, 좋아요 정보 가져오기
        List<PostDetailReadAllResponseDTO> postDetailReadAllResponseDTOS=readDetailAllByPosts(posts);

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

        Long cursor= pageRequestDTO.getCursor();

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

        Long cursor= pageRequestDTO.getCursor();

        // 특정 유저가 작성한 게시물 목록 조회
        List<Post> posts=postRepository.findAllByWriterId(userId, cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(posts.size()>pageRequestDTO.getSize()){
            posts.remove(posts.size()-1);
            hasNext=true;
        }

        // 조회된 게시물 목록을 기반으로, 게시물 정보 이외에 태그, 댓글, 좋아요 정보 가져오기
        List<PostDetailReadAllResponseDTO> postDetailReadAllResponseDTOS=readDetailAllByPosts(posts);

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

        Long cursor= pageRequestDTO.getCursor();

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

        Long cursor= pageRequestDTO.getCursor();

        // 특정 유저가 좋아요 누른 게시물 목록 조회
        List<Post> posts=postRepository.findLikedPostAllByUserId(userId, cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(posts.size()>pageRequestDTO.getSize()){
            posts.remove(posts.size()-1);
            hasNext=true;
        }

        // 조회된 게시물 목록을 기반으로, 게시물 정보 이외에 태그, 댓글, 좋아요 정보 가져오기
        List<PostDetailReadAllResponseDTO> postDetailReadAllResponseDTOS=readDetailAllByPosts(posts);

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

        Long cursor= pageRequestDTO.getCursor();

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


    /**
     * Post 엔티티 목록을 받아서,
     * 태그, 댓글, 좋아요 정보를 조회한 후,
     * 게시물 세부사항 DTO 목록으로 변환한다.
     * (반복되는 코드를 줄이기 위함)
     *
     * @param posts Post 엔티티 목록
     * @return 게시물 세부사항 DTO 목록
     */
    private List<PostDetailReadAllResponseDTO> readDetailAllByPosts(List<Post> posts){
        // 나의 유저 ID 가져오기
        String myUserId=null;
        try{
            myUserId=securityService.getUserId();
        }catch(TokenException e){
            // 로그인하지 않은 경우
            // 로그인이 필요하지 않으므로 아무런 처리도 하지 않는다.
        }

        List<Long> postIds=posts.stream().map(post -> post.getId()).toList();

        // 태그 목록 조회
        List<Tuple> postTagTuples=tagRepository.findTagAllByPostIds(postIds);

        Map<Long, List<String>> postTagsMap=new LinkedHashMap<>();
        // 각 게시물 ID에 대해 빈 리스트 준비 (태그가 없는 게시물의 경우 응답에서 빈 리스트를 보여주기 위함)
        for(Long postId : postIds){
            if(!postTagsMap.containsKey(postId)){
                postTagsMap.put(postId, new LinkedList<>());
            }
        }
        if(postTagTuples!=null){
            for(Tuple tuple : postTagTuples){
                Long postId= (Long)tuple.get("postId");
                String tagName= (String)tuple.get("tagName");
                List<String> tags=postTagsMap.get(postId);
                tags.add(tagName);
            }
        }

        // 댓글 개수 가져오기
        List<Tuple> postCommentCountTuples=commentRepository.countByPostIds(postIds);

        Map<Long, Long> commentCountMap=new LinkedHashMap<>();
        for(Tuple tuple : postCommentCountTuples){
            commentCountMap.put((Long) tuple.get("postId"), (long) tuple.get("commentCount"));
        }

        // 댓글 작성 여부 가져오기
        List<Long> commentedPostIds=(myUserId!=null)?
                commentRepository.findAllCommentedPostIdByPostIdsAndUserId(postIds, myUserId)
                : new ArrayList<>();

        // 좋아요 개수 가져오기
        List<Tuple> postLikeCountTuples=postLikeRepository.countByPostIds(postIds);

        Map<Long, Long> likeCountMap=new LinkedHashMap<>();
        for(Tuple tuple : postLikeCountTuples){
            likeCountMap.put((Long)tuple.get("postId"), (long)tuple.get("likeCount"));
        }

        // 좋아요 여부 가져오기
        List<Long> likedPostIds=(myUserId!=null)?
                postLikeRepository.findAllLikedPostIdByPostIdsAndUserId(postIds, myUserId)
                : new ArrayList<>();

        // DTO 변환
        List<PostDetailReadAllResponseDTO> postDetailReadAllResponseDTOS=posts.stream().map((post)->{
            // Post 엔티티를 PostDetailReadAllResponseDTO로 매핑
            PostDetailReadAllResponseDTO postDetailReadAllResponseDTO=modelMapper.map(post, PostDetailReadAllResponseDTO.class);
            // 유저 프로필 조회
            UserProfileReadResponseDTO writer=userProfileService.readProfileById(post.getWriter().getId());
            // 파일명을 URL로 변환
            List<StorageFile> files=post.getFiles();
            List<String> urls=files.stream().map((storageFile)->
                    fileService.convertFilenameToUrl(storageFile.getFilename())
            ).toList();
            // 미디어 타입 조회
            MediaType mediaType=null;
            if(files.size()>0){
                mediaType=FileService.convertContentTypeToMediaType(files.get(0).getContentType());
            }

            // DTO 필드 채우기
            Long postId=post.getId();
            return postDetailReadAllResponseDTO.toBuilder()
                    .writer(writer)
                    .tags(postTagsMap.get(postId))
                    .mediaType(mediaType)
                    .urls(urls)
                    .commentCount(commentCountMap.getOrDefault(postId, 0L))
                    .likeCount(likeCountMap.getOrDefault(postId, 0L))
                    .isCommented(commentedPostIds.contains(postId))
                    .isLiked(likedPostIds.contains(postId))
                    .build();
        }).toList();

        return postDetailReadAllResponseDTOS;
    }
}
