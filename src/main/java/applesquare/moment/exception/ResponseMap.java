package applesquare.moment.exception;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Getter
public class ResponseMap {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final Map<String, Object> map;

    public ResponseMap() {
        map = new HashMap<>();
        map.put("timeStamp", LocalDateTime.now().format(formatter));
    }

    public void put(String key, Object value) {
        map.put(key, value);
    }
}
