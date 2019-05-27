/*
 * Copyright (c) 2015, www.jd.com. All rights reserved.
 *
 * 警告：本计算机程序受著作权法和国际公约的保护，未经授权擅自复制或散布本程序的部分或全部、以及其他
 * 任何侵害著作权人权益的行为，将承受严厉的民事和刑事处罚，对已知的违反者将给予法律范围内的全面制裁。
 *
 */

package com.example.demo.util;

import com.jd.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.*;
import java.util.Enumeration;

/**
 * Description: IP工具类
 * @author pudongping
 * @author zhouen
 * @version 1.1.0
 */
/*
 * =========================== 维护日志 ===========================
 * 2015-01-08  14:00  pudongping  基于历史代码新建代码
 * 2015-01-09  17:00  zhouen  规范化代码，添加异常日志
 * =========================== 维护日志 ===========================
 */

public class IPUtil {

    private static final Logger LOG = LoggerFactory.getLogger(IPUtil.class);

    private IPUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取客户端真实IP地址
     *
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String ip = request.getHeader("J-Forwarded-For");
        if (com.jd.common.util.StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("x-forwarded-for");
        }
        if (com.jd.common.util.StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (com.jd.common.util.StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (com.jd.common.util.StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (("127.0.0.1").equals(ip)) {
                ip = getLocalIPAdress();
            }
        }
        if (ip != null && ip.length() > 15) {
            int location = ip.indexOf(",");
            if (location > 0) {
                ip = ip.substring(0, location);
            }
        }
        return ip;
    }

    /**
     * 获取服务器的ip（new）
     *
     * @return
     * @throws UnknownHostException
     */
    public static String getLocalIPAdress() {

        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = (InetAddress) addresses.nextElement();
                    if (ip != null
                            && ip instanceof Inet4Address
                            && !ip.isLoopbackAddress() //loopback地址即本机地址，IPv4的loopback范围是127.0.0.0 ~ 127.255.255.255
                            && ip.getHostAddress().indexOf(":") == -1) {
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获得本地IP地址：未能获得则返回"未获取到IP地址！"
     * @return
     */
    public static String getLocalIp() {
        try {
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements();) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                if (addresses.hasMoreElements()) {
                    return addresses.nextElement().toString().split("/")[1];
                }
            }
        } catch (SocketException e) {
            LOG.error("获取本地IP出现错误！", e);
        }
        
        return "未获取到IP地址！" ;
    }

}
