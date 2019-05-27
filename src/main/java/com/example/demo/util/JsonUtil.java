/*
 * Copyright (c) 2015, www.jd.com. All rights reserved.
 *
 * 警告：本计算机程序受著作权法和国际公约的保护，未经授权擅自复制或散布本程序的部分或全部、以及其他
 * 任何侵害著作权人权益的行为，将承受严厉的民事和刑事处罚，对已知的违反者将给予法律范围内的全面制裁。
 *
 */

package com.example.demo.util;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Map;

/**
 * Description: JSON工具类
 * @author gaoxingang
 * @author zhouen
 * @version 1.0.2
 */
/*
 * =========================== 维护日志 ===========================
 * 2015-01-08  14:00  gaoxingang  基于历史代码新建代码
 * 2015-01-09  17:00  zhouen  优化代码：减少临时变量定义并在异常中输出参数
 * 2015-02-11  19:00  liudong 增加配置， null不输出， 设置日期类型
 * =========================== 维护日志 ===========================
 */

public final class JsonUtil {
    private static final Logger LOG = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();


    static {
        MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        MAPPER.configure(JsonParser.Feature.INTERN_FIELD_NAMES, true);
        MAPPER.configure(JsonParser.Feature.CANONICALIZE_FIELD_NAMES, true);
        MAPPER.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 设置输出包含的属性
        // 2015-02-11 liudong 值为null不输出
        MAPPER.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        // 2015-02-11 liudong 设置日期类型
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        MAPPER.getSerializationConfig().setDateFormat(df);
        MAPPER.getDeserializationConfig().setDateFormat(df);
    }

    private JsonUtil() {
        throw new UnsupportedOperationException();
    }

    public synchronized  static  ObjectMapper  getObjectMapper(){
        return MAPPER;
    }
    /**
     * 将 Java 对象转为 JSON 字符串
     */
    public static <T> String toJSON(T obj) {
        try {
            if (obj==null){
                return null;
            }
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            LOG.error("Java对象{}转JSON字符串出错！", obj, e);
            throw new RuntimeException(e);
        }
    }

    /**
     *  将 JSON 字符串转为 Java 对象
     * @param json
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T fromJSON(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            LOG.error("JSON字符串{}转Java对象出错！", json, e);
            LOG.error("fromJSON.json="+json);
        }
        return null;
    }

    /**
     * json转化成集合
     * @param <T>
     * @param <E>
     * @param json
     * @param collectionType 集合类型
     * @param elementType 集合中元素类型
     * @return
     */
    public static <T extends Collection<E>, E> T jsonToCollection(String json, Class<T> collectionType, Class<E> elementType) {
        JavaType type = getCollectionType(collectionType,elementType);
        try {
            if(null == jsonToGenericsType(json, type)){
                return null;
            }
            return (T)jsonToGenericsType(json, type);
        } catch (Exception e) {
            LOG.error("jsonToCollection error!", e);
            LOG.error("jsonToCollection.json="+json);
            throw new RuntimeException(e);
        }
    }

    /**
     * json转化成Map
     * @param <T>
     * @param <K>
     * @param <V>
     * @param json
     * @param mapType
     * @param keyType
     * @param valueType
     * @return
     */
    public static <T extends Map<K,V>, K, V> T jsonToMap(String json, Class<T> mapType, Class<K> keyType, Class<V> valueType) {
        JavaType type = getCollectionType(mapType,keyType,valueType);
        try {
            if(null == jsonToGenericsType(json, type)){
                return null;
            }
            return (T)jsonToGenericsType(json, type);
        } catch (Exception e) {
            LOG.error("jsonToMap error!", e);
            LOG.error("jsonToMap.json="+json);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取泛型的Collection Type
     * @param collectionClass 泛型的Collection
     * @param elementClasses 元素类
     * @return Java类型
     */
    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return MAPPER.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }


    /**
     * json串转化成带泛型的对象
     *
     * @param json
     * @param type
     * @param <T>
     * @return
     */
    private static <T> T jsonToGenericsType(String json, JavaType type) {
        try {
            if(null == MAPPER.readValue(json, type) ){
                return null;
            }
            return (T)MAPPER.readValue(json, type);
        } catch (Exception e) {
            LOG.error("jsonToGenericsBean error!", e);
            LOG.error("jsonToGenericsType.json="+json);
            throw new RuntimeException(e);
        }
    }
    
	/**
	 * Json 转换成 Object
	 * @param <T> 
	 * @param json json字符串
	 * @return Object
	 * @throws IOException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 */
	public static <T> T jsonToObject(String json, TypeReference<T> type) {
		
		try {
			return MAPPER.readValue(json, type);
		} catch (Exception e) {
			LOG.error("json parse error", e);
            LOG.error("jsonToObject.json="+json);
		}
		return null;
	}

}
