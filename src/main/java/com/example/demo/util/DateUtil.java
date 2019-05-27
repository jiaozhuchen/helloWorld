/*
 * Copyright (c) 2015, www.jd.com. All rights reserved.
 *
 * 警告：本计算机程序受著作权法和国际公约的保护，未经授权擅自复制或散布本程序的部分或全部、以及其他
 * 任何侵害著作权人权益的行为，将承受严厉的民事和刑事处罚，对已知的违反者将给予法律范围内的全面制裁。
 *
 */
package com.example.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Description: 日期格式处理工具类
 *
 * @author pudongping
 * @author zhouen
 * @version 1.1.1
 */
/*
 * =========================== 维护日志 ===========================
 * 2015-01-27 16:06  pudongping 新建代码
 * 2015-01-30 10:36  zhouen  规范化代码
 * 2015-02-12 14:00  zhouen  修改原SimpleDateFormat作为静态常量方式实现getCurrentDatetime()的方式。
 *                           此修改主要目的在于规避多线程安全性模式（即可能使得SimpleDateFormat作为静态常量方式被滥用）风险。
 *                           可以使用FastDateFormat或者joda来做更好。
 * 2015-03-11 20:00  liudong 增加方法 getDayBetween getYearMonthListBefore getBeginTimeOfMonth getEndTimeOfMonth
 * 2016-08-22 18:45  lichangqign3 getDayBetweenMinute 
 * =========================== 维护日志 ===========================
 */
public class DateUtil {

    private static final Logger LOG = LoggerFactory.getLogger(DateUtil.class);

    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_DAY_PATTERN = "yyyy-MM-dd";

    private DateUtil() {
        throw new UnsupportedOperationException();
    }

    public static String formatDate(Date date) {
        return formatDate(date, DEFAULT_DATE_PATTERN);
    }

    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            throw new IllegalArgumentException("date is null");
        }

        if (pattern == null) {
            throw new IllegalArgumentException("pattern is null");
        }

        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.CHINA);
        return formatter.format(date);
    }

    /**
     * 获得账期(年份和月份 例如 201510)
     * @return
     */
    public static String getAccountPeriod(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        String accountPeriod = formatDate(c.getTime(), "yyyyMM");
        return accountPeriod;
    }
    public static Date getFistDayOfMonth(Date d){
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.DAY_OF_MONTH,1);
        return c.getTime();
    }
    public static Date getLastDayOfMonth(Date d){
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.DAY_OF_MONTH,c.getActualMaximum(Calendar.DAY_OF_MONTH));
        return c.getTime();
    }
    public static String getLastTwoMonthAccountPeriod(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -2);
        String accountPeriod = formatDate(c.getTime(), "yyyyMM");
        return accountPeriod;
    }

    public static int getLastTwoMonth(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -2);
        return c.get(Calendar.MONTH)+1;
    }

    public static int getLastMonth(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        return c.get(Calendar.MONTH)+1;
    }

    /**
     * 将当前时间转换为账期格式(年份和月份 例如 )
     * @return
     */
    public static String getCurrentAccountPeriod(){
        Calendar c = Calendar.getInstance();
        String accountPeriod = formatDate(c.getTime(), "yyyyMM");
        return accountPeriod;
    }

    public static XMLGregorianCalendar convertToXMLGregorianCalendar(Date date) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        XMLGregorianCalendar gc = null;

        try {
            gc = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (Exception e) {
            LOG.error("转换时间出现问题",e);
        }

        return gc;
    }

    public static Date convertToDate(XMLGregorianCalendar cal) throws Exception {
        GregorianCalendar ca = cal.toGregorianCalendar();
        return ca.getTime();
    }

    /**
     * 将时间格式的字符串 比如 2015年4月8日16:17:07或 2014-04-06 16:17:07转换为 日期格式的时间
     * @param strDate
     * @return
     */
    public static Date StringToDate(String strDate) {
        Date date=null;
        SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date=formatter.parse(strDate);
        } catch (ParseException e) {
            LOG.error("字符串转换为时间出现问题",e);
        }
        return date;
    }

    /**
     * 字符串转化为时间
     * @param dateStr
     * @param pattern
     * @return
     */
    public static Date stringToDate(String dateStr, String pattern){
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            date = format.parse(dateStr);
        } catch (ParseException e) {
            LOG.error("时间转化异常! dateStr = " + dateStr + ", pattern = " + pattern);
        }
        return date;
    }



    /**
     * 获取当前日期与时间
     */
    public static String getCurrentDatetime() {
        // 2015-02-12 14:00  zhouen  修改原SimpleDateFormat作为静态常量方式实现getCurrentDatetime()的方式。
        //                           此修改主要目的在于规避多线程安全性模式（即可能使得SimpleDateFormat作为静态常量方式被滥用）风险。
        //                           可以使用FastDateFormat或者joda来做更好。
            return formatDate(new Date());
    }
    // 获得本周一0点时间
    public static Date getCurrentWeekMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getCurrentMonthDatetime() {
        Calendar c = Calendar.getInstance();
        c.set(c.get(Calendar.YEAR),c.get(Calendar.MONTH),1,0,0,0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static String getCurrentMonthDatetimeStr() {
        Calendar c = Calendar.getInstance();
        c.set(c.get(Calendar.YEAR),c.get(Calendar.MONTH),1,0,0,0);
        c.set(Calendar.MILLISECOND, 0);
        return formatDate(c.getTime());
    }
    /**
     * 比较时间 d1 > d2 ?
     * @param d1
     * @param d2
     * @return
     */
    public static boolean compareDate(Date d1, Date d2) {
        return d1 != null && d2 != null && d1.getTime() > d2.getTime();
    }

    /**
     * 比较时间 d1 >= d2 ?
     * @param d1
     * @param d2
     * @return
     */
    public static boolean compareDateEq(Date d1, Date d2) {
        return d1 != null && d2 != null && d1.getTime() >= d2.getTime();
    }

    /**
     * d1距d2多少天
     *       2015-03-09 距离 2015-03-11 2天
     *       2015-03-10 距离 2015-03-11 1天
     *       2015-03-11 距离 2015-03-11 0天
     *
     * @param d1
     * @param d2
     * @return
     */
    public static long getDayBetween(Date d1, Date d2){
        if (d1 == null || d2 == null || d1.getTime() >= d2.getTime()) {
            return 0;
        } else {
            Calendar c1 = Calendar.getInstance();
            c1.setTime(d1);
            c1.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            Calendar c2 = Calendar.getInstance();
            c2.setTime(d2);
            c2.set(c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            return (c2.getTime().getTime()/1000 - c1.getTime().getTime()/1000) / (60 * 60 * 24);
        }
    }

    /**
     * 前xx月，含当前月
     *        beforeCount = 12  now 2015-3
     *        [2015-3, 2015-2, 2015-1, 2014-12, 2014-11, 2014-10, 2014-9, 2014-8, 2014-7, 2014-6, 2014-5, 2014-4]
     * @param beforeCount
     * @return
     */
    public static List<String> getYearMonthListBefore(int beforeCount){
        List<String> list = new ArrayList<String>(beforeCount);
        Calendar c1 = Calendar.getInstance();
        c1.setTime(new Date());
        int tempYear = c1.get(Calendar.YEAR);
        int tempMonth = c1.get(Calendar.MONTH) + 1;
        for(int i = 0; i < beforeCount; i++){
            if(tempMonth < 10){
                list.add(tempYear + "-0" + tempMonth);
            }else{
                list.add(tempYear + "-" + tempMonth);
            }
            tempMonth--;
            if(tempMonth == 0){
                tempYear--;
                tempMonth = 12;
            }
        }
        return list;
    }

    /**
     * 获得 yyyy-MM 的月初 时间
     *         yearMonth 2015-3 -->  2015-03-01 00:00:00
     * @param yearMonth
     * @return
     */
    public static Date getBeginTimeOfMonth(String yearMonth){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            return format.parse(yearMonth + "-01 00:00:00");
        } catch (ParseException e) {
            LOG.error("获得 yyyy-MM 的月初 时间异常！yearMonth = " + yearMonth, e);
        }

        return new Date();
    }


    /**
     * 获取date的第一天
     * @param format
     * @return
     */
    public static String getBeginTimeOfMonthByCurrentDate(String format){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            //获取前月的第一天
            Calendar cal_1= Calendar.getInstance();//获取当前日期
            //cal_1.add(Calendar.MONTH, -1);
            cal_1.set(Calendar.DAY_OF_MONTH,1);//设置为1号,当前日期既为本月第一天
            String firstDay = simpleDateFormat.format(cal_1.getTime());
            return firstDay;
        } catch (Exception e) {
            LOG.error("获得 当前月的第一天的 时间异常！" , e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前日期
     */
    public static String getCurrentDate(String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            return simpleDateFormat.format(new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获得 yyyy-MM 的月初 时间
     *         yearMonth 2015-3 -->  2015-03-01
     * @param yearMonth
     * @return
     */
    public static String getBeginTimeOfMonthFormat(String yearMonth){
        try {
            return yearMonth + "-01";
        } catch (Exception e) {
            LOG.error("获得 yyyy-MM 的月初 时间异常！yearMonth = " + yearMonth, e);
        }
        return null;
    }

    /**
     * 获得 yyyy-MM 的月末 时间
     *         yearMonth 2015-3 -->  2015-03-31
     * @param yearMonth
     * @return
     */
    public static String getEndTimeOfMonthFormat(String yearMonth){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format.parse(yearMonth + "-01"));
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
            calendar.set(Calendar.DAY_OF_MONTH, 0);
        } catch (ParseException e) {
            LOG.error("获得 yyyy-MM 的月末 时间异常！yearMonth = " + yearMonth, e);
        }

        return format.format(calendar.getTime());
    }

    /**
     * 获得 yyyy-MM 的月末 时间
     *         yearMonth 2015-3 -->  2015-03-31 23:59:59
     * @param yearMonth
     * @return
     */
    public static Date getEndTimeOfMonth(String yearMonth){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format.parse(yearMonth + "-01 23:59:59"));
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
            calendar.set(Calendar.DAY_OF_MONTH, 0);
        } catch (ParseException e) {
            LOG.error("获得 yyyy-MM 的月末 时间异常！yearMonth = " + yearMonth, e);
        }

        return calendar.getTime();
    }

    /**
     * 根据传入的日期，向前推 或向后退多少天
     * @param date
     * @param days
     * @return
     */
    public static Date AddDayOrDeleteDay(Date date, Integer days){
        Date dNow = date;   //当前时间
        Date dBefore = new Date();

        Calendar calendar = Calendar.getInstance(); //得到日历
        calendar.setTime(dNow);//把当前时间赋给日历
        calendar.add(Calendar.DAY_OF_MONTH, days);  //设置为前一天
        dBefore = calendar.getTime();   //得到前一天的时间
        return dBefore;
    }

    /**
     * 根据传入的日期，向前推 或向后退多少天
     * @param date yyyy-MM-dd
     * @param days
     * @return
     */
    public static String addOrDeleteDay(String date, Integer days){
        Date dNow = stringToDate(date,DEFAULT_DAY_PATTERN);   //当前时间
        Date dBefore = new Date();

        Calendar calendar = Calendar.getInstance(); //得到日历
        calendar.setTime(dNow);//把当前时间赋给日历
        calendar.add(Calendar.DAY_OF_MONTH, days);  //设置为前一天
        dBefore = calendar.getTime();   //得到前一天的时间
        return formatDate(dBefore,DEFAULT_DAY_PATTERN);
    }

    // 得到几天前的时间
    public static Date getDateBefore(Date d, int day){
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
        return now.getTime();
    }
    public static Date getDateWeekBefore(Date d, int week){
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.setFirstDayOfWeek(Calendar.MONDAY);
        now.set(Calendar.WEEK_OF_YEAR, now.get(Calendar.WEEK_OF_YEAR) - week);
        return now.getTime();
    }
    public static Date getDateWeekAfter(Date d, int week){
        Calendar now = Calendar.getInstance();
        now.setFirstDayOfWeek(Calendar.MONDAY);
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.WEEK_OF_YEAR) + week);
        return now.getTime();
    }
    public static Date getDateMonthBefore(Date d, int month){
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.MONTH, now.get(Calendar.MONTH) - month);
        return now.getTime();
    }
    public static Date getDateMonthafter(Date d, int month){
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.MONTH, now.get(Calendar.MONTH) +month);
        return now.getTime();
    }
    // 得到几天后的时间
    public static Date getDateAfter(Date d, int day){
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
        return now.getTime();
    }

    /**
     * 获得某天的零点 2014-08-14 00:00:00
     * @return
     */
    public static Date getDayStart(Date date){
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        return now.getTime();
    }

    /**
     * 获得某天的 2014-08-14 23:59:59
     * @param date
     * @return
     */
    public static Date getDayEnd(Date date){
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        return now.getTime();
    }

    /**
     * 获取上一个月Date
     *
     * @return 上一个月Date
     */
    public static Date getLastMonthDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        return cal.getTime();
    }

    /**
     * 获取当月的天数
     *
     * @param date
     * @return 当月的天数
     */
    public static int getLastDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    public static String getMaxDayOfMonth(String dates, String patten) {
        Date date=DateUtil.stringToDate(dates,patten);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, getLastDay(date));
        return DateUtil.formatDate(cal.getTime(),"yyyyMMdd");
    }
    public static int getHour(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY);
    }
    public static int getMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH)+1;
    }
    public static int getWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        return cal.get(Calendar.WEEK_OF_YEAR);
    }
    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    /**
     * 获取两个时间相差的分钟数
     *
     * @param smallDate
     * @param bigDate
     * @return
     */
    public static long getDayBetweenMinute(Date smallDate, Date bigDate) {
        if (smallDate == null || bigDate == null) {
            return -1;
        }
        long diff = ((bigDate.getTime() - smallDate.getTime())/1000)/60;
        return diff;
    }


    /**
     *
     * 获取本月的第一天
     * @param format
     * @return
     */
    public static String getNowTimeOfMonthByCurrentDate(String format){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            //获取本月的第一天
            Calendar c= Calendar.getInstance();//获取当前日期
            c.add(Calendar.MONTH, 0);
            c.set(Calendar.DAY_OF_MONTH,1);//设置为1号,当前日期既为本月第一天
            String firstDay = simpleDateFormat.format(c.getTime());
            return firstDay;
        } catch (Exception e) {
            LOG.error("获得 本月的第一天的 时间异常！" , e);
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 给当前时间加一天
     * @param time
     * @return
     */

    public static Date getAfterTime(Date time) {
        Calendar para = Calendar.getInstance();
        para.setTime(time);
        //给当前时间增加一天
        para.add(Calendar.DATE, 1);
        return para.getTime();
    }
}

