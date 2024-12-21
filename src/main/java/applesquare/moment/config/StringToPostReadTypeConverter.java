package applesquare.moment.config;

import applesquare.moment.post.controller.PostReadType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPostReadTypeConverter implements Converter<String, PostReadType> {

    @Override
    public PostReadType convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            // source 값이 들어오지 않았다면, 기본 설정 DETAIL 사용
            return PostReadType.DETAIL;
        }
        return PostReadType.valueOf(source.trim().toUpperCase());
    }
}