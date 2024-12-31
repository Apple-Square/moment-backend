package applesquare.moment.auth.service.impl;

import applesquare.moment.address.dto.AddressSearchResponseDTO;
import applesquare.moment.address.service.AddressService;
import applesquare.moment.auth.dto.UserCreateRequestDTO;
import applesquare.moment.auth.model.UserAccount;
import applesquare.moment.auth.repository.UserAccountRepository;
import applesquare.moment.auth.service.AuthService;
import applesquare.moment.common.exception.DuplicateDataException;
import applesquare.moment.common.service.StateService;
import applesquare.moment.email.dto.MailDTO;
import applesquare.moment.email.exception.EmailValidationException;
import applesquare.moment.email.service.EmailSendService;
import applesquare.moment.user.model.UserInfo;
import applesquare.moment.user.repository.UserInfoRepository;
import applesquare.moment.user.service.UserInfoService;
import applesquare.moment.util.StringUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserAccountRepository userAccountRepository;
    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder encoder;
    private final AddressService addressService;
    private final StateService stateService;
    private final EmailSendService emailSendService;

    @Value("${moment.front.reset-password}")
    private String pwResetUrl;


    /**
     * 회원가입
     * @param userCreateRequestDTO 회원가입 정보
     */
    @Override
    public void createUser(UserCreateRequestDTO userCreateRequestDTO, String emailState){
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

        // 이메일이 입력된 경우,
        if(email!=null){
            // 이메일 인증 상태 검사
            String stateMetaData=stateService.getMetaData(emailState);
            if(stateMetaData==null || !stateMetaData.equals(email)){
                throw new EmailValidationException("이메일 인증 상태를 확인할 수 없습니다.");
            }

            // 중복 검사
            if(userAccountRepository.existsByEmail(email)){
                stateService.delete(emailState);
                throw new DuplicateDataException("이미 사용 중인 이메일입니다. (email = "+email+")");
            }
        }

        // 주소 유효성 검사 (실제로 존재하는 주소인지 검사)
        String address=null;
        if(userCreateRequestDTO.getAddress()!=null){
            AddressSearchResponseDTO addressSearchResponseDTO=addressService.searchAddress(userCreateRequestDTO.getAddress());
            if(addressSearchResponseDTO==null){
                throw new IllegalArgumentException("존재하지 않는 주소입니다. 정확한 주소를 입력해주세요.");
            }
            address=addressSearchResponseDTO.getAddressName();
        }

        // UserAccount, UserInfo 엔티티 생성
        String userId;
        do {
            userId= StringUtil.generateRandomString(StringUtil.USER_ID_CHARACTERS, UserInfoService.USER_ID_LENGTH);
        } while (userInfoRepository.existsById(userId));

        UserInfo userInfo=UserInfo.builder()
                .id(userId)
                .nickname(nickname)
                .birth(userCreateRequestDTO.getBirth())
                .gender(userCreateRequestDTO.getGender())
                .address(address)
                .social(false)
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

        // 이메일 인증 상태 제거하기 (일회용)
        if(email!=null){
            stateService.delete(emailState);
        }
    }

    /**
     * 계정 복구 메일 전송
     * @param email 이메일
     */
    @Override
    public void sendAccountRecoveryEmail(String email){
        // 이메일로 ID(=username) 찾기
        Optional<UserAccount> userAccountOptional = userAccountRepository.findByEmail(email);
        if(userAccountOptional.isEmpty()){
            // 만약 등록되지 않은 이메일이라면, 그냥 나가기
            // 예외를 던지지 않는 이유 : 해당 이메일을 사용하는 유저가 있는지 알려주는 것이 공격자들에게 힌트가 될 수 있기 때문
            return;
        }
        String username=userAccountOptional.get().getUsername();

        // PW 재설정 URL에 붙힐 토큰 생성
        String token= UUID.randomUUID().toString();
        stateService.create(token, username, PW_RESET_TOKEN_TTL_MINUTE, TimeUnit.MINUTES);

        // 계정 복구 메일 전송
        MailDTO mailDTO=MailDTO.builder()
                .toEmail(email)
                .title("[MOMENT] 계정 복구 메일")
                .message(
                        "안녕하세요.<br/><br/>" +
                        "회원님의 아이디는 " + username + " 입니다.<br/><br/>" +
                        "혹시 비밀번호를 모르시겠다면, 아래 링크를 이용해서 초기화해주세요.<br/><br/>" +
                        "<a href=\""+pwResetUrl+"?token=" + token + "\">비밀번호 재설정하러 가기</a>"
                )
                .useHtml(true)
                .build();

        emailSendService.send(mailDTO);
    }

    /**
     * 비밀번호 재설정
     * @param newPassword 새로운 비밀번호
     */
    @Override
    public void resetPassword(String username, String newPassword){
        UserAccount oldAccount=userAccountRepository.findByUsername(username)
                .orElseThrow(()-> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        // 비밀번호 해시값 생성
        String encodedPassword=encoder.encode(newPassword);

        // 비밀번호 변경 (pw는 반드시 해시값을 넣어줘야 함)
        UserAccount newAccount=oldAccount.toBuilder()
                .password(encodedPassword)
                .build();

        // DB 저장
        userAccountRepository.save(newAccount);
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
