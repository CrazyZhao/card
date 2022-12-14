package com.bright.card.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description json--object转换工具
 */
public class JsonUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 对象字段全部列入
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        // 取消默认转换timestamps形式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 忽略空bean转json的错误
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 统一日期格式yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 忽略在json字符串中存在,但是在java对象中不存在对应属性的情况
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    private static ObjectMapper mapper = new ObjectMapper();

    public static Map<String, Object> jsonToMap(String json) {
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        Map result = null;
        try {
            result = (Map) mapper.readValue(json, HashMap.class);
        } catch (JsonParseException e) {
            logger.error("json解释错误", e);
        } catch (JsonMappingException e) {
            logger.error("json映射错误", e);
        } catch (IOException e) {
            logger.error("json转换是发生IO异常", e);
        }
        return result;
    }

    public static String toJson(Object obj) {
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, obj);
        } catch (JsonGenerationException e) {
            logger.error("json生成出错", e);
        } catch (JsonMappingException e) {
            logger.error("json映射错误", e);
        } catch (IOException e) {
            logger.error("json转换是发生IO异常", e);
        }
        return writer.toString();
    }

    /**
     * string转object
     *
     * @param str   json字符串
     * @param clazz 被转对象class
     * @param <T>
     * @return
     */
    public static <T> T string2Obj(String str, Class<T> clazz) {
        if (StringUtils.isEmpty(str) || clazz == null) {
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T) str : objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            System.out.println("Parse String to Object error");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将字符串转list对象
     *
     * @param <T>
     * @param jsonStr
     * @param cls
     * @return
     */
    public static <T> List<T> str2list(String jsonStr, Class<T> cls) {
        List<T> objList = null;
        try {
            JavaType t = objectMapper.getTypeFactory().constructParametricType(List.class, cls);
            objList = objectMapper.readValue(jsonStr, t);
        } catch (Exception e) {
        }
        return objList;
    }

}

