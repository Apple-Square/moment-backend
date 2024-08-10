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

    public static String toJson(Object object){
        return gson.toJson(object);
    }

    public static Map<String, String> fromJson(Reader reader, Type type){
        return gson.fromJson(reader, type);
    }
}
