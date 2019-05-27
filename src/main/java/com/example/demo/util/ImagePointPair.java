/*
 * Copyright (c) 2016, www.jd.com. All rights reserved.
 *
 * 警告：本计算机程序受著作权法和国际公约的保护，未经授权擅自复制或散布本程序的部分或全部、以及其他
 * 任何侵害著作权人权益的行为，将承受严厉的民事和刑事处罚，对已知的违反者将给予法律范围内的全面制裁。
 *
 */

package com.example.demo.util;

/**
 * Description: base code
 * 
 * @author mengxianglei
 * @version 1.0.0
 */
/*
 * =========================== 维护日志 ===========================
 * 2016-09-12 09:22  mengxianglei 新建代码
 * =========================== 维护日志 ===========================
 */
public class ImagePointPair {
    /* 图片起始点横坐标（像素单位） */
    private int startX;
    /* 图片起始点纵坐标（像素单位） */
    private int startY;
    /* 图片宽度（像素单位） */
    private int width;
    /* 图片高度（像素单位） */
    private int height;

    public ImagePointPair(int startX, int startY, int endX, int endY) {
        super();
        this.startX = startX;
        this.startY = startY;
        width = endX - startX + 1;
        height = endY - startY + 1;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}