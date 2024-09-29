package applesquare.moment.post.service;

import applesquare.moment.post.dto.PostCreateRequestDTO;
import applesquare.moment.post.dto.PostUpdateRequestDTO;
import applesquare.moment.post.model.Post;

import java.io.IOException;

public interface PostManagementService {
    int MIN_CONTENT_LENGTH=0;
    int MAX_CONTENT_LENGTH=2048;
    int MAX_IMAGE_COUNT=10;
    int MAX_VIDEO_COUNT=1;
    int MAX_FILE_SIZE_MB=100;
    int MAX_FILE_SIZE_BYTES=MAX_FILE_SIZE_MB*1024*1024; // 100MB
    int MAX_TAG_COUNT=10;


    Long create(PostCreateRequestDTO postCreateRequestDTO) throws Exception;
    Long update(Long postId, PostUpdateRequestDTO postUpdateRequestDTO) throws Exception;
    void delete(Long postId) throws IOException;
    boolean isOwner(Post post, String userId);
    long incrementViewCount(Long postId, long increment);
}
