package io.jenkins.plugins.orka.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.logging.Logger;

class JsonHelper {
    private static final Logger logger = Logger.getLogger(JsonHelper.class.getName());

    public static <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        try {
            return new Gson().fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            String error = String.format("Failed to parse json: %s", json);
            logger.severe(error);

            throw e;
        }
    }
}
