package applesquare.moment.user.service;

import applesquare.moment.user.dto.UserPageReadResponseDTO;

public interface UserPageService {
    UserPageReadResponseDTO readUserPageById(String userId);
}
