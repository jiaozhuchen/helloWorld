package com.example.demo.util;

import java.util.Date;

/**
 * Created on 2012-7-17
 * @author:xuzongpeng
 * <p>Description: [NULL处理工具类]</p>
 * @version     1.0
 */
public class NullUtil {

	/**
	 * <p>Description:[处理对象数据]</p>
	 * @param source 需要被处理的对象
	 * @param target source为null时需要被转换成的字符串
	 * @return
	 * [经过处理后的字符串]
	 */
	public static String nullToString(Object source, String target) {
		return source == null ? target : String.valueOf(source);
	}
	/**
	 * <p>Description:[处理Integer数据]</p>
	 * @param source 需要被处理的Integer数据
	 * @param target source为null时需要被转换成的Integer数据
	 * @return
	 * [经过处理后的Integer数据]
	 */
	public static Integer nullToInteger(Integer source, Integer target) {
		return source == null ? target : source;
	}
	/**
	 * <p>Description:[处理Long数据]</p>
	 * @param source 需要被处理的Long数据
	 * @param target source为null时需要被转换成的Long数据
	 * @return
	 * [经过处理后的Long数据]
	 */
	public static Long nullToLong(Long source, Long target) {
		return source == null ? target : source;
	}
	/**
	 * <p>Description:[处理Float数据]</p>
	 * @param source 需要被处理的Float数据
	 * @param target source为null时需要被转换成的Float数据
	 * @return
	 * [经过处理后的Float数据]
	 */
	public static Float nullToFloat(Float source, Float target) {
		return source == null ? target : source;
	}
	/**
	 * <p>Description:[处理Double数据]</p>
	 * @param source 需要被处理的Double数据
	 * @param target source为null时需要被转换成的Double数据
	 * @return
	 * [经过处理后的Double数据]
	 */
	public static Double nullToDouble(Double source, Double target) {
		return source == null ? target : source;
	}
	/**
	 * <p>Description:[处理Date数据]</p>
	 * @param source 需要被处理的Date数据
	 * @param target source为null时需要被转换成的Date数据
	 * @return
	 * [经过处理后的Date数据]
	 */
	public static Date nullToDate(Date source, Date target) {
		return source == null ? target : source;
	}
}
