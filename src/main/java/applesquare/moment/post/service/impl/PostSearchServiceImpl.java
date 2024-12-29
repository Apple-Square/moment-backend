package applesquare.moment.post.service.impl;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.file.service.FileService;
import applesquare.moment.post.dto.PostDetailReadAllResponseDTO;
import applesquare.moment.post.dto.PostThumbnailReadAllResponseDTO;
import applesquare.moment.post.model.Post;
import applesquare.moment.post.repository.PostRepository;
import applesquare.moment.post.service.PostReadSupport;
import applesquare.moment.post.service.PostSearchService;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PostSearchServiceImpl implements PostSearchService {
    private final PostReadSupport postReadSupport;
    private final PostRepository postRepository;
    private final FileService fileService;


    /**
     * 키워드로 게시물 세부 사항 검색
     * - 검색 속성 : 게시물 내용, 작성자 닉네임, 태그명
     * - 정렬 기준 : 최신순
     *
     * @param pageRequestDTO 페이지 요청 정보
     * @return 게시물 세부 사항 목록
     */
    public PageResponseDTO<PostDetailReadAllResponseDTO> searchDetail(PageRequestDTO pageRequestDTO){
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

        // 키워드로 게시물 검색 (내용, 작성자, 태그 검색)
        List<Long> postIds=postRepository.searchPostIdsByKeyword(keyword, cursor, pageSize);

        // hasNext 설정
        boolean hasNext=false;
        if(postIds.size()>pageRequestDTO.getSize()){
            postIds.remove(postIds.size()-1);
            hasNext=true;
        }

        // postIds 기반으로 Post 엔티티 조회
        List<Post> posts=postRepository.findAllByPostIds(postIds);

        // 조회된 게시물 목록을 기반으로, 게시물 정보 이외에 태그, 댓글, 좋아요 정보 가져오기
        List<PostDetailReadAllResponseDTO> postDetailDTOs=postReadSupport.readPostDetailAllByPosts(posts);

        // 검색 결과 페이지 반환
        return PageResponseDTO.<PostDetailReadAllResponseDTO>builder()
                .content(postDetailDTOs)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 키워드로 게시물 썸네일 검색
     * - 검색 속성 : 게시물 내용, 작성자 닉네임, 태그명
     * - 정렬 기준 : 최신순
     *
     * @param pageRequestDTO 페이지 요청 정보
     * @return 게시물 썸네일 목록
     */
    public PageResponseDTO<PostThumbnailReadAllResponseDTO> searchThumbnail(PageRequestDTO pageRequestDTO){
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

        // 키워드로 게시물 검색 (내용, 작성자, 태그 검색)
        List<Long> postIds=postRepository.searchPostIdsByKeyword(keyword, cursor, pageSize);

        // hasNext 설정
        boolean hasNext=false;
        if(postIds.size()>pageRequestDTO.getSize()){
            postIds.remove(postIds.size()-1);
            hasNext=true;
        }

        // postIds 기반으로 썸네일 파일 조회
        List<Tuple> tuples=postRepository.findAllByPostIdsWithFirstFile(postIds);

        // DTO 변환
        List<PostThumbnailReadAllResponseDTO> postThumbnailDTOs=tuples.stream().map((tuple)->{
            String thumbFilename=fileService.convertFilenameToThumbFilename((String) tuple.get("filename"));
            return PostThumbnailReadAllResponseDTO.builder()
                    .id((Long) tuple.get("postId"))
                    .url(fileService.convertFilenameToUrl(thumbFilename))
                    .mediaType(FileService.convertContentTypeToMediaType((String)tuple.get("contentType")))
                    .build();
        }).toList();

        // 검색 결과 페이지 반환
        return PageResponseDTO.<PostThumbnailReadAllResponseDTO>builder()
                .content(postThumbnailDTOs)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 태그로 게시물 세부 사항 검색
     * - 검색 속성 : 태그명
     * - 정렬 기준 : 최신순
     *
     * @param pageRequestDTO 페이지 요청 정보
     * @return 게시물 세부 사항 목록
     */
    @Override
    public PageResponseDTO<PostDetailReadAllResponseDTO> searchDetailByTag(PageRequestDTO pageRequestDTO){
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

        // 태그로 게시물 검색
        List<Long> postIds=postRepository.searchPostIdsByTag(keyword, cursor, pageSize);

        // hasNext 설정
        boolean hasNext=false;
        if(postIds.size()>pageRequestDTO.getSize()){
            postIds.remove(postIds.size()-1);
            hasNext=true;
        }

        // postIds 기반으로 Post 엔티티 조회
        List<Post> posts=postRepository.findAllByPostIds(postIds);

        // 조회된 게시물 목록을 기반으로, 게시물 정보 이외에 태그, 댓글, 좋아요 정보 가져오기
        List<PostDetailReadAllResponseDTO> postDetailDTOs=postReadSupport.readPostDetailAllByPosts(posts);

        // 검색 결과 페이지 반환
        return PageResponseDTO.<PostDetailReadAllResponseDTO>builder()
                .content(postDetailDTOs)
                .hasNext(hasNext)
                .build();
    }

    /**
     * 태그로 게시물 썸네일 검색
     * - 검색 속성 : 태그명
     * - 정렬 기준 : 최신순
     *
     * @param pageRequestDTO 페이지 요청 정보
     * @return 게시물 썸네일 목록
     */
    @Override
    public PageResponseDTO<PostThumbnailReadAllResponseDTO> searchThumbnailByTag(PageRequestDTO pageRequestDTO){
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

        // 태그로 게시물 검색
        List<Long> postIds=postRepository.searchPostIdsByTag(keyword, cursor, pageSize);

        // hasNext 설정
        boolean hasNext=false;
        if(postIds.size()>pageRequestDTO.getSize()){
            postIds.remove(postIds.size()-1);
            hasNext=true;
        }

        // postIds 기반으로 썸네일 파일 조회
        List<Tuple> tuples=postRepository.findAllByPostIdsWithFirstFile(postIds);

        // DTO 변환
        List<PostThumbnailReadAllResponseDTO> postThumbnailDTOs=tuples.stream().map((tuple)->{
            String thumbFilename=fileService.convertFilenameToThumbFilename((String) tuple.get("filename"));
            return PostThumbnailReadAllResponseDTO.builder()
                    .id((Long) tuple.get("postId"))
                    .url(fileService.convertFilenameToUrl(thumbFilename))
                    .mediaType(FileService.convertContentTypeToMediaType((String)tuple.get("contentType")))
                    .build();
        }).toList();

        // 검색 결과 페이지 반환
        return PageResponseDTO.<PostThumbnailReadAllResponseDTO>builder()
                .content(postThumbnailDTOs)
                .hasNext(hasNext)
                .build();
    }
}
