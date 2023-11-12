package kz.monsha.taboobot.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class CallBackParams {
    @Getter
    @Setter
    private String action;
    private Map<String, String> map = new HashMap<>();

    public void put(String key, String val) {
        map.put(key, val);
    }

    public String getString(String key) {
        return map.getOrDefault(key, "");
    }

    public Long getLong(String key) {
        return Long.valueOf(map.get(key));
    }
}
