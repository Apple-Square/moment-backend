package applesquare.moment.post.service.impl;

import applesquare.moment.auth.exception.TokenException;
import applesquare.moment.comment.repository.CommentRepository;
import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.common.service.SecurityService;
import applesquare.moment.file.model.StorageFile;
import applesquare.moment.file.service.FileService;
import applesquare.moment.follow.repository.FollowRepository;
import applesquare.moment.like.repository.PostLikeRepository;
import applesquare.moment.post.dto.MomentDetailReadAllResponseDTO;
import applesquare.moment.post.dto.MomentThumbnailReadAllResponseDTO;
import applesquare.moment.post.model.Post;
import applesquare.moment.post.repository.PostRepository;
import applesquare.moment.post.service.MomentReadService;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.user.service.UserProfileService;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class MomentReadServiceImpl implements MomentReadService {
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;
    private final SecurityService securityService;
    private final FileService fileService;
    private final UserProfileService userProfileService;
    private final ModelMapper modelMapper;


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
        List<MomentDetailReadAllResponseDTO> momentDetailReadAllResponseDTOS=readDetailAllByPosts(posts);

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
        List<MomentDetailReadAllResponseDTO> momentDetailReadAllResponseDTOS=readDetailAllByPosts(posts);

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
        List<MomentDetailReadAllResponseDTO> momentDetailReadAllResponseDTOS=readDetailAllByPosts(posts);

        // 게시글 세부사항 페이지 반환
        return PageResponseDTO.<MomentDetailReadAllResponseDTO>builder()
                .content(momentDetailReadAllResponseDTOS)
                .hasNext(hasNext)
                .build();
    }


    /**
     * Post 엔티티 목록을 받아서,
     * 댓글, 좋아요, 팔로우 정보를 조회한 후,
     * 모먼트 세부사항 DTO 목록으로 변환한다.
     * (반복되는 코드를 줄이기 위함)
     *
     * @param posts Post 엔티티 목록
     * @return 모먼트 세부사항 DTO 목록
     */
    private List<MomentDetailReadAllResponseDTO> readDetailAllByPosts(List<Post> posts) {
        // 나의 유저 ID 가져오기
        String myUserId = null;
        try {
            myUserId = securityService.getUserId();
        } catch (TokenException e) {
            // 로그인하지 않은 경우
            // 로그인이 필요하지 않으므로 아무런 처리도 하지 않는다.
        }

        List<Long> postIds = posts.stream().map(post -> post.getId()).toList();

        // 댓글 개수 가져오기
        List<Tuple> postCommentCountTuples = commentRepository.countByPostIds(postIds);

        Map<Long, Long> commentCountMap = new LinkedHashMap<>();
        for (Tuple tuple : postCommentCountTuples) {
            commentCountMap.put((Long) tuple.get("postId"), (long) tuple.get("commentCount"));
        }

        // 댓글 작성 여부 가져오기
        List<Long> commentedPostIds = (myUserId != null) ?
                commentRepository.findAllCommentedPostIdByPostIdsAndUserId(postIds, myUserId)
                : new ArrayList<>();

        // 좋아요 개수 가져오기
        List<Tuple> postLikeCountTuples = postLikeRepository.countByPostIds(postIds);

        Map<Long, Long> likeCountMap = new LinkedHashMap<>();
        for (Tuple tuple : postLikeCountTuples) {
            likeCountMap.put((Long) tuple.get("postId"), (long) tuple.get("likeCount"));
        }

        // 좋아요 여부 가져오기
        List<Long> likedPostIds = (myUserId != null) ?
                postLikeRepository.findAllLikedPostIdByPostIdsAndUserId(postIds, myUserId)
                : new ArrayList<>();

        // 작성자 팔로우 여부 가져오기
        List<String> writerIds = posts.stream().map(post -> post.getWriter().getId()).toList();
        List<String> followedWriterIds = followRepository.findAllFollowedFolloweeIdByFolloweeIdsAndUserId(writerIds, myUserId);

        // DTO 변환
        List<MomentDetailReadAllResponseDTO> momentDetailReadAllResponseDTOS = posts.stream().map((post) -> {
            // Post 엔티티를 PostDetailReadAllResponseDTO로 매핑
            MomentDetailReadAllResponseDTO momentDetailReadAllResponseDTO = modelMapper.map(post, MomentDetailReadAllResponseDTO.class);
            // 유저 프로필 조회
            UserProfileReadResponseDTO writer = userProfileService.readProfileById(post.getWriter().getId());
            // 파일명을 URL로 변환
            List<StorageFile> files = post.getFiles();
            String url = null;
            if (files != null && files.size() > 0) {
                for (StorageFile sf : files) {
                    url = fileService.convertFilenameToUrl(sf.getFilename());
                }
            }

            // DTO 필드 채우기
            Long postId = post.getId();
            return momentDetailReadAllResponseDTO.toBuilder()
                    .writer(writer)
                    .url(url)
                    .isFollowed(followedWriterIds.contains(writer.getId()))
                    .commentCount(commentCountMap.getOrDefault(postId, 0L))
                    .likeCount(likeCountMap.getOrDefault(postId, 0L))
                    .isCommented(commentedPostIds.contains(postId))
                    .isLiked(likedPostIds.contains(postId))
                    .build();
        }).toList();

        return momentDetailReadAllResponseDTOS;
    }
}