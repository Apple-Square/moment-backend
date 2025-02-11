package applesquare.moment.post.service;

import applesquare.moment.auth.exception.TokenException;
import applesquare.moment.comment.repository.CommentRepository;
import applesquare.moment.common.security.SecurityService;
import applesquare.moment.file.model.MediaType;
import applesquare.moment.file.model.StorageFile;
import applesquare.moment.file.service.FileService;
import applesquare.moment.follow.repository.FollowRepository;
import applesquare.moment.like.repository.PostLikeRepository;
import applesquare.moment.post.dto.MomentDetailReadAllResponseDTO;
import applesquare.moment.post.dto.PostDetailReadAllResponseDTO;
import applesquare.moment.post.model.Post;
import applesquare.moment.tag.repository.TagRepository;
import applesquare.moment.user.dto.UserProfileReadResponseDTO;
import applesquare.moment.user.service.UserProfileService;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PostReadSupport {
    private final SecurityService securityService;
    private final UserProfileService userProfileService;
    private final FileService fileService;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final FollowRepository followRepository;
    private final ModelMapper modelMapper;

    /**
     * Post 엔티티 목록을 받아서,
     * 태그, 댓글, 좋아요 정보를 조회한 후,
     * 게시물 세부사항 DTO 목록으로 변환한다.
     * (반복되는 코드를 줄이기 위함)
     *
     * @param posts Post 엔티티 목록
     * @return 게시물 세부사항 DTO 목록
     */

    public List<PostDetailReadAllResponseDTO> readPostDetailAllByPosts(List<Post> posts){
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
                mediaType= FileService.convertContentTypeToMediaType(files.get(0).getContentType());
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

    /**
     * Post 엔티티 목록을 받아서,
     * 댓글, 좋아요, 팔로우 정보를 조회한 후,
     * 모먼트 세부사항 DTO 목록으로 변환한다.
     * (반복되는 코드를 줄이기 위함)
     *
     * @param posts Post 엔티티 목록
     * @return 모먼트 세부사항 DTO 목록
     */
    public List<MomentDetailReadAllResponseDTO> readMomentDetailAllByPosts(List<Post> posts) {
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
