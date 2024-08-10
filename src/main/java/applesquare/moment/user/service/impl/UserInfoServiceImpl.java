package applesquare.moment.user.service.impl;

import applesquare.moment.user.dto.UserInfoReadResponseDTO;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import applesquare.moment.user.service.UserInfoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {
    private final UserInfoRepository userInfoRepository;
    private final ModelMapper modelMapper;


    /**
     * 사용자 정보 조회
     * @param id 사용자 ID
     * @return 사용자 정보
     */
    @Override
    public UserInfoReadResponseDTO readById(String id){
        // 엔티티 조회
        UserInfo userInfo=userInfoRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+id+")"));

        // DTO로 변환해서 반환
        return modelMapper.map(userInfo, UserInfoReadResponseDTO.class);
    }
}
