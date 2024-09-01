package applesquare.moment.tag.service.impl;

import applesquare.moment.common.dto.PageRequestDTO;
import applesquare.moment.common.dto.PageResponseDTO;
import applesquare.moment.tag.dto.TagReadResponseDTO;
import applesquare.moment.tag.model.Tag;
import applesquare.moment.tag.repository.TagRepository;
import applesquare.moment.tag.service.TagService;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;


    /**
     * 이름으로 태그 조회
     * (기존에 있던 태그면 조회하고, 없는 태그면 생성하기)
     *
     * @param tagName 태그 이름
     * @return 태그
     */
    @Override
    public Tag readByName(String tagName){
        Optional<Tag> optionalTag=tagRepository.findByName(tagName);
        if(optionalTag.isPresent()){
            // 태그가 존재하는 경우
            return optionalTag.get();
        }
        else{
            // 태그가 존재하지 않는 경우, DB에 태그 추가하기
            Tag newTag=Tag.builder()
                    .name(tagName)
                    .build();
            return tagRepository.save(newTag);
        }
    }

    /**
     * (주어진 태그 목록 중에서) 아무에게도 참조되지 않는 태그를 삭제
     * @param tags 태그 목록
     */
    @Override
    public void deleteUnreferencedTags(Collection<Tag> tags){
        List<String> tagNames=tags.stream().map(tag -> tag.getName()).toList();
        List<Tag> unreferencedTags=tagRepository.findUnreferencedTags(tagNames);
        tagRepository.deleteAll(unreferencedTags);
    }

    /**
     * (키워드에 따른) 태그 검색
     * @param pageRequestDTO 페이지 요청 정보
     * @return 검색 결과 목록
     */
    @Override
    public PageResponseDTO<TagReadResponseDTO> search(PageRequestDTO pageRequestDTO){
        // 입력 형식 검사
        String keyword=pageRequestDTO.getKeyword();
        if(keyword==null){
            throw new IllegalArgumentException("키워드를 입력해주세요.");
        }

        // 다음 페이지 존재 여부를 확인하기 위해 (size + 1)
        int pageSize=pageRequestDTO.getSize()+1;
        Sort sort= Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable= PageRequest.of(0, pageSize, sort);

        Long cursor=pageRequestDTO.getCursor();

        // 키워드에 따른 태그 검색
        List<Tuple> tuples=tagRepository.findByKeyword(keyword, cursor, pageable);

        // hasNext 설정
        boolean hasNext=false;
        if(tuples.size()>pageRequestDTO.getSize()){
            tuples.remove(tuples.size()-1);
            hasNext=true;
        }

        // DTO 변환
        List<TagReadResponseDTO> tagDTOs=tuples.stream().map((tuple)->{
            Tag tag1=(Tag) tuple.get("tag");
            long postCount=(long) tuple.get("postCount");

            return TagReadResponseDTO.builder()
                    .id(tag1.getId())
                    .name(tag1.getName())
                    .usageCount(postCount)
                    .build();
        }).toList();

        PageResponseDTO<TagReadResponseDTO> pageResponseDTO=PageResponseDTO.<TagReadResponseDTO>builder()
                .content(tagDTOs)
                .hasNext(hasNext)
                .build();

        return pageResponseDTO;
    }

    /**
     * 인기 태그 조회
     * @param days 며칠 전 기록부터 조회할 것인지
     * @param size 조회할 태그 개수
     * @return 인기 태그 목록
     */
    @Override
    public List<TagReadResponseDTO> readPopularTags(Integer days, Integer size){
        size = (size!=null)? size : 10;
        Sort sort= Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable= PageRequest.of(0, size, sort);

        // 키워드에 따른 태그 검색
        LocalDateTime baseTime = (days!=null)? LocalDateTime.now().minusDays(days) : null;
        List<Tuple> tuples=tagRepository.findPopularTags(baseTime, pageable);

        if(tuples.size()==0 && baseTime!=null){
            // 만약 최근 (days)일 동안 사용된 태그가 없다면, 전체 기간에서 인기 태그 조회
            tuples=tagRepository.findPopularTags(null, pageable);
        }

        // DTO 변환
        List<TagReadResponseDTO> tagDTOs=tuples.stream().map((tuple)->{
            Tag tag1=(Tag) tuple.get("tag");
            long postCount=(long) tuple.get("postCount");

            return TagReadResponseDTO.builder()
                    .id(tag1.getId())
                    .name(tag1.getName())
                    .usageCount(postCount)
                    .build();
        }).toList();

        return tagDTOs;
    }
}