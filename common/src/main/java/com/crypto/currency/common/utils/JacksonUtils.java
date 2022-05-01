package com.crypto.currency.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Panzi
 * @Description jackJSON serialize and deserialize
 * @date 2022/5/1 17:02
 */
public final class JacksonUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonUtils.class);

    private final static ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        MAPPER.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        MAPPER.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        MAPPER.configure(JsonParser.Feature.IGNORE_UNDEFINED, true);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        MAPPER.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        SimpleModule serializerModule = new SimpleModule();
        serializerModule.addSerializer(Long.class, ToStringSerializer.instance);
        serializerModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        MAPPER.registerModule(serializerModule);
    }

    /**
     * get the jackson mapper
     *
     * @return
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    /**
     * convert json to JsonNode.
     *
     * @param json
     * @return
     */
    public static JsonNode readTree(String json) {

        if (org.apache.commons.lang3.StringUtils.isBlank(json)) {
            return null;
        }

        try {
            return MAPPER.readTree(json);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return null;
    }

    /**
     * serialize obj to string
     *
     * @param obj
     * @return if error will return empty,so not Exception.
     */
    public static String toJson(Object obj) {

        try {
            return serialize(obj);
        } catch (Exception ex) {
            LOGGER.error("JacksonUtils serialize failed", ex);
            return org.apache.commons.lang3.StringUtils.EMPTY;
        }
    }

    /**
     * serialize obj to string
     *
     * @param obj
     * @return return json string
     * @throws Exception
     */
    public static String serialize(Object obj) {

        if (obj == null) {
            return org.apache.commons.lang3.StringUtils.EMPTY;
        }
        String jsonStr = org.apache.commons.lang3.StringUtils.EMPTY;
        try {
            jsonStr = MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return jsonStr;
    }

    /**
     * deserialize s to T
     *
     * @param s     the json string
     * @param clazz the T class
     * @param <T>   the T type
     * @return return T obj
     * @throws Exception
     */
    public static <T> T deserialize(String s, Class<T> clazz) {

        if (org.apache.commons.lang3.StringUtils.isBlank(s)) {
            return null;
        }
        T result = null;
        try {
            result = MAPPER.readValue(s, clazz);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return result;
    }

    /**
     * deserialize s to T
     *
     * @param s             the json string
     * @param typeReference actual type
     * @param <T>
     * @return
     * @throws JsonParseException   if underlying input contains invalid content
     *                              of type {@link JsonParser} supports (JSON for default case)
     * @throws JsonMappingException if the input JSON structure does not match structure
     *                              expected for result type (or has other mismatch issues)
     */
    public static <T> T deserialize(String s, TypeReference<T> typeReference) {

        if (StringUtils.isBlank(s)) {
            return null;
        }
        T result = null;
        try {
            result = (T)MAPPER.readValue(s, typeReference);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return result;
    }
}
