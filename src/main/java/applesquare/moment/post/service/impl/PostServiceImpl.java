package applesquare.moment.post.service.impl;

import applesquare.moment.common.service.SecurityService;
import applesquare.moment.post.dto.PostCreateRequestDTO;
import applesquare.moment.post.dto.PostUpdateRequestDTO;
import applesquare.moment.post.model.Post;
import applesquare.moment.post.repository.PostRepository;
import applesquare.moment.post.service.PostService;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserInfoRepository userInfoRepository;
    private final SecurityService securityService;


    /**
     * 게시글 생성
     * [필요 권한 : 로그인 상태]
     * (Controller 계층에서 @PreAuthorize 검사)
     *
     * @param postCreateRequestDTO 게시글 생성 정보
     * @return 새로 생성된 게시글의 ID
     */
    @Override
    public Long create(PostCreateRequestDTO postCreateRequestDTO){
        // 입력 형식 검사

        // 엔티티 생성
        String userId= securityService.getUserId();
        UserInfo writer=userInfoRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+userId+")"));
        Post post=Post.builder()
                .content(postCreateRequestDTO.getContent())
                .viewCount(0)
                .writer(writer)
                .build();

        // DB 저장
        Post result=postRepository.save(post);

        // 리소스 ID 반환
        return result.getId();
    }

    /**
     * 특정 게시글 수정
     * [필요 권한 : 로그인 상태 & 게시글 소유자]
     *
     * @param postId 게시글 ID
     * @param postUpdateRequestDTO 게시글 변경 정보
     * @return 수정된 게시글의 ID
     */
    @Override
    public Long update(Long postId, PostUpdateRequestDTO postUpdateRequestDTO){
        // DB에 저장된 이전 게시글 엔티티 가져오기
        Post oldPost=postRepository.findById(postId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 게시글입니다. (id = "+postId+")"));

        // 권한 검사
        String userId= securityService.getUserId();
        if(!isOwner(oldPost, userId)){
            throw new AccessDeniedException("게시글 작성자만 수정할 수 있습니다.");
        }


        // 일부 필드를 변경한 새로운 게시글 엔티티 생성
        String content=(postUpdateRequestDTO.getContent()!=null)? postUpdateRequestDTO.getContent(): oldPost.getContent();
        Post newPost=oldPost.toBuilder()
                .content(content)
                .build();

        // DB 저장
        Post result=postRepository.save(newPost);

        // 리소스 ID 반환
        return result.getId();
    }

    /**
     * 특정 게시글 삭제
     * [필요 권한 : 로그인 상태 & 게시글 소유자]
     *
     * @param postId 게시글 ID
     */
    @Override
    public void delete(Long postId){
        // DB에 저장된 이전 게시글 엔티티 가져오기
        Optional<Post> postOptional=postRepository.findById(postId);

        // DB에 게시글이 있는 경우
        if(postOptional.isPresent()){
            // 권한 검사
            Post post=postOptional.get();
            String userId= securityService.getUserId();
            if(!isOwner(post, userId)){
                throw new AccessDeniedException("게시글 작성자만 삭제할 수 있습니다.");
            }

            // DB 삭제
            postRepository.deleteById(postId);
        }

        // 이미 게시글이 삭제된 상태라면 아무 동작도 하지 않고 않기
    }


    /**
     * 특정 사용자가 특정 게시글을 소유하고 있는지 여부 검사
     * @param post 게시글
     * @param userId 사용자 ID
     * @return 게시글 소유 여부
     */
    private boolean isOwner(Post post, String userId){
        return post.getWriter().getId().equals(userId);
    }
}
