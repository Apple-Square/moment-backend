package applesquare.moment.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Map;

public class JsonUtil {
    private static final Gson gson=new GsonBuilder()
            .registerTypeAdapter(LocalDate .class, new LocalDateAdapter()).create();

    /**
     * 특정 객체를 Json 문자열로 직렬화
     * @param object 객체
     * @return Json 문자열
     */
    public static String toJson(Object object){
        return gson.toJson(object);
    }

    /**
     * 특정 Reader에 담긴 특정 타입의 Json을 Map으로 역직렬화
     * @param reader Reader
     * @param type Reader에 담긴 객체 타입
     * @return Json을 역직렬화한 Map
     */
    public static Map<String, String> fromJson(Reader reader, Type type){
        return gson.fromJson(reader, type);
    }
}
