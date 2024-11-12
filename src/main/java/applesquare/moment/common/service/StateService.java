package applesquare.moment.common.service;

import java.util.concurrent.TimeUnit;

public interface StateService {
    void create(String state, long ttl, TimeUnit timeUnit);
    void delete(String state);
    boolean exists(String state);
}
