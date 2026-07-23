package org.simulynx.fixora.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Shared Gson instance for the application. Java's module system blocks Gson's
 * default reflective (de)serialization of java.time types (e.g. LocalDateTime)
 * unless --add-opens java.base/java.time is granted, so we register explicit
 * adapters instead of relying on reflection.
 */
public final class GsonUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Gson INSTANCE = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                            src == null ? null : new com.google.gson.JsonPrimitive(FORMATTER.format(src)))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                            json == null || json.isJsonNull() ? null : LocalDateTime.parse(json.getAsString(), FORMATTER))
            .create();

    private GsonUtil() {
    }

    public static Gson getInstance() {
        return INSTANCE;
    }
}
