package com.laptrinhjavaweb.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

public class HttpUtil {
    private static ObjectMapper mapper = new ObjectMapper();
    private String value;

    static {
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }

    public HttpUtil(String value) {
        this.value = value;
    }

    public <T> T toObject(Class<T> clazz) {
        T model = null;
        try {
            model = mapper.readValue(value, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return model;
    }

    public static HttpUtil of(BufferedReader reader) {
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new HttpUtil(sb.toString());
    }

    public static void writeValue(OutputStream out, Object value) {
        try {
            mapper.writeValue(out, value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String writeValueAsString(Object value) {
        if (value == null) {
            return "{}";
        }
        try {
            return mapper.writeValueAsString(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "{}";
    }
}