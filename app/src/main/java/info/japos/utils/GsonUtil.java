package info.japos.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Created by HWAHYUDI on 11/08/2017.
 */

public class GsonUtil {
    private static final Gson gInstance = new Gson();

    public static Gson getInstance() {
        return gInstance;
    }

    public static Integer getAsInteger(JsonObject object, String memberName) {
        final Number number = getAsNumber(object, memberName);
        return number == null ? null : number.intValue();
    }

    public static Long getAsLong(JsonObject object, String memberName) {
        final Number number = getAsNumber(object, memberName);
        return number == null ? null : number.longValue();
    }

    public static Short getAsShort(JsonObject object, String memberName) {
        final Number number = getAsNumber(object, memberName);
        return number == null ? null : number.shortValue();
    }

    public static Float getAsFloat(JsonObject object, String memberName) {
        final Number number = getAsNumber(object, memberName);
        return number == null ? null : number.floatValue();
    }

    public static Double getAsDouble(JsonObject object, String memberName) {
        final Number number = getAsNumber(object, memberName);
        return number == null ? null : number.doubleValue();
    }

    public static Byte getAsByte(JsonObject object, String memberName) {
        final Number number = getAsNumber(object, memberName);
        return number == null ? null : number.byteValue();
    }

    public static Number getAsNumber(JsonObject object, String memberName) {
        final JsonPrimitive primitive = getAsJsonPrimitive(object, memberName);
        return primitive != null && primitive.isNumber()
                ? primitive.getAsNumber()
                : null;
    }

    public static Boolean getAsBoolean(JsonObject object, String memberName) {
        final JsonPrimitive primitive = getAsJsonPrimitive(object, memberName);
        return primitive != null && primitive.isBoolean()
                ? primitive.getAsBoolean()
                : null;
    }

    public static Character getAsCharacter(JsonObject object, String memberName) {
        final JsonPrimitive primitive = getAsJsonPrimitive(object, memberName);
        return primitive != null && primitive.isString() && primitive.getAsString().length() == 1
                ? primitive.getAsCharacter()
                : null;
    }

    public static String getAsString(JsonObject object, String memberName) {
        final JsonPrimitive primitive = getAsJsonPrimitive(object, memberName);
        return primitive != null && primitive.isString()
                ? primitive.getAsString()
                : null;
    }

    public static JsonPrimitive getAsJsonPrimitive(JsonObject object, String memberName) {
        final JsonElement element = object.get(memberName);
        return element != null && element.isJsonPrimitive()
                ? element.getAsJsonPrimitive()
                : null;
    }

    public static JsonNull getAsJsonNull(JsonObject object, String memberName) {
        final JsonElement element = object.get(memberName);
        return element != null && element.isJsonNull()
                ? element.getAsJsonNull()
                : null;
    }

    public static JsonArray getAsJsonArray(JsonObject object, String memberName) {
        final JsonElement element = object.get(memberName);
        return element != null && element.isJsonArray()
                ? element.getAsJsonArray()
                : null;
    }

    public static JsonObject getAsJsonObject(JsonObject object, String memberName) {
        final JsonElement element = object.get(memberName);
        return element != null && element.isJsonObject()
                ? element.getAsJsonObject()
                : null;
    }
}
