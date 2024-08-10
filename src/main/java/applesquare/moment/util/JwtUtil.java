package applesquare.moment.util;

import applesquare.moment.exception.TokenError;
import applesquare.moment.exception.TokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;

@Component
public class JwtUtil {
    public static final String REFRESH_TOKEN_COOKIE="refresh_token";
    private final long REFRESH_TOKEN_REISSUE_MS=1000*60*60*24*7;  //7일
    private final int ACCESS_TOKEN_EXPIRATION_SEC=60*30;  //30분
    private final int REFRESH_TOKEN_EXPIRATION_SEC=60*60*24*10;  //10일
    @Value("${applesquare.jwt.secret}")
    private String jwtSecret;


    /**
     * JWT 서명을 위한 HMAC SHA 키를 생성합니다.
     *
     * 이 메서드는 제공된 Base64로 인코딩된 비밀 키(jwtSecret)를 디코딩하여
     * HMAC SHA 키를 생성합니다. 이 키는 JSON Web Token(JWT)의 서명에 사용됩니다.
     *
     * @return HMAC SHA 서명을 위한 Key 객체
     */
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }


    /**
     * JWT 생성
     * @param subject 주체
     * @param expirationSecond 유효 시간 (second)
     * @return JWT 문자열
     */
    private String generateToken(String subject, int expirationSecond) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .setExpiration(Date.from(ZonedDateTime.now().plusSeconds(expirationSecond).toInstant()))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Access Token 생성
     * @param username username
     * @return Access Token
     */
    public String generateAccessToken(String username){
        return generateToken(username, ACCESS_TOKEN_EXPIRATION_SEC);
    }

    /**
     * Refresh Token을 담은 Http Only 쿠키 생성
     * @param username username
     * @return Refresh Token을 담은 Http Only 쿠키
     */
    public ResponseCookie generateRefreshCookie(String username){
        String refreshToken = generateToken(username, REFRESH_TOKEN_EXPIRATION_SEC);
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .path("/")
                .maxAge(REFRESH_TOKEN_EXPIRATION_SEC)
                .httpOnly(true)
                .build();
    }

    /**
     * 만료 기한이 지난 Refresh Token 생성
     * @return Refresh Token
     */
    public ResponseCookie generateClearRefreshCookie(){
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "clear_token")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();
    }


    /**
     * JWT 검증
     * @param token 토큰
     * @return 토큰 유효 여부
     * @throws TokenException TokenException
     */
    public boolean validateToken(String token) throws TokenException {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(token);
            return true;
        } catch (MalformedJwtException e) {
            throw new TokenException(TokenError.MALFORM);
        } catch (ExpiredJwtException e) {
            throw new TokenException(TokenError.EXPIRED);
        } catch(SignatureException signatureException){
            throw new TokenException(TokenError.BADSIGN);
        } catch (UnsupportedJwtException e) {
            throw new TokenException(TokenError.UNSUPPORTED);
        } catch (IllegalArgumentException e) {
            throw new TokenException(TokenError.UNACCEPT);
        }
    }

    /**
     * JWT 문자열에서 username을 추출
     * @param token JWT 문자열
     * @return username
     */
    public String getSubjectFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * JWT 문자열에서 만료 기한을 추출
     * @param token JWT 문자열
     * @return 만료 기한
     */
    public Date getExpirationFromToken(String token){
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getExpiration();
    }

    /**
     * Refresh 토큰의 재발급이 필요한지 확인
     * @param refreshToken Refresh 토큰
     * @return 재발급이 필요한지 여부
     */
    public boolean needNewRefreshToken(String refreshToken){
        // Refresh 토큰 만료까지 남은 시간 계산
        long expTime= getExpirationFromToken(refreshToken).getTime();
        long nowTime = System.currentTimeMillis();
        long diff = expTime - nowTime;

        return diff < REFRESH_TOKEN_REISSUE_MS;
    }
}
