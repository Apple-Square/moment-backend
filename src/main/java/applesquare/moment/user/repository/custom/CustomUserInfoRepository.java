package applesquare.moment.user.repository.custom;

import applesquare.moment.user.model.UserInfo;

import java.util.List;

public interface CustomUserInfoRepository {
    List<UserInfo> searchByKeyword(String keyword, String cursor, int pageSize);
}
