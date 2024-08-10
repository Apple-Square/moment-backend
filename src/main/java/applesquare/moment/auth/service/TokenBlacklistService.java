package applesquare.moment.auth.service;

public interface TokenBlacklistService {
    void blacklist(String token, String reason);
    boolean exists(String token);
}
