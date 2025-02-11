package applesquare.moment.comment.service;

import applesquare.moment.comment.dto.CommentCreateRequestDTO;
import applesquare.moment.comment.dto.CommentReadAllResponseDTO;
import applesquare.moment.comment.dto.CommentUpdateRequestDTO;
import applesquare.moment.comment.model.Comment;
import applesquare.moment.common.page.PageRequestDTO;
import applesquare.moment.common.page.PageResponseDTO;

public interface CommentService {
    int MIN_CONTENT_LENGTH=1;
    int MAX_CONTENT_LENGTH=2048;

    Long create(Long postId, CommentCreateRequestDTO commentCreateRequestDTO);
    Long update(Long commentId, CommentUpdateRequestDTO commentUpdateRequestDTO);
    void delete(Long commentId);

    PageResponseDTO<CommentReadAllResponseDTO> readAll(Long postId, PageRequestDTO pageRequestDTO);

    boolean isOwner(Comment comment, String userId);
}
