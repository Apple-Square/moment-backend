package applesquare.moment.auth.service.impl;

import applesquare.moment.auth.dto.UserCreateRequestDTO;
import applesquare.moment.auth.model.UserAccount;
import applesquare.moment.auth.repository.UserAccountRepository;
import applesquare.moment.auth.service.AuthService;
import applesquare.moment.exception.DuplicateDataException;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import applesquare.moment.user.service.UserInfoService;
import applesquare.moment.util.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserAccountRepository userAccountRepository;
    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder encoder;


    /**
     * 회원가입
     * @param userCreateRequestDTO 회원가입 정보
     */
    @Override
    public void createUser(UserCreateRequestDTO userCreateRequestDTO){
        // 중복 검사
        String nickname= userCreateRequestDTO.getNickname();
        String username= userCreateRequestDTO.getUsername();
        String email= userCreateRequestDTO.getEmail();
        if(userInfoRepository.existsByNickname(nickname)){
            throw new DuplicateDataException("이미 존재하는 닉네임입니다. (nickname = "+nickname+")");
        }
        if(userAccountRepository.existsByUsername(username)){
            throw new DuplicateDataException("이미 존재하는 아이디입니다. (id = "+username+")");
        }
        if(userAccountRepository.existsByEmail(email)){
            throw new DuplicateDataException("이미 사용 중인 이메일입니다. (email = "+email+")");
        }

        // 이메일 유효성 검사
        /*
            실제로 존재하는 본인 소유의 이메일이 맞는지 확인
         */


        // 주소 유효성 검사
        /*
            실제로 존재하는 주소지인지 확인
         */


        // UserAccount, UserInfo 엔티티 생성
        String userId;
        do {
            userId= StringUtil.generateRandomString(UserInfoService.USER_ID_LENGTH);
        } while (userInfoRepository.existsById(userId));

        UserInfo userInfo=UserInfo.builder()
                .id(userId)
                .nickname(nickname)
                .birth(userCreateRequestDTO.getBirth())
                .gender(userCreateRequestDTO.getGender())
                .address(userCreateRequestDTO.getAddress())
                .build();

        // 비밀번호 해시값 생성
        String encodedPassword=encoder.encode(userCreateRequestDTO.getPassword());
        UserAccount userAccount=UserAccount.builder()
                .username(username)
                .password(encodedPassword)
                .email(email)
                .userInfo(userInfo)
                .build();

        // UserAccount, UserInfo는 OneToOne으로 연결되어 있기 때문에 UserAccount만 저장
        userAccountRepository.save(userAccount);
    }

    /**
     * 아이디 유일성 검사
     * @param username 아이디
     * @return 유일성 여부
     */
    @Override
    public boolean isUniqueUsername(String username){
        return !userAccountRepository.existsByUsername(username);
    }

    /**
     * 이메일 유일성 검사
     * @param email 이메일
     * @return 유일성 여부
     */
    @Override
    public boolean isUniqueEmail(String email){
        return !userAccountRepository.existsByEmail(email);
    }
}
