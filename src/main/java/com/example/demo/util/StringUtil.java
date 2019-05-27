/*
 * Copyright (c) 2015, www.jd.com. All rights reserved.
 *
 * 警告：本计算机程序受著作权法和国际公约的保护，未经授权擅自复制或散布本程序的部分或全部、以及其他
 * 任何侵害著作权人权益的行为，将承受严厉的民事和刑事处罚，对已知的违反者将给予法律范围内的全面制裁。
 *
 */

package com.example.demo.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Description:字符串工具类
 *
 * @author gaoxingang
 * @version 1.0.0
 */
/*
 * =========================== 维护日志 ===========================
 * 2015-01-28 09:24  gaoxingang 新建代码 
 * 2016-09-20 mengxianglei 增加方法：replaceBadCharOfFileName
 * =========================== 维护日志 ===========================
 */
public class StringUtil {

    private StringUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * 判断字符串是否非空
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return StringUtils.isNotEmpty(str);
    }

    /**
     * <p>Checks if a CharSequence is whitespace, empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is null, empty or whitespace
     * @since 2.0
     * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
     */
    public static boolean isBlank(String str){
        return StringUtils.isBlank(str);
    }

    /**
     * <p>Checks if a CharSequence is not empty (""), not null and not whitespace only.</p>
     *
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param str  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is
     *  not empty and not null and not whitespace
     * @since 2.0
     * @since 3.0 Changed signature from isNotBlank(String) to isNotBlank(CharSequence)
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return StringUtils.isEmpty(str);
    }

    /**
     * 若字符串为空，则取默认值
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return StringUtils.defaultIfEmpty(str, defaultValue);
    }

    /**
     * 是否为数字（整数或小数）
     */
    public static boolean isNumber(String str) {
        return NumberUtils.isCreatable(str);
    }


    /**
     * 校验是否纯整数
     */
    public static boolean isIntegerNumber(String str)
    {
        Pattern pattern= Pattern.compile("^[1-9]\\d*$");
        Matcher isNum=pattern.matcher(str);
        if(!isNum.matches() ){
            return false;
        }
        return true;
    }

    /**
     * 校验是否为数字
     */
    public static boolean isNumeric(String number) {
        if (StringUtil.isNotBlank(number)){
            Pattern pattern = Pattern.compile("[0-9]*");
            Matcher matcher = pattern.matcher(number);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 分割固定格式的字符串
     */
    public static String[] splitString(String str, String separator) {
        return StringUtils.splitByWholeSeparator(str, separator);
    }

    /**
     * 将指定字符串去空格并转换成小写
     * @param str 目标字符串
     * @return 操作执行后的小写串
     */
    public static String toTrimAndLowerCase(String str){
        return EncodeUtil.hexEncodeString(str.trim().toLowerCase());
    }

    public static String replace(String email) {
        String userName = email.substring(0, email.indexOf("@"));
        if(userName.length()<=2){
            return new StringBuffer("**").append(email.substring(email.indexOf("@"))).toString();
        }
        StringBuilder sb = new StringBuilder(userName);
        for (int i = 1; i < userName.length() - 1; i++) {
            sb.setCharAt(i, '*');
        }
        sb.append(email.substring(email.indexOf("@")));
        return sb.toString();
    }

    /**
     * 去掉文件名中的无效字符,如 \r\n\ / : * ? " < > | 
     * 
     * @param fileName 待处理的文件名
     * @return 处理后的文件名
     */
    public static String replaceBadCharOfFileName(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return fileName;
        }
        String str=fileName;
        str=str.replace("\r",StringUtils.EMPTY);
        str=str.replace("\n",StringUtils.EMPTY);
        str=str.replace("\\",StringUtils.EMPTY);
        str=str.replace("/",StringUtils.EMPTY);
        str=str.replace(":",StringUtils.EMPTY);
        str=str.replace("*",StringUtils.EMPTY);
        str=str.replace("?",StringUtils.EMPTY);
        str=str.replace("\"",StringUtils.EMPTY);
        str=str.replace("<",StringUtils.EMPTY);
        str=str.replace(">",StringUtils.EMPTY);
        str=str.replace("|",StringUtils.EMPTY);
        str=str.replace(" ",StringUtils.EMPTY);    //前面的替换会产生空格,最后将其一并替换掉
        return str;
    }

    /*
    * 判断是否是日期类型的字符串
    * */
    public static boolean isDate(String date)
    {
        boolean isTrue=false;
        if(StringUtil.isBlank(date))
            return isTrue;

        try{
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd mm:hh:ss");
            sdf.parse(date);
            isTrue=true;
        }catch(Exception ex)
        {

        }
        return isTrue;
    }

    /**
     * 将姓名的第一个字母替换成“*”
     * @param name
     * @return
     */
    public static String dealName(String name){
        if(isBlank(name)){
            return name;
        }
        return name.replace(name.charAt(0), '*');

    }

}
