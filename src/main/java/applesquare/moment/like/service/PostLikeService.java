package applesquare.moment.like.service;

public interface PostLikeService {
    Long like(Long postId);
    void unlike(Long postId);
}
