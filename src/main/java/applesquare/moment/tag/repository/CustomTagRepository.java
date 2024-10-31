package applesquare.moment.tag.repository;

import applesquare.moment.tag.dto.TagReadResponseDTO;

import java.util.List;

public interface CustomTagRepository {
    List<TagReadResponseDTO> searchByKeyword(String keyword, Long cursor, int pageSized);
}
