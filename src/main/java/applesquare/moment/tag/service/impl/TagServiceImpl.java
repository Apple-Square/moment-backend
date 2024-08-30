package applesquare.moment.tag.service.impl;

import applesquare.moment.tag.model.Tag;
import applesquare.moment.tag.repository.TagRepository;
import applesquare.moment.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Tag readTagByName(String tagName){
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

    @Override
    public void deleteUnreferencedTags(Collection<Tag> tags){
        List<String> tagNames=tags.stream().map(tag -> tag.getName()).toList();
        List<Tag> unreferencedTags=tagRepository.findUnreferencedTags(tagNames);
        tagRepository.deleteAll(unreferencedTags);
    }
}
