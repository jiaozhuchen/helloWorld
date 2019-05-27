package com.example.demo.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * created by zhuyi8 on 2018/10/17
 */
public class SecurityUtil {

    /**
     * 对敏感信息经md5加密
     * 规则：敏感信息+seclog1359 后在进行md5加密
     * @param sourceStr
     * @return
     */
    public static String md5(String sourceStr) {
        String result = null;
        if(StringUtil.isBlank(sourceStr)){
            return "";
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update((sourceStr + "seclog1359").getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer();

            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }

                if (i < 16){
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

}
