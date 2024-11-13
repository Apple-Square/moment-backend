package applesquare.moment.common.exception;

import applesquare.moment.util.JsonUtil;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Getter
public class ResponseMap {
    private final Map<String, Object> map;

    public ResponseMap() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

        LocalDateTime now=LocalDateTime.now();
        map = new HashMap<>();
        map.put("timeStamp", now.format(formatter));
    }

    public void put(String key, Object value) {
        map.put(key, value);
    }

    public String toJson(){
        return JsonUtil.toJson(map);
    }
}
