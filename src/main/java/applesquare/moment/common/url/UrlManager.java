package applesquare.moment.common.url;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UrlManager {
    @Value("${moment.front.domain}")
    private String frontDomain;
    @Value("${moment.back.domain}")
    private String backDomain;

    public String getUrl(UrlPath urlPath){
        UrlSource source=urlPath.getSource();
        switch(source){
            case FRONT:  // 프론트엔드 URL
                return frontDomain + urlPath.getPath();
            case BACK:  // 백엔드 URL
                return backDomain + urlPath.getPath();
            case EXTERNAL:  // 외부 URL
                return urlPath.getPath();
            default:  // 나머지는 예외 처리
                throw new RuntimeException("알 수 없는 URL 경로입니다. (path = "+urlPath.name()+")");
        }
    }
}
