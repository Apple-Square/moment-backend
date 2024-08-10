package applesquare.moment.auth.repository;

import java.util.concurrent.TimeUnit;

public interface TokenBlacklistRepository {
    void saveStringWithTimeout(String key, String value, long timeout, TimeUnit unit);
    boolean exists(String key);
}
