/**
 * Copyright (c) 2016, www.jd.com. All rights reserved.
 *
 * 警告：本计算机程序受著作权法和国际公约的保护，未经授权擅自复制或散布本程序的部分或全部、以及其他
 * 任何侵害著作权人权益的行为，将承受严厉的民事和刑事处罚，对已知的违反者将给予法律范围内的全面制裁。
 */
package com.example.demo.util;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.velocity.tools.generic.NumberTool;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Description:用于前台处理数字类型比如：保留2位小数不进行四舍五入
 *
 * @author wangxiaocong
 * @version 1.0.0
 */
/*
 * =========================== 维护日志 ===========================
 * 2016年2月2日  wangxiaocong 新建代码 
 * =========================== 维护日志 ===========================
 */
public class NumericTool {
	/**
	 * 将浮点数小数，固定保留两位小数 
	 * @param d
	 * @return
	 */
    public static String toFixedNumber(Number d) {
        if (d == null) {  
            return "";  
        }
        DecimalFormat f = new DecimalFormat("##0.00");
		f.setRoundingMode(RoundingMode.DOWN);
        return f.format(d);  
    }

    public static String toFixedAmount(Number d) {
		if (d == null) {
			return "";
		}
		DecimalFormat f = new DecimalFormat("##0.00");
		f.setRoundingMode(RoundingMode.HALF_UP);
		return f.format(d);
	}

	public static String sub(Number d1, Number d2){
		BigDecimal b1 = new BigDecimal(d1.toString());
		BigDecimal b2 = new BigDecimal(d2.toString());
		BigDecimal result = b1.subtract(b2);
		return result.setScale(2, BigDecimal.ROUND_DOWN).toString();
	}
	
	public static boolean isNumber(String val){
		if(NumberUtils.isNumber(val)){
			return true;
		}else {
			return false;
		}
	}

	public static BigDecimal formatDecimal(BigDecimal bigDecimal, int scale){
		if(bigDecimal==null ||bigDecimal.compareTo(BigDecimal.ZERO) <= 0){
			return  new BigDecimal(0.00).setScale(2);
		}
		return  bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_DOWN);
	}
    
    public static void main(String[] args) {


    	NumberTool nt = new NumberTool();
    	Double d = 32.569328;
		System.out.println(nt.format("#0.00", d));
		DecimalFormat f = new DecimalFormat("#,##0.00");
		f.setRoundingMode(RoundingMode.DOWN);
		System.out.println(f.format(d));

		NumericTool numericTool = new NumericTool();
		//numericTool.toFixedNumber()
		System.out.printf(formatDecimal(new BigDecimal(0.0),2).toString());
	}
}
