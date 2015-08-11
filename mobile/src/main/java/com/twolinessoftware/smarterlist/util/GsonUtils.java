package com.twolinessoftware.smarterlist.util;

import android.graphics.Color;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.twolinessoftware.smarterlist.Constants;
import com.twolinessoftware.smarterlist.model.MasterSmartListItem;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class GsonUtils {

     public static Gson buildGsonAdapter() {

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .registerTypeAdapter(MasterSmartListItem.class, new MasterSmartListItemDeserializer())
                // https://code.google.com/p/google-gson/issues/detail?id=281
                .registerTypeAdapter(Date.class, new GmtDateTypeAdapter())
                .create();

        return gson;



    }


    private static class MasterSmartListItemDeserializer  implements JsonDeserializer<MasterSmartListItem> {

        @Override
        public MasterSmartListItem deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

            JsonObject jsonObject = jsonElement.getAsJsonObject();

            String rawJson = jsonObject.toString();

            MasterSmartListItem item = new Gson().fromJson(rawJson,MasterSmartListItem.class);

            JsonObject categoryObject = jsonObject.getAsJsonObject("category");
            String categoryName = categoryObject.get("name").getAsString();
            Long categoryId = categoryObject.get("id").getAsLong();
            String categoryColor = categoryObject.get("color").getAsString();
            String categoryIconUrl = categoryObject.get("iconUrl").getAsString();

            item.setCategoryId(categoryId);
            item.setCategoryName(categoryName);
            item.setCategoryColor(Color.parseColor(categoryColor));
            item.setCategoryIconUrl(categoryIconUrl);

            return item;
        }

    }

    private static class GmtDateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
        private final DateFormat dateFormat;

        private GmtDateTypeAdapter() {
            dateFormat = Constants.getDateFormat();
        }

        @Override
        public synchronized JsonElement serialize(Date date, Type type,
                                                  JsonSerializationContext jsonSerializationContext) {
            synchronized (dateFormat) {
                String dateFormatAsString = dateFormat.format(date);
                return new JsonPrimitive(dateFormatAsString);
            }
        }

        @Override
        public synchronized Date deserialize(JsonElement jsonElement, Type type,
                                             JsonDeserializationContext jsonDeserializationContext) {
            try {
                synchronized (dateFormat) {
                    return dateFormat.parse(jsonElement.getAsString());
                }
            } catch (ParseException e) {
                throw new JsonSyntaxException(jsonElement.getAsString(), e);
            }
        }
    }
}
