package applesquare.moment.like.service;

public interface CommentLikeService {
    Long like(Long commentId);
    void unlike(Long commentId);
}
