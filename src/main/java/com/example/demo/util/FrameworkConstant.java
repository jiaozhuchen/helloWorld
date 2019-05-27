/*
 * Copyright (c) 2015, www.jd.com. All rights reserved.
 *
 * 警告：本计算机程序受著作权法和国际公约的保护，未经授权擅自复制或散布本程序的部分或全部、以及其他
 * 任何侵害著作权人权益的行为，将承受严厉的民事和刑事处罚，对已知的违反者将给予法律范围内的全面制裁。
 *
 */

package com.example.demo.util;

import java.util.Properties;

/**
 * Description:框架常量
 *
 * @author gaoxingang
 * @version 1.0.0
 */
/*
 * =========================== 维护日志 ===========================
 * 2015-01-28 17:57  gaoxingang 新建代码 
 * =========================== 维护日志 ===========================
 */
public class FrameworkConstant {

    public static final String COMMONS_PROPS = "commons.properties";

    public static final String IMPORTANT_PROPS = "important.properties";

    public static final Properties importantProps = PropsUtil.loadProps(IMPORTANT_PROPS);

    public static final Properties commonsProps = PropsUtil.loadProps(COMMONS_PROPS);

    public static final String SYSTEM_CREATER = "system"; // 后台系统创建人名称

    public static final boolean IS_DEBUG = true;      //是否处在调试状态

    public static final int RESPONSE_TIME_DEFAULT = 200;  //默认响应时间

}
