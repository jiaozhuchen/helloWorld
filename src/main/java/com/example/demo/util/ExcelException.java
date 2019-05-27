/*
 * Copyright (c) 2015, www.jd.com. All rights reserved.
 *
 * 警告：本计算机程序受著作权法和国际公约的保护，未经授权擅自复制或散布本程序的部分或全部、以及其他
 * 任何侵害著作权人权益的行为，将承受严厉的民事和刑事处罚，对已知的违反者将给予法律范围内的全面制裁。
 *
 */

package com.example.demo.util;

/**
 * Description:
 *
 * @author xiongqingfeng
 * @version 1.0.0
 */
/*
 * =========================== 维护日志 ===========================
 * 2015-05-04 15:01  xiongqingfeng 新建代码 
 * =========================== 维护日志 ===========================
 */
public class ExcelException extends Exception {

    private static final long serialVersionUID = -5380340740654027757L;

    public ExcelException(){
        super();
    }
    public ExcelException(String msg){
        super(msg);
    }
    public ExcelException(Throwable cause){
        super(cause);
    }
    public ExcelException(String msg, Throwable cause){
        super(msg,cause);
    }
} 

