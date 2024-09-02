package applesquare.moment.user.service.impl;

import applesquare.moment.address.dto.AddressSearchResponseDTO;
import applesquare.moment.address.service.AddressService;
import applesquare.moment.common.service.SecurityService;
import applesquare.moment.user.dto.UserInfoUpdateRequestDTO;
import applesquare.moment.user.model.Gender;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import applesquare.moment.user.service.UserInfoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {
    private final UserInfoRepository userInfoRepository;
    private final SecurityService securityService;
    private final AddressService addressService;


    /**
     * 사용자 정보 편집
     * [필요 권한 : 로그인 상태 & 사용자 본인]
     *
     * @param userId 사용자 ID
     * @param userInfoUpdateRequestDTO 사용자 변경 정보
     * @return 수정된 사용자의 ID
     */
    @Override
    public String updateUserInfo(String userId, UserInfoUpdateRequestDTO userInfoUpdateRequestDTO){
        // 권한 검사
        String myUserId= securityService.getUserId();
        if(!myUserId.equals(userId)){
            throw new AccessDeniedException("사용자 본인의 정보만 수정할 수 있습니다.");
        }

        // 기존 UserInfo 엔티티 가져오기
        UserInfo oldUserInfo=userInfoRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 사용자입니다. (id = "+userId+")"));

        // 주소 유효성 검사 (실제로 존재하는 주소인지 검사)
        String newAddress=oldUserInfo.getAddress();
        if(userInfoUpdateRequestDTO.getAddress()!=null){
            AddressSearchResponseDTO addressSearchResponseDTO=addressService.searchAddress(userInfoUpdateRequestDTO.getAddress());
            if(addressSearchResponseDTO==null){
                throw new IllegalArgumentException("존재하지 않는 주소입니다. 정확한 주소를 입력해주세요.");
            }
            newAddress=addressSearchResponseDTO.getAddressName();
        }

        // 새로운 UserInfo 엔티티 생성
        String newNickname=(userInfoUpdateRequestDTO.getNickname()!=null)? userInfoUpdateRequestDTO.getNickname():oldUserInfo.getNickname();
        LocalDate newBirth=(userInfoUpdateRequestDTO.getBirth()!=null)? userInfoUpdateRequestDTO.getBirth():oldUserInfo.getBirth();
        Gender newGender=(userInfoUpdateRequestDTO.getGender()!=null)? userInfoUpdateRequestDTO.getGender():oldUserInfo.getGender();
        String newIntro=(userInfoUpdateRequestDTO.getIntro()!=null)?userInfoUpdateRequestDTO.getIntro():oldUserInfo.getIntro();

        UserInfo newUserInfo=oldUserInfo.toBuilder()
                .nickname(newNickname)
                .birth(newBirth)
                .gender(newGender)
                .address(newAddress)
                .intro(newIntro)
                .build();

        // DB 저장
        userInfoRepository.save(newUserInfo);

        // 리소스 ID 반환
        return userId;
    }

    /**
     * 닉네임 유일성 검사
     * @param nickname 닉네임
     * @return 유일성 여부
     */
    @Override
    public boolean isUniqueNickname(String nickname){
        return !userInfoRepository.existsByNickname(nickname);
    }
}
