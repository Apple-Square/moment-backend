package applesquare.moment.follow.service;

public interface FollowService {
    Long follow(String followeeId);
    void unfollow(String followeeId);
}
