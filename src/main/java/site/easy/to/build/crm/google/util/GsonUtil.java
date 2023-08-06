package site.easy.to.build.crm.google.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.google.gson.JsonElement;

import java.io.Reader;
import java.lang.reflect.Type;

public class GsonUtil {
    private static final Gson gson = new Gson();

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static <T> T fromJson(JsonElement jsonElement, Type typeOfT) {
        return gson.fromJson(jsonElement, typeOfT);
    }

    public static JsonObject fromJson(String json) {
        return gson.fromJson(json, JsonObject.class);
    }

    public static <T> T fromJson(Reader reader, Class<T> classOfT) {
        return gson.fromJson(reader, classOfT);
    }

}
