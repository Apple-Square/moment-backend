package applesquare.moment.post.service.impl;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.post.dto.MomentDetailReadAllResponseDTO;
import applesquare.moment.post.model.Post;
import applesquare.moment.post.repository.PostRepository;
import applesquare.moment.post.service.MomentSearchService;
import applesquare.moment.post.service.PostReadSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MomentSearchServiceImpl implements MomentSearchService {
    private final PostReadSupport postReadSupport;
    private final PostRepository postRepository;


    /**
     * 키워드 기반 모먼트 검색
     * - 검색 속성 : 게시물 내용, 작성자 닉네임, 태그명
     * - 정렬 기준 : 최신순
     *
     * @param pageRequestDTO 페이지 요청 정보
     * @return 모먼트 세부사항 목록
     */
    @Override
    public PageResponseDTO<MomentDetailReadAllResponseDTO> searchDetail(PageRequestDTO pageRequestDTO){
        // 입력 형식 검사
        String keyword=pageRequestDTO.getKeyword();
        if(keyword==null || keyword.isBlank()) {
            throw new IllegalArgumentException("키워드를 입력해주세요.");
        }

        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize=pageRequestDTO.getSize()+1;

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor=Long.parseLong(pageRequestDTO.getCursor());
        }

        // 키워드로 모먼트 검색 (내용, 작성자, 태그 검색)
        List<Long> postIds=postRepository.searchMomentIdsByKeyword(keyword, cursor, pageSize);

        // hasNext 설정
        boolean hasNext=false;
        if(postIds.size()>pageRequestDTO.getSize()){
            postIds.remove(postIds.size()-1);
            hasNext=true;
        }

        // postIds 기반으로 Post 엔티티 조회
        List<Post> posts=postRepository.findAllByPostIds(postIds);
        List<MomentDetailReadAllResponseDTO> momentDetailDTOs=postReadSupport.readMomentDetailAllByPosts(posts);

        // 모먼트 검색 페이지 반환
        return PageResponseDTO.<MomentDetailReadAllResponseDTO>builder()
                .content(momentDetailDTOs)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 태그 기반 모먼트 검색
     * - 검색 속성 : 태그명
     * - 정렬 기준 : 최신순
     *
     * @param pageRequestDTO 페이지 요청 정보
     * @return 모먼트 세부사항 목록
     */
    @Override
    public PageResponseDTO<MomentDetailReadAllResponseDTO> searchDetailByTag(PageRequestDTO pageRequestDTO){
        // 입력 형식 검사
        String keyword=pageRequestDTO.getKeyword();
        if(keyword==null || keyword.isBlank()) {
            throw new IllegalArgumentException("키워드를 입력해주세요.");
        }

        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize=pageRequestDTO.getSize()+1;

        Long cursor=null;
        if(pageRequestDTO.getCursor()!=null){
            cursor=Long.parseLong(pageRequestDTO.getCursor());
        }

        // 태그로 모먼트 검색
        List<Long> postIds=postRepository.searchMomentIdsByTag(keyword, cursor, pageSize);

        // hasNext 설정
        boolean hasNext=false;
        if(postIds.size()>pageRequestDTO.getSize()){
            postIds.remove(postIds.size()-1);
            hasNext=true;
        }

        // postIds 기반으로 Post 엔티티 조회
        List<Post> posts=postRepository.findAllByPostIds(postIds);
        List<MomentDetailReadAllResponseDTO> momentDetailDTOs=postReadSupport.readMomentDetailAllByPosts(posts);

        // 모먼트 검색 페이지 반환
        return PageResponseDTO.<MomentDetailReadAllResponseDTO>builder()
                .content(momentDetailDTOs)
                .hasNext(hasNext)
                .build();
    }
}
