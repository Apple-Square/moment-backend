package applesquare.moment.common.service;

public interface StateService {
    int TIMEOUT_MINUTE=10;

    void create(String state);
    void delete(String state);
    boolean exists(String state);
}
