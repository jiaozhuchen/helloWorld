/**
 * Copyright (c) 2015, www.jd.com. All rights reserved.
 *
 * 警告：本计算机程序受著作权法和国际公约的保护，未经授权擅自复制或散布本程序的部分或全部、以及其他
 * 任何侵害著作权人权益的行为，将承受严厉的民事和刑事处罚，对已知的违反者将给予法律范围内的全面制裁。
 */

package com.example.demo.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Description:各种格式的编码加码工具类.
 *
 * 集成Commons-Codec,Commons-Lang及JDK提供的编解码方法.
 *
 * @author liudong1
 * @author zhouen
 * @version 1.0.1
 */
/*
 * =========================== 维护日志 ===========================
 * 2015-03-05 18:02  liudong1 新建代码
 * 2015-03-06 14:43  zhouen 修改常量命名、添加私有构造函数和其他规范性问题
 * =========================== 维护日志 ===========================
 */
public final class EncodeUtil {

    private static final String DEFAULT_URL_ENCODING = "UTF-8"; //hex
    private static final String DEFAULT_HEX_ENCODING = "UTF-8"; //hex

    private EncodeUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Hex编码.默认UTF-8
     */
    public static String hexEncodeString(String input) {
        return hexEncodeString(input, DEFAULT_HEX_ENCODING);
    }

    /**
     * Hex编码.
     */
    public static String hexEncodeString(String input, String charset) {
        try {
            if (input == null) {
                return "";
            }
            return hexEncode(input.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Hex string encode exception", e);
        }
    }

    /**
     * Hex编码.
     */
    public static String hexEncode(byte[] input) {
        return Hex.encodeHexString(input);
    }

    /**
     * Hex解码.
     */
    public static String hexDecodeString(String input) {
        return hexDecodeString(input, DEFAULT_HEX_ENCODING);
    }

    /**
     * Hex解码.
     */
    public static String hexDecodeString(String input, String charset) {
        try {
            return new String(hexDecode(input), charset);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Hex string encode exception", e);
        }
    }

    /**
     * Hex解码.
     */
    public static byte[] hexDecode(String input) {
        try {
            return Hex.decodeHex(input.toCharArray());
        } catch (DecoderException e) {
            throw new IllegalStateException("Hex Decoder exception", e);
        }
    }

    /**
     * Base64编码.
     */
    public static String base64Encode(byte[] input) {
        return new String(Base64.encodeBase64(input));
    }

    /**
     * Base64编码, URL安全(将Base64中的URL非法字符如+,/=转为其他字符, 见RFC3548).
     */
    public static String base64UrlSafeEncode(byte[] input) {
        return Base64.encodeBase64URLSafeString(input);
    }

    /**
     * Base64解码.
     */
    public static byte[] base64Decode(String input) {
        return Base64.decodeBase64(input);
    }

    /**
     * URL 编码, Encode默认为UTF-8.
     */
    public static String urlEncode(String input) {
        if (input == null) {
            return null;
        }
        return urlEncode(input, DEFAULT_URL_ENCODING);
    }

    /**
     * URL 编码.
     */
    public static String urlEncode(String input, String encoding) {
        try {
            String str = URLEncoder.encode(input, encoding);
            return str.replaceAll("\\+", "%20"); //空格被替换成+,js是%20,所以此处将+替换成%20
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unsupported Encoding Exception", e);
        }
    }

    /**
     * URL 解码, Encode默认为UTF-8.
     */
    public static String urlDecode(String input) {
        return urlDecode(input, DEFAULT_URL_ENCODING);
    }

    /**
     * URL 解码.
     */
    public static String urlDecode(String input, String encoding) {
        try {
            return URLDecoder.decode(input, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unsupported Encoding Exception", e);
        }
    }

    /**
     * Html 转码.
     */
    public static String htmlEscape(String html) {
        return StringEscapeUtils.escapeHtml(html);
    }

    /**
     * Html 解码.
     */
    public static String htmlUnescape(String htmlEscaped) {
        return StringEscapeUtils.unescapeHtml(htmlEscaped);
    }

    public static String HTMLEncode(String txt)
    {
        String Ntxt = txt;
        if(StringUtils.isNotEmpty(txt)){
            Ntxt = Ntxt.replace(" ", "&nbsp;");
            Ntxt = Ntxt.replace("<", "&lt;");
            Ntxt = Ntxt.replace(">", "&gt;");
            Ntxt = Ntxt.replace("\"", "&quot;");
            Ntxt = Ntxt.replace("'", "&#39;");
            Ntxt = Ntxt.replace("\n", "<br>");
        }
        return Ntxt;
    }

    /**
     * Xml 转码.
     */
    public static String xmlEscape(String xml) {
        return StringEscapeUtils.escapeXml(xml);
    }

    /**
     * Xml 解码.
     */
    public static String xmlUnescape(String xmlEscaped) {
        return StringEscapeUtils.unescapeXml(xmlEscaped);
    }

} 
