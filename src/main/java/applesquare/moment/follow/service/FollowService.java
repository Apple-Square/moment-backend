package applesquare.moment.follow.service;

public interface FollowService {
    String follow(String followeeId);
    String unfollow(String followeeId);
}
