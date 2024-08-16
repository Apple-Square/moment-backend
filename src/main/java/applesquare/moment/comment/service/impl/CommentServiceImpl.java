package applesquare.moment.comment.service.impl;

import applesquare.moment.comment.dto.CommentCreateRequestDTO;
import applesquare.moment.comment.dto.CommentReadAllResponseDTO;
import applesquare.moment.comment.dto.CommentUpdateRequestDTO;
import applesquare.moment.comment.model.Comment;
import applesquare.moment.comment.repository.CommentRepository;
import applesquare.moment.comment.service.CommentService;
import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.common.service.SecurityService;
import applesquare.moment.post.model.Post;
import applesquare.moment.post.repository.PostRepository;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserInfoRepository userInfoRepository;
    private final SecurityService securityService;
    private final ModelMapper modelMapper;


    /**
     * 댓글 생성
     * [필요 권한 : 로그인 상태]
     *
     * @param postId 소속한 게시글 ID
     * @param commentCreateRequestDTO 댓글 생성 정보
     * @return 새로 생성된 댓글의 ID
     */
    @Override
    public Long create(Long postId, CommentCreateRequestDTO commentCreateRequestDTO){
        // 입력 형식 검사

        // 권한 검사
        String userId= securityService.getUserId();

        // 엔티티 생성
        UserInfo writer=userInfoRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+userId+")"));
        Post post=postRepository.findById(postId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 게시글입니다. (id = "+postId+")"));
        Comment comment=Comment.builder()
                .content(commentCreateRequestDTO.getContent())
                .writer(writer)
                .post(post)
                .build();

        // DB 저장
        Comment result=commentRepository.save(comment);

        // 리소스 ID 반환
        return result.getId();
    }

    /**
     * 특정 댓글 수정
     * [필요 권한 : 로그인 상태 & 댓글 소유자]
     *
     * @param commentId 댓글 ID
     * @param commentUpdateRequestDTO 댓글 변경 정보
     * @return 수정된 댓글의 ID
     */
    @Override
    public  Long update(Long commentId, CommentUpdateRequestDTO commentUpdateRequestDTO){
        // DB에 저장된 이전 댓글 엔티티 가져오기
        Comment oldComment=commentRepository.findById(commentId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 댓글입니다. (id = "+commentId+")"));

        // 권한 검사
        String userId=securityService.getUserId();
        if(!isOwner(oldComment, userId)){
            throw new AccessDeniedException("댓글 작성자만 수정할 수 있습니다.");
        }

        // 일부 필드를 변경한 새로운 댓글 엔티티 생성
        String content=(commentUpdateRequestDTO.getContent()!=null)? commentUpdateRequestDTO.getContent() : oldComment.getContent();
        Comment newComment=oldComment.toBuilder()
                .content(content)
                .build();

        // DB 저장
        Comment result=commentRepository.save(newComment);

        // 리소스 ID 반환
        return result.getId();
    }

    /**
     * 특정 댓글 삭제
     * [필요 권한 : 로그인 상태 & 댓글 소유자]
     *
     * @param commentId 댓글 ID
     */
    @Override
    public void delete(Long commentId){
        // DB에 저장된 이전 댓글 엔티티 가져오기
        Comment comment=commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("이미 삭제된 댓글입니다. (id = "+commentId+")"));

        // 권한 검사
        String userId=securityService.getUserId();
        if(!isOwner(comment, userId)){
            throw new AccessDeniedException("댓글 작성자만 삭제할 수 있습니다.");
        }

        // DB 삭제
        commentRepository.deleteById(commentId);
    }

    /**
     * 특정 게시글에 소속한 댓글 목록 조회
     * [필요 권한 : 없음]
     *
     * @param postId 소속한 게시글 ID
     * @param pageRequestDTO 페이지 요청 정보
     * @return 댓글 목록 페이지
     */
    @Override
    public PageResponseDTO<CommentReadAllResponseDTO> readAll(Long postId, PageRequestDTO pageRequestDTO){
        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize= pageRequestDTO.getSize()+1;
        Sort sort= Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable= PageRequest.of(0, pageSize, sort);

        Long cursor= pageRequestDTO.getCursor();

        // 특정 게시글의 댓글 목록 조회
        List<Comment> comments=commentRepository.findAllByPostId(postId, cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(comments.size()>pageRequestDTO.getSize()){
            comments.remove(comments.size()-1);
            hasNext=true;
        }

        // DTO 변환
        List<CommentReadAllResponseDTO> commentReadAllResponseDTOS=comments.stream().map(comment -> {
            CommentReadAllResponseDTO commentReadAllResponseDTO=modelMapper.map(comment, CommentReadAllResponseDTO.class);
            return commentReadAllResponseDTO;
        }).collect(Collectors.toList());

        PageResponseDTO<CommentReadAllResponseDTO> pageResponseDTO=PageResponseDTO.<CommentReadAllResponseDTO>builder()
                .content(commentReadAllResponseDTOS)
                .hasNext(hasNext)
                .build();

        return pageResponseDTO;
    }


    /**
     * 특정 사용자가 특정 댓글을 소유하고 있는지 검사
     * @param comment 댓글
     * @param userId 사용자 ID
     * @return 댓글 소유 여부
     */
    @Override
    public boolean isOwner(Comment comment, String userId){
        return comment.getWriter().getId().equals(userId);
    }
}