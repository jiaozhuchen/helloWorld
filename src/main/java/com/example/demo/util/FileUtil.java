/*
 * Copyright (c) 2015, www.jd.com. All rights reserved.
 *
 * 警告：本计算机程序受著作权法和国际公约的保护，未经授权擅自复制或散布本程序的部分或全部、以及其他
 * 任何侵害著作权人权益的行为，将承受严厉的民事和刑事处罚，对已知的违反者将给予法律范围内的全面制裁。
 *
 */

package com.example.demo.util;

import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Description:
 * 
 * @author xiongqingfeng
 * @version 1.0.0
 */
public class FileUtil {
	private final static Logger LOGGER = Logger.getLogger(FileUtil.class);
	public final static Map<String, String> FILE_TYPE_MAP = new HashMap<String, String>();
    
    static{    
        getAllFileType();  //初始化文件类型信息    
    }    
        
    private static void getAllFileType()    
    {    
        FILE_TYPE_MAP.put("jpg", "FFD8FF"); //JPEG (jpg)    
        FILE_TYPE_MAP.put("png", "89504E47");  //PNG (png)    
        FILE_TYPE_MAP.put("gif", "47494638");  //GIF (gif)    
        FILE_TYPE_MAP.put("tif", "49492A00");  //TIFF (tif)    
        FILE_TYPE_MAP.put("bmp", "424D"); //Windows Bitmap (bmp)    
    }    
    
	// 从给定位置读取Json文件
	public static String readJson(String path) {
		// 从给定位置获取文件
		File file = new File(path);
		BufferedReader reader = null;
		// 返回值,使用StringBuffer
		StringBuffer data = new StringBuffer();
		//
		try {
			reader = new BufferedReader(new FileReader(file));
			// 每次读取文件的缓存
			String temp = null;
			while ((temp = reader.readLine()) != null) {
				data.append(temp);
			}
		} catch (FileNotFoundException e) {
			LOGGER.error(e);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭文件流
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					LOGGER.error(e);
				}
			}
		}
		return data.toString();
	}

	// 给定路径与Json文件，存储到硬盘
	public static void writeJson(String path, Object json, String fileName) {
		BufferedWriter writer = null;
		File file = new File(path + fileName + ".json");
		// 如果文件不存在，则新建一个
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}
		// 写入
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(json.toString());
		} catch (IOException e) {
			LOGGER.error(e);
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}
		// LOGGER.info("文件写入成功！");
	}

	/**
	 * 判断是否为图片
	 * 
	 * @param file
	 * @return
	 */
	public static final boolean isImage(File file) {
		boolean flag = false;
		try {
			BufferedImage bufreader = ImageIO.read(file);
			int width = bufreader.getWidth();
			int height = bufreader.getHeight();
			if (width == 0 || height == 0) {
				flag = false;
			} else {
				flag = true;
			}
		} catch (IOException e) {
			flag = false;
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 获取图片的实际类型，如果不是图片就返回Null
	 * 
	 * @param file
	 * @return
	 */
	public final static String getImageFileType(File file)
			throws Exception {
		if (isImage(file)) {
			ImageInputStream iis = null;
			try {
				iis = ImageIO.createImageInputStream(file);
				Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
				if (!iter.hasNext()) {
					return null;
				}
				ImageReader reader = iter.next();
				iis.close();
				return reader.getFormatName();
			} catch (IOException e) {
				return "";
			} catch (Exception e) {
				return "";
			} finally {
				if (iis != null) {
					iis.close();
				}
			}
		}
		return "";
	}

	public final static String getFileHexString(byte[] b) {
		StringBuilder stringBuilder = new StringBuilder();
		if (b == null || b.length <= 0) {
			return null;
		}
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public final static String getFileTypeByStream(byte[] b) {
		try {
			String filetypeHex = String.valueOf(getFileHexString(b));
			Iterator<Entry<String, String>> entryiterator = FILE_TYPE_MAP
					.entrySet().iterator();
			while (entryiterator.hasNext()) {
				Entry<String, String> entry = entryiterator.next();
				String fileTypeHexValue = entry.getValue();
				if (filetypeHex.toUpperCase().startsWith(fileTypeHexValue)) {
					return entry.getKey();
				}
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}
	
		return "";
	}
	
	/**
	 * 校验图片的宽和高是否符合指定的宽度和高度
	 * @param posterImgheight  高度
	 * @param posterImgwidth   宽度
	 * @param b  图片文件
	 * @return
	 */
	public static boolean checkImgPixel(int posterImgheight,
			int posterImgwidth, byte[] b) throws Exception {
		Boolean flag = true;
		ByteArrayInputStream is = null;
		
		try {
			is = new ByteArrayInputStream(b);
			BufferedImage bufreader = ImageIO.read(new ByteArrayInputStream(b));
			int width = bufreader.getWidth();
			int height = bufreader.getHeight();
			if(width != posterImgwidth || height != posterImgheight){
				flag = false;
			}
		} catch (IOException e) {
			flag = false;
			LOGGER.error("!Error:判断图片宽高出错",e);
		} catch (Exception e) {
			flag = false;
			LOGGER.error("!Error:判断图片宽高出错",e);
		}finally{
			if(is != null){
				is.close();
			}
		}
		
		return flag;
	}

	/**
	 * 创建目录
	 * @param filePath
	 * @return
	 */
	public static boolean makeDir(String filePath){
		try {
			if(StringUtil.isNotBlank(filePath)){
				File dir = new File(filePath);
				if (dir.isDirectory()){
					return true;
				}else {
					dir.mkdirs();
					return true;
				}
			}
			LOGGER.error("创建目录===>>"+filePath);
		}catch (Exception e){
			LOGGER.error("创建目录失败！",e);
		}
		return false;
	}

	public static void main(String[] args) {
	}

	/**
	 * 文件删除  生成源目录文件
	 * @param file
	 */
	public static void deleteFile(File file)
	{
		if(file.isDirectory())
		{
			File[] files= file.listFiles();

			if(files==null||files.length==0) //如果是空文件夹
			{
				file.delete();
				return ;
			}

			for(File obj:files)
			{
				deleteFile(obj);
			}
		}
		file.delete();
	}

}
