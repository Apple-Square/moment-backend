package applesquare.moment.common.state;

import java.util.concurrent.TimeUnit;

public interface StateService {
    void create(String state, String metaData, long ttl, TimeUnit timeUnit);
    void delete(String state);
    String getMetaData(String state);
}
