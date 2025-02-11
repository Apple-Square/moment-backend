package applesquare.moment.tag.service;

import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;
import applesquare.moment.tag.dto.TagReadResponseDTO;
import applesquare.moment.tag.model.Tag;

import java.util.Collection;
import java.util.List;


public interface TagService {
    int MIN_TAG_NAME_LENGTH=1;
    int MAX_TAG_NAME_LENGTH=100;

    Tag readByName(String tagName);
    void deleteUnreferencedTags(Collection<Tag> tags);
    PageResponseDTO<TagReadResponseDTO> search(PageRequestDTO pageRequestDTO);
    List<TagReadResponseDTO> readPopularTags(Integer days, Integer size);
}
