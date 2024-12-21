package applesquare.moment.config;

import applesquare.moment.post.controller.PostReadType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToPostReadTypeConverter implements Converter<String, PostReadType> {

    @Override
    public PostReadType convert(String source) {
        return PostReadType.valueOf(source.toUpperCase());
    }
}