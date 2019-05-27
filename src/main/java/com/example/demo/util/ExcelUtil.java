/*
 * Copyright (c) 2015, www.jd.com. All rights reserved.
 *
 * 警告：本计算机程序受著作权法和国际公约的保护，未经授权擅自复制或散布本程序的部分或全部、以及其他
 * 任何侵害著作权人权益的行为，将承受严厉的民事和刑事处罚，对已知的违反者将给予法律范围内的全面制裁。
 *
 */

package com.example.demo.util;

import com.google.common.collect.Lists;
import jxl.*;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * 导入导出Excel工具类
 *
 * @author xiongqingfeng
 * @version 1.0.0 , 2015年5月4日 8:33:09
 */
public class ExcelUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ExcelUtil.class);

    private static String tempPath = PropsUtil.getString(FrameworkConstant.commonsProps, "wj.temp.path", "d;/temp");

	private static final String FORMATTER = "yyyy-MM-dd";

	/**
	 * 导出Excel（可以导出到本地文件系统，也可以导出到浏览器，可自定义工作表大小）
	 *
	 * @param data
	 *            数据源
	 * @param fieldMap
	 *            类的英文属性和Excel中的中文列名的对应关系,如果需要的是引用对象的属性，则英文属性使用类似于EL表达式的格式,如：
	 *            list中存放的都是student
	 *            ，student中又有college属性，而我们需要学院名称，则可以这样写,fieldMap
	 *            .put("college.collegeName","学院名称")
	 * @param fileName
	 *            导出流
	 * @throws ExcelException
	 *             异常
	 */
	public static <T> void mapToExcel(Map<String, List<T>> data, LinkedHashMap<String, String> fieldMap, String fileName, HttpServletResponse response) throws ExcelException {

        // 如果文件名没提供，则使用时间戳
        if (fileName == null || fileName.trim().equals("")) {
            // 设置默认文件名为当前时间：年月日时分秒
            fileName = new SimpleDateFormat("yyyyMMddhhmmss")
                .format(new Date()).toString();
        } else {
            fileName += new SimpleDateFormat("yyyyMMddhhmmss")
                .format(new Date()).toString();
        }

        // 设置response头信息
        response.reset();
        response.setContentType("application/vnd.ms-excel"); // 改成输出excel文件

        // 创建工作簿并发送到浏览器
        OutputStream out = null;

        if (data == null || data.size() == 0) {
            throw new ExcelException("数据源中没有任何数据");
        }

        // 创建工作簿并发送到OutputStream指定的地方
        WritableWorkbook wwb = null;
        try {
            response.setHeader("Content-disposition", "attachment; filename="
                + URLEncoder.encode(fileName, "UTF-8") + ".xls");

            out = response.getOutputStream();
            wwb = Workbook.createWorkbook(out);

            // 因为2003的Excel一个工作表最多可以有65536条记录，除去列头剩下65535条
            // 所以如果记录太多，需要放到多个工作表中，其实就是个分页的过程
            // 1.计算一共有多少个工作表
            Set<Entry<String, List<T>>> entrySet = data.entrySet();
            Iterator<Entry<String, List<T>>> it = entrySet.iterator();
            while (it.hasNext()) {
                Entry<String, List<T>> entry = it.next();
                String sheetName = entry.getKey().toString();
                List<T> list = entry.getValue();
                // 2.创建相应的工作表，并向其中填充数据
                int count = 0;
                WritableSheet sheet = wwb.createSheet(sheetName, count++);
                fillSheet(sheet, list, fieldMap, 0, list.size() - 1, FORMATTER);
            }

        } catch (Exception e) {
            LOG.error("导出报错", e);
            // 如果是ExcelException，则直接抛出
            if (e instanceof ExcelException) {
                throw (ExcelException) e;

                // 否则将其它异常包装成ExcelException再抛出
            } else {
                throw new ExcelException("导出Excel失败");
            }
        } finally {
            try {
                if (null != wwb) {
                    wwb.write();
                    wwb.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WriteException e) {
                e.printStackTrace();
            }

        }

    }

	/***
	 *  采用默认格式处理日期格式
	 * @param list
	 * @param fieldMap
	 * @param sheetName
	 * @param sheetSize
	 * @param out
	 * @param <T>
	 * @throws ExcelException
	 */
	public static <T> void listToExcel(List<T> list,
                                       LinkedHashMap<String, String> fieldMap, String sheetName,
                                       int sheetSize, OutputStream out) throws ExcelException {
		listToExcelWithDate(list,fieldMap,sheetName,sheetSize,out,FORMATTER);
	}

    /**
     * 导出Excel（可以导出到本地文件系统，也可以导出到浏览器，可自定义工作表大小）
     *
     * @param list 数据源
     * @param fieldMap 类的英文属性和Excel中的中文列名的对应关系,如果需要的是引用对象的属性，则英文属性使用类似于EL表达式的格式,如： list中存放的都是student
     * ，student中又有college属性，而我们需要学院名称，则可以这样写,fieldMap .put("college.collegeName","学院名称")
     * @param sheetName 工作表的名称
     * @param sheetSize 每个工作表中记录的最大个数
     * @param out 导出流
     * @throws ExcelException 异常
     */
    public static <T> void listToExcelWithDate(List<T> list,
                                               LinkedHashMap<String, String> fieldMap, String sheetName,
                                               int sheetSize, OutputStream out, String formatter) throws ExcelException {

        if (list == null || list.size() == 0) {
            throw new ExcelException("数据源中没有任何数据");
        }

        if (sheetSize > 65535 || sheetSize < 1) {
            sheetSize = 65535;
        }

        // 创建工作簿并发送到OutputStream指定的地方
        WritableWorkbook wwb;
        try {
            wwb = Workbook.createWorkbook(out);

            // 因为2003的Excel一个工作表最多可以有65536条记录，除去列头剩下65535条
            // 所以如果记录太多，需要放到多个工作表中，其实就是个分页的过程
            // 1.计算一共有多少个工作表
            double sheetNum = Math.ceil(list.size()
                / Integer.valueOf(sheetSize).doubleValue());

            // 2.创建相应的工作表，并向其中填充数据
            for (int i = 0; i < sheetNum; i++) {
                // 如果只有一个工作表的情况
                if (1 == sheetNum) {

					WritableSheet sheet = wwb.createSheet(sheetName, i);
					fillSheet(sheet, list, fieldMap, 0, list.size() - 1, formatter);

                    // 有多个工作表的情况
                } else {
                    WritableSheet sheet = wwb.createSheet(sheetName + (i + 1),
                        i);

					// 获取开始索引和结束索引
					int firstIndex = i * sheetSize;
					int lastIndex = (i + 1) * sheetSize - 1 > list.size() - 1 ? list
							.size() - 1 : (i + 1) * sheetSize - 1;
							// 填充工作表
					fillSheet(sheet, list, fieldMap, firstIndex, lastIndex, formatter);
				}
			}

			wwb.write();
			wwb.close();

		} catch (Exception e) {
			LOG.error("导出报错",e);
			// 如果是ExcelException，则直接抛出
			if (e instanceof ExcelException) {
				throw (ExcelException) e;

				// 否则将其它异常包装成ExcelException再抛出
			} else {
				throw new ExcelException("导出Excel失败");
			}
		}

	}

	/**
	 * 导出Excel（可以导出到本地文件系统，也可以导出到浏览器，可自定义工作表大小）
	 *
	 * @param list
	 *            数据源
	 * @param fieldMap
	 *            类的英文属性和Excel中的中文列名的对应关系,如果需要的是引用对象的属性，则英文属性使用类似于EL表达式的格式,如：
	 *            list中存放的都是student
	 *            ，student中又有college属性，而我们需要学院名称，则可以这样写,fieldMap
	 *            .put("college.collegeName","学院名称")
	 * @param sheetName
	 *            工作表的名称
	 * @param sheetSize
	 *            每个工作表中记录的最大个数
	 * @param out
	 *            导出流
	 * @throws ExcelException
	 *             异常
	 */
	public static <T> void listToExcelWithDateFormatter(List<T> list,
                                                        LinkedHashMap<String, String> fieldMap, String sheetName,
                                                        int sheetSize, OutputStream out, String dateFormatter) throws ExcelException {

		if (StringUtil.isBlank(dateFormatter)) {
			dateFormatter = FORMATTER;
		}

		if (list == null || list.size() == 0) {
			throw new ExcelException("数据源中没有任何数据");
		}

		if (sheetSize > 65535 || sheetSize < 1) {
			sheetSize = 65535;
		}

		// 创建工作簿并发送到OutputStream指定的地方
		WritableWorkbook wwb;
		try {
			wwb = Workbook.createWorkbook(out);

			// 因为2003的Excel一个工作表最多可以有65536条记录，除去列头剩下65535条
			// 所以如果记录太多，需要放到多个工作表中，其实就是个分页的过程
			// 1.计算一共有多少个工作表
			double sheetNum = Math.ceil(list.size()
					/ Integer.valueOf(sheetSize).doubleValue());

			// 2.创建相应的工作表，并向其中填充数据
			for (int i = 0; i < sheetNum; i++) {
				// 如果只有一个工作表的情况
				if (1 == sheetNum) {

					WritableSheet sheet = wwb.createSheet(sheetName, i);
					fillSheet(sheet, list, fieldMap, 0, list.size() - 1, dateFormatter);

					// 有多个工作表的情况
				} else {
					WritableSheet sheet = wwb.createSheet(sheetName + (i + 1),
							i);

					// 获取开始索引和结束索引
					int firstIndex = i * sheetSize;
					int lastIndex = (i + 1) * sheetSize - 1 > list.size() - 1 ? list
							.size() - 1 : (i + 1) * sheetSize - 1;
					// 填充工作表
					fillSheet(sheet, list, fieldMap, firstIndex, lastIndex, dateFormatter);
				}
			}

			wwb.write();
			wwb.close();

		} catch (Exception e) {
			LOG.error("导出报错",e);
			// 如果是ExcelException，则直接抛出
			if (e instanceof ExcelException) {
				throw (ExcelException) e;

				// 否则将其它异常包装成ExcelException再抛出
			} else {
				throw new ExcelException("导出Excel失败");
			}
		}

	}


	/**
	 * List转为File文件
	 * @param list
	 * @param fieldMap
	 * @param sheetName
	 * @param sheetSize
	 * @param excelFile
	 * @param <T>
	 * @return
	 */
	public static <T> File listToExcel(List<T> list,
                                       LinkedHashMap<String, String> fieldMap, String sheetName,
                                       int sheetSize, File excelFile){
		File result = excelFile;
		if (excelFile==null) {
			return null;
		}
		if (list == null || list.size() == 0) {
			LOG.error("数据源中没有任何数据");
			result = excelFile;
		}

		if (sheetSize > 65535 || sheetSize < 1) {
			sheetSize = 65535;
		}

		// 创建工作簿并发送到OutputStream指定的地方
		WritableWorkbook wwb;
		try {
			wwb = Workbook.createWorkbook(excelFile);

			// 因为2003的Excel一个工作表最多可以有65536条记录，除去列头剩下65535条
			// 所以如果记录太多，需要放到多个工作表中，其实就是个分页的过程
			// 1.计算一共有多少个工作表
			double sheetNum = Math.ceil(list.size()
					/ Integer.valueOf(sheetSize).doubleValue());

			// 2.创建相应的工作表，并向其中填充数据
			for (int i = 0; i < sheetNum; i++) {
				// 如果只有一个工作表的情况
				if (1 == sheetNum) {

					WritableSheet sheet = wwb.createSheet(sheetName, i);
					fillSheet(sheet, list, fieldMap, 0, list.size() - 1, FORMATTER);

					// 有多个工作表的情况
				} else {
					WritableSheet sheet = wwb.createSheet(sheetName + (i + 1),
							i);

					// 获取开始索引和结束索引
					int firstIndex = i * sheetSize;
					int lastIndex = (i + 1) * sheetSize - 1 > list.size() - 1 ? list
							.size() - 1 : (i + 1) * sheetSize - 1;
					// 填充工作表
					fillSheet(sheet, list, fieldMap, firstIndex, lastIndex, FORMATTER);
				}
			}

			wwb.write();
			wwb.close();

		} catch (Exception e) {
			LOG.error("导出报错",e);
			// 如果是ExcelException，则直接抛出
		}
		return result;

	}

	/**
	 * 导出excel
	 *
	 * @param list
	 *            数据源
	 * @param fieldMap
	 *            类的英文属性和Excel中的中文列名的对应关系
	 * @param collectionName
	 *            子集合的名称
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 * @param sheetName
	 *            工作表的名称
	 * @param sheetSize
	 *            每个工作表中记录的最大个数
	 * @param out
	 *            导出流
	 * @throws ExcelException
	 *             异常
	 */
	public static <T> void listToExcel(List<T> list,
                                       LinkedHashMap<String, String> fieldMap, String collectionName,
                                       String title, String content, String sheetName, int sheetSize,
                                       OutputStream out) throws ExcelException {

		if (list == null || list.size() == 0) {
			throw new ExcelException("数据源中没有任何数据");
		}

		if (sheetSize > 65535 || sheetSize < 1) {
			sheetSize = 65535;
		}

		// 创建工作簿并发送到OutputStream指定的地方
		WritableWorkbook wwb;
		try {
			wwb = Workbook.createWorkbook(out);

			// 因为2003的Excel一个工作表最多可以有65536条记录，除去列头剩下65535条
			// 所以如果记录太多，需要放到多个工作表中，其实就是个分页的过程
			// 1.计算一共有多少个工作表
			double sheetNum = Math.ceil(list.size()
					/ Integer.valueOf(sheetSize).doubleValue());

			// 2.创建相应的工作表，并向其中填充数据
			for (int i = 0; i < sheetNum; i++) {
				// 如果只有一个工作表的情况
				if (1 == sheetNum) {
					WritableSheet sheet = wwb.createSheet(sheetName, i);
					fillSheet(sheet, list, fieldMap, collectionName, title,
							content, 0, list.size() - 1, FORMATTER);

					// 有多个工作表的情况
				} else {
					WritableSheet sheet = wwb.createSheet(sheetName + (i + 1),
							i);

					// 获取开始索引和结束索引
					int firstIndex = i * sheetSize;
					int lastIndex = (i + 1) * sheetSize - 1 > list.size() - 1 ? list
							.size() - 1 : (i + 1) * sheetSize - 1;
					// 填充工作表
					fillSheet(sheet, list, fieldMap, collectionName, title,
							content, firstIndex, lastIndex, FORMATTER);
				}
			}

			wwb.write();
			wwb.close();

		} catch (Exception e) {
			LOG.error("导出报错",e);
			// 如果是ExcelException，则直接抛出
			if (e instanceof ExcelException) {
				throw (ExcelException) e;

				// 否则将其它异常包装成ExcelException再抛出
			} else {
				throw new ExcelException("导出Excel失败");
			}
		}

	}

	/**
	 * 导出excel
	 *
	 * @param list
	 *            数据源
	 * @param fieldMap
	 *            类的英文属性和Excel中的中文列名的对应关系
	 * @param sheetName
	 *            工作表的名称
	 * @param out
	 *            导出流
	 * @throws ExcelException
	 *             异常
	 */
	public static <T> void listToExcel(List<T> list,
                                       LinkedHashMap<String, String> fieldMap, String sheetName,
                                       OutputStream out) throws ExcelException {

		listToExcel(list, fieldMap, sheetName, 65535, out);

	}

	/**
	 * 导出Excel 指定文件名编码格式为utf8
	 * @param list
	 * @param fieldMap
	 * @param sheetName
	 * @param response
	 * @param fileName
	 * @param <T>
	 * @throws ExcelException
	 */
	public static <T> void listToExcelUTF8(List<T> list,
                                           LinkedHashMap<String, String> fieldMap, String sheetName, HttpServletResponse response, String fileName)
			throws ExcelException {
		int sheetSize = 65535;
		String timstamp = new SimpleDateFormat("yyyyMMddhhmmss")
				.format(new Date()).toString();
		// 如果文件名没提供，则使用时间戳
		if (fileName == null || fileName.trim().equals("")) {
			// 设置默认文件名为当前时间：年月日时分秒
			fileName = timstamp;
		} else {
			fileName += timstamp;
		}

		// 设置response头信息
		response.reset();
		response.setContentType("application/vnd.ms-excel"); // 改成输出excel文件
		// 创建工作簿并发送到浏览器
		OutputStream out = null;
		try {
			response.setHeader("Content-disposition", "attachment; filename="
					+ URLEncoder.encode(fileName, "UTF-8") + ".xls");
			out = response.getOutputStream();
			listToExcel(list, fieldMap, sheetName, sheetSize, out);

		} catch (Exception e) {
			LOG.error("导出报错",e);

			// 如果是ExcelException，则直接抛出
			if (e instanceof ExcelException) {
				throw (ExcelException) e;

				// 否则将其它异常包装成ExcelException再抛出
			} else {
				throw new ExcelException("导出Excel失败");
			}
		}finally{
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
					LOG.error("导出报错",e);
				}
			}
		}

	}

	public static <T> void listToExcel(List<T> list,
                                       LinkedHashMap<String, String> fieldMap, String sheetName,
                                       int sheetSize, HttpServletResponse response, String fileName)
			throws ExcelException {
		listToExcelWithDate(list,fieldMap,sheetName,sheetSize,response,fileName,FORMATTER);
	}
	/**
	 * 导出Excel（导出到浏览器，可以自定义工作表的大小）
	 *
	 * @param list
	 *            数据源
	 * @param fieldMap
	 *            类的英文属性和Excel中的中文列名的对应关系
	 * @param sheetName
	 *            工作表的名称
	 * @param sheetSize
	 *            每个工作表中记录的最大个数
	 * @param response
	 *            使用response可以导出到浏览器
	 * @param fileName
	 *            Excel的文件名
	 * @throws ExcelException
	 *             异常
	 */
	public static <T> void listToExcelWithDate(List<T> list,
                                               LinkedHashMap<String, String> fieldMap, String sheetName,
                                               int sheetSize, HttpServletResponse response, String fileName, String formatter)
			throws ExcelException {

		// 如果文件名没提供，则使用时间戳
		if (fileName == null || fileName.trim().equals("")) {
			// 设置默认文件名为当前时间：年月日时分秒
			fileName = new SimpleDateFormat("yyyyMMddhhmmss")
					.format(new Date()).toString();
		}

		// 设置response头信息
		response.reset();
		response.setContentType("application/vnd.ms-excel"); // 改成输出excel文件
		response.setHeader("Content-disposition", "attachment; filename="
				+ fileName + ".xls");

		// 创建工作簿并发送到浏览器
		OutputStream out = null;
		try {
			out = response.getOutputStream();
			listToExcelWithDate(list, fieldMap, sheetName, sheetSize, out,formatter);

		} catch (Exception e) {
            LOG.error("导出报错",e);

			// 如果是ExcelException，则直接抛出
			if (e instanceof ExcelException) {
				throw (ExcelException) e;

				// 否则将其它异常包装成ExcelException再抛出
			} else {
				throw new ExcelException("导出Excel失败");
			}
		}finally{
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
					LOG.error("导出报错",e);
				}
			}
		}
	}

	/**
	 * 导出Excel（导出到浏览器，可以自定义工作表的大小）
	 *
	 * @param list
	 *            数据源
	 * @param fieldMap
	 *            类的英文属性和Excel中的中文列名的对应关系
	 * @param collectionName
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 * @param sheetName
	 *            工作表的名称
	 * @param sheetSize
	 *            每个工作表中记录的最大个数
	 * @param response
	 *            使用response可以导出到浏览器
	 * @param fileName
	 *            Excel的文件名
	 * @throws ExcelException
	 *             异常
	 */
	public static <T> void listToExcel(List<T> list,
                                       LinkedHashMap<String, String> fieldMap, String collectionName,
                                       String title, String content, String sheetName, int sheetSize,
                                       HttpServletResponse response, String fileName)
			throws ExcelException {

		// 如果文件名没提供，则使用时间戳
		if (fileName == null || fileName.trim().equals("")) {
			// 设置默认文件名为当前时间：年月日时分秒
			fileName = new SimpleDateFormat("yyyyMMddhhmmss")
					.format(new Date()).toString();
		}

		// 设置response头信息
		response.reset();
		response.setContentType("application/vnd.ms-excel"); // 改成输出excel文件
		response.setHeader("Content-disposition", "attachment; filename="
				+ fileName + ".xls");

		// 创建工作簿并发送到浏览器
		try {

			OutputStream out = response.getOutputStream();
			listToExcel(list, fieldMap, collectionName, title, content,
					sheetName, sheetSize, out);

		} catch (Exception e) {
            LOG.error("导出报错",e);

			// 如果是ExcelException，则直接抛出
			if (e instanceof ExcelException) {
				throw (ExcelException) e;

				// 否则将其它异常包装成ExcelException再抛出
			} else {
				throw new ExcelException("导出Excel失败");
			}
		}
	}

	/**
	 * 导出Excel（导出到浏览器，可以自定义工作表的大小）
	 *
	 * @param list
	 *            数据源
	 * @param fieldMap
	 *            类的英文属性和Excel中的中文列名的对应关系
	 * @param sheetName
	 *            工作表的名称
	 * @param sheetSize
	 *            每个工作表中记录的最大个数
	 * @param response
	 *            使用response可以导出到浏览器
	 * @throws ExcelException
	 *             异常
	 */
	public static <T> void listToExcel(List<T> list,
                                       LinkedHashMap<String, String> fieldMap, String sheetName,
                                       int sheetSize, HttpServletResponse response) throws ExcelException {

		// 设置默认文件名为当前时间：年月日时分秒
		String fileName = new SimpleDateFormat("yyyyMMddhhmmss").format(
				new Date()).toString();

		listToExcel(list, fieldMap, sheetName, sheetSize, response, fileName);
	}


	/**
	 * 导出excel文件
	 * @param list
	 * @param fieldMap
	 * @param sheetName
	 * @param sheetSize
	 * @param <T>
	 * @return
	 * @throws ExcelException
	 */
	public static <T> String listToExcel(List<T> list,
                                         LinkedHashMap<String, String> fieldMap, String sheetName,
                                         int sheetSize) {
		// 设置默认文件名为当前时间：年月日时分秒
		String fileName = UUID.randomUUID()  + ".xls";

		if (!FileUtil.makeDir(tempPath)) {
			return null;
		}
		File excelFile = listToExcel(list, fieldMap, sheetName, sheetSize, new File(tempPath+"/"+fileName));
		if (excelFile!=null){
			return fileName;
		}
		return null;
	}

	/**
	 * 导出Excel（导出到浏览器，可以自定义工作表的大小）
	 *
	 * @param list
	 *            数据源
	 * @param fieldMap
	 *            类的英文属性和Excel中的中文列名的对应关系
	 * @param sheetName
	 *            工作表的名称
	 * @param sheetSize
	 *            每个工作表中记录的最大个数
	 * @param response
	 *            使用response可以导出到浏览器
	 * @throws ExcelException
	 *             异常
	 */
	public static <T> void listToExcel(List<T> list,
                                       LinkedHashMap<String, String> fieldMap, String sheetName,
                                       int sheetSize, String fileName, HttpServletResponse response) throws ExcelException {

		if(StringUtil.isEmpty(fileName)){
			// 设置默认文件名为当前时间：年月日时分秒
			fileName = new SimpleDateFormat("yyyyMMddhhmmss").format(
					new Date()).toString();
		}

		listToExcel(list, fieldMap, sheetName, sheetSize, response, fileName);
	}

	/**
	 * 导出Excel（导出到浏览器，可以自定义工作表的大小）
	 *
	 * @param list
	 *            数据源
	 * @param fieldMap
	 *            类的英文属性和Excel中的中文列名的对应关系
	 * @param collectionName
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 * @param sheetName
	 *            工作表的名称
	 * @param response
	 *            使用response可以导出到浏览器
	 * @throws ExcelException
	 *             异常
	 */
	public static <T> void listToExcel(List<T> list,
                                       LinkedHashMap<String, String> fieldMap, String collectionName,
                                       String title, String content, String sheetName,
                                       HttpServletResponse response) throws ExcelException {

		listToExcel(list, fieldMap, collectionName, title, content, sheetName,
				65535, response, "");
	}

	/**
	 * 导出Excel（导出到浏览器，可以自定义工作表的大小）
	 *
	 * @param list
	 *            数据源
	 * @param fieldMap
	 *            类的英文属性和Excel中的中文列名的对应关系
	 * @param sheetName
	 *            工作表的名称
	 * @param response
	 *            使用response可以导出到浏览器
	 * @throws ExcelException
	 *             异常
	 */
	public static <T> void listToExcel(List<T> list,
                                       LinkedHashMap<String, String> fieldMap, String sheetName,
                                       HttpServletResponse response) throws ExcelException {

		listToExcel(list, fieldMap, sheetName, 65535, response);
	}

	/**
	 * 导出excel
	 * @param list
	 * @param fieldMap
	 * @param sheetName
	 * @param <T>
	 * @return
	 * @throws ExcelException
	 */
	public static <T> String listToExcel(List<T> list,
                                         LinkedHashMap<String, String> fieldMap, String sheetName) {
		return listToExcel(list, fieldMap, sheetName, 65535);
	}
	/**
	 * 导出Excel（导出到浏览器，可以自定义工作表的大小）
	 *
	 * @param list
	 *            数据源
	 * @param fieldMap
	 *            类的英文属性和Excel中的中文列名的对应关系
	 * @param sheetName
	 *            工作表的名称
	 * @param fileName
	 *            表名
	 * @param response
	 *            使用response可以导出到浏览器
	 * @throws ExcelException
	 *             异常
	 */
	public static <T> void listToExcel(List<T> list,
                                       LinkedHashMap<String, String> fieldMap, String sheetName, String fileName,
                                       HttpServletResponse response) throws ExcelException {

		listToExcel(list, fieldMap, sheetName, 65535, fileName,response);
	}

	/**
	 * 导出Excel（导出到浏览器，可以自定义工作表的大小）
	 *
	 * @param list
	 *            数据源
	 * @param fieldMap
	 *            类的英文属性和Excel中的中文列名的对应关系
	 * @param sheetName
	 *            工作表的名称
	 * @param response
	 *            使用response可以导出到浏览器
	 * @throws ExcelException
	 *             异常
	 */
	public static <T> void listToExcel(List<T> list,
                                       LinkedHashMap<String, String> fieldMap, String sheetName,
                                       HttpServletResponse response, String fileName) throws ExcelException {
		// 设置默认文件名为当前时间：年月日时分秒
		fileName = fileName + new SimpleDateFormat("yyyyMMddhhmmss").format(
						new Date()).toString();
		listToExcel(list, fieldMap, sheetName, 65535, response, fileName);
	}

	/**
	 * 导出Excel模板导出到浏览器
	 *
	 * @param fieldMap 类的英文属性和Excel中的中文列名的对应关系
	 * @param sheetName 工作表的名称
	 * @param response 使用response可以导出到浏览器
	 * @throws ExcelException 异常
	 */
	public static <T> void exportExcelTemplate(LinkedHashMap<String, String> fieldMap, String sheetName,
                                               HttpServletResponse response) throws ExcelException {

		// 设置默认文件名为当前时间：年月日时分秒
		String fileName = new SimpleDateFormat("yyyyMMddhhmmss").format(
				new Date()).toString();
		exportExcelTemplate(fieldMap,sheetName,response,fileName);
	}

	/**
	 * 导出Excel模板 自定义模板名称
	 * @param fieldMap
	 * @param sheetName
	 * @param response
	 * @param fileName
	 * @param <T>
	 * @throws ExcelException
	 */
	public static <T> void exportExcelTemplate(LinkedHashMap<String, String> fieldMap, String sheetName,
                                               HttpServletResponse response, String fileName)throws ExcelException {
		// 设置response头信息
		response.reset();
		response.setContentType("application/vnd.ms-excel"); // 改成输出excel文件
		// 创建工作簿并发送到浏览器
		try {
			response.setHeader("Content-Disposition",
					"attachment;filename*=UTF-8''" + URLEncoder.encode(fileName, "UTF-8") + ".xls");
			OutputStream out = response.getOutputStream();
			// 创建工作簿并发送到OutputStream指定的地方
			WritableWorkbook wwb;
			try {
				wwb = Workbook.createWorkbook(out);
				double sheetNum = 1;

				// 2.创建相应的工作表，并向其中填充数据
				// 如果只有一个工作表的情况
				if (1 == sheetNum) {
					WritableSheet sheet = wwb.createSheet(sheetName, 1);

					// 定义存放英文字段名和中文字段名的数组
					String[] enFields = new String[fieldMap.size()];
					String[] cnFields = new String[fieldMap.size()];

					// 填充数组
					int count = 0;
					for (Entry<String, String> entry : fieldMap.entrySet()) {
						enFields[count] = entry.getKey();
						cnFields[count] = entry.getValue();
						count++;
					}
					// 填充表头
					for (int i = 0; i < cnFields.length; i++) {
						Label label = new Label(i, 0, cnFields[i]);
						sheet.addCell(label);
					}
					// 设置自动列宽
					setColumnAutoSize(sheet, 5);
				}
				wwb.write();
				wwb.close();
			} catch (Exception e) {
				LOG.error("生成模板报错", e);
				// 如果是ExcelException，则直接抛出
				if (e instanceof ExcelException) {
					throw (ExcelException) e;
				} else {
					throw new ExcelException("导出Excel失败");
				}
			}

		} catch (Exception e) {
			LOG.error("生成模板报错", e);
			// 如果是ExcelException，则直接抛出
			if (e instanceof ExcelException) {
				throw (ExcelException) e;
			} else {
				throw new ExcelException("导出Excel失败");
			}
		}
	}
	
	/**
	 * 将Excel转化为List
	 *
	 * @param in
	 *            ：承载着Excel的输入流
	 * @param entityClass
	 *            ：List中对象的类型（Excel中的每一行都要转化为该类型的对象）
	 * @param fieldMap
	 *            ：Excel中的中文列头和类的英文属性的对应关系Map
	 * @param uniqueFields
	 *            ：指定业务主键组合（即复合主键），这些列的组合不能重复
	 * @return list集合
	 * @throws ExcelException
	 *             异常
	 */
	public static <T> List<T> excelToList(InputStream in, String sheetName,
                                          Class<T> entityClass, LinkedHashMap<String, String> fieldMap,
                                          String[] uniqueFields) throws ExcelException {

		// 定义要返回的list
		List<T> resultList = new ArrayList<T>();

		try {

			// 根据Excel数据源创建WorkBook
			Workbook wb = Workbook.getWorkbook(in);
			// 获取工作表
			Sheet sheet = wb.getSheet(sheetName);

			// 获取工作表的有效行数
			int realRows = 0;
			for (int i = 0; i < sheet.getRows(); i++) {

				int nullCols = 0;
				for (int j = 0; j < sheet.getColumns(); j++) {
					Cell currentCell = sheet.getCell(j, i);
					if (currentCell == null
							|| "".equals(currentCell.getContents().toString())) {
						nullCols++;
					}
				}

				if (nullCols == sheet.getColumns()) {
					break;
				} else {
					realRows++;
				}
			}

			// 如果Excel中没有数据则提示错误
			if (realRows <= 1) {
				throw new ExcelException("Excel文件中没有任何数据");
			}

			Cell[] firstRow = sheet.getRow(0);

			String[] excelFieldNames = new String[firstRow.length];

			// 获取Excel中的列名
			for (int i = 0; i < firstRow.length; i++) {
				excelFieldNames[i] = firstRow[i].getContents().toString()
						.trim();
			}

			// 判断需要的字段在Excel中是否都存在
			boolean isExist = true;
			List<String> excelFieldList = Arrays.asList(excelFieldNames);
			for (String cnName : fieldMap.keySet()) {
				if (!excelFieldList.contains(cnName)) {
					isExist = false;
					break;
				}
			}

			// 如果有列名不存在，则抛出异常，提示错误
			if (!isExist) {
				throw new ExcelException("Excel中缺少必要的字段，或字段名称有误");
			}

			// 将列名和列号放入Map中,这样通过列名就可以拿到列号
			LinkedHashMap<String, Integer> colMap = new LinkedHashMap<String, Integer>();
			for (int i = 0; i < excelFieldNames.length; i++) {
				colMap.put(excelFieldNames[i], firstRow[i].getColumn());
			}

			// 判断是否有重复行
			// 1.获取uniqueFields指定的列
			Cell[][] uniqueCells = new Cell[uniqueFields.length][];
			for (int i = 0; i < uniqueFields.length; i++) {
				int col = colMap.get(uniqueFields[i]);
				uniqueCells[i] = sheet.getColumn(col);
			}

			// 2.从指定列中寻找重复行
			for (int i = 1; i < realRows; i++) {
				int nullCols = 0;
				for (int j = 0; j < uniqueFields.length; j++) {
					String currentContent = uniqueCells[j][i].getContents();
					Cell sameCell = sheet.findCell(currentContent,
							uniqueCells[j][i].getColumn(),
							uniqueCells[j][i].getRow() + 1,
							uniqueCells[j][i].getColumn(),
							uniqueCells[j][realRows - 1].getRow(), true);
					if (sameCell != null) {
						nullCols++;
					}
				}

				if (nullCols == uniqueFields.length) {
					throw new ExcelException("Excel中有重复行，请检查");
				}
			}

			// 将sheet转换为list
			for (int i = 1; i < realRows; i++) {
				// 新建要转换的对象
				T entity = entityClass.newInstance();

				// 给对象中的字段赋值
				for (Entry<String, String> entry : fieldMap.entrySet()) {
					// 获取中文字段名
					String cnNormalName = entry.getKey();
					// 获取英文字段名
					String enNormalName = entry.getValue();
					// 根据中文字段名获取列号
					int col = colMap.get(cnNormalName);

					// 获取当前单元格中的内容
					String content = sheet.getCell(col, i).getContents()
							.toString().trim();

					// 给对象赋值
					setFieldValueByName(enNormalName, content, entity);
				}

				resultList.add(entity);
			}
		} catch (Exception e) {
            LOG.error("导入报错",e);
			// 如果是ExcelException，则直接抛出
			if (e instanceof ExcelException) {
				throw (ExcelException) e;

				// 否则将其它异常包装成ExcelException再抛出
			} else {
                LOG.error("导入报错",e);
				throw new ExcelException("导入Excel失败");
			}
		}
		return resultList;
	}

	/**
	 * 根据字段名获取字段值
	 *
	 * @param fieldName
	 *            字段名
	 * @param o
	 *            对象
	 * @return 字段值
	 * @throws Exception
	 *             异常
	 */
	public static Object getFieldValueByName(String fieldName, Object o, String dateFormatter)
			throws Exception {
		Object value = null;
		//增加判断是否为map
		if(o instanceof Map){
			value = ((Map) o).get(fieldName);
			if(value == null){
				value = "";
			}
		}else {
			Field field = getFieldByName(fieldName, o.getClass());
			if (field != null) {
				field.setAccessible(true);
				if(field.getType()==Date.class&&field.get(o)!=null)
				{
					try{
						SimpleDateFormat sdf=new SimpleDateFormat(dateFormatter);
						value=sdf.format(field.get(o));
					}catch(Exception ex)
					{

					}
				}else{
					value = field.get(o);
				}

			} else {
				throw new ExcelException(o.getClass().getSimpleName() + "类不存在字段名 "
						+ fieldName);
			}
		}
		

		

		return value;
	}

	/**
	 * 根据字段名获取字段
	 *
	 * @param fieldName
	 *            字段名
	 * @param clazz
	 *            包含该字段的类
	 * @return 字段
	 */
	public static Field getFieldByName(String fieldName, Class<?> clazz) {
		// 拿到本类的所有字段
		Field[] selfFields = clazz.getDeclaredFields();

		// 如果本类中存在该字段，则返回
		for (Field field : selfFields) {
			if (field.getName().equals(fieldName)) {
				return field;
			}
		}

		// 否则，查看父类中是否存在此字段，如果有则返回
		Class<?> superClazz = clazz.getSuperclass();
		if (superClazz != null && superClazz != Object.class) {
			return getFieldByName(fieldName, superClazz);
		}

		// 如果本类和父类都没有，则返回空
		return null;
	}
	
	
	/**
	 * 根据实体拿到该实体的所有属性
	 *
	 * @param clazz 实体
	 * @return 返回属性的list集合
	 */
	public static List getFieldByClass(Class<?> clazz) {
		
		List list = new ArrayList();
		
		// 拿到本类的所有字段
		Field[] selfFields = clazz.getDeclaredFields();

		for (Field field : selfFields) {
			list.add(field.getName());
		}
		// 否则，查看父类中是否存在此字段，如果有则返回
		Class<?> superClazz = clazz.getSuperclass();
		
		Field[] superFields=superClazz.getDeclaredFields();
		for (Field field : superFields) {
			list.add(field.getName());
		}
		

		// 如果本类和父类都没有，则返回空
		return list;
	}
		
	
	/**
	 * 根据实体拿到该实体的所有属性
	 *
	 * @param clazz 实体
	 * @return 返回属性的list集合
	 */
	public static List getSuperClassFieldByClass(Class<?> clazz) {
		
		List list = new ArrayList();
		
		// 否则，查看父类中是否存在此字段，如果有则返回
		Class<?> superClazz = clazz.getSuperclass();
		
		Field[] superFields=superClazz.getDeclaredFields();
		for (Field field : superFields) {
			list.add(field.getName());
		}
		

		// 如果父类没有，则返回空
		return list;
	}	

	/**
	 * 根据带路径或不带路径的属性名获取属性值,即接受简单属性名，如userName等，又接受带路径的属性名，如student.department.
	 * name等
	 *
	 * @param fieldNameSequence
	 *            带路径的属性名或简单属性名
	 * @param o
	 *            对象
	 * @return 属性值
	 * @throws Exception
	 *             异常
	 */
	public static Object getFieldValueByNameSequence(String fieldNameSequence,
                                                     Object o, String dateFormatter) throws Exception {

		Object value = null;

		// 将fieldNameSequence进行拆分
		String[] attributes = fieldNameSequence.split("\\.");
		if (attributes.length == 1) {
			value = getFieldValueByName(fieldNameSequence, o, dateFormatter);
		} else {
			// 根据属性名获取属性对象
			Object fieldObj = getFieldValueByName(attributes[0], o, dateFormatter);
			String subFieldNameSequence = fieldNameSequence
					.substring(fieldNameSequence.indexOf(".") + 1);
			value = getFieldValueByNameSequence(subFieldNameSequence, fieldObj, dateFormatter);
		}
		return value;

	}

	/**
	 * 根据字段名给对象的字段赋值
	 *
	 * @param fieldName
	 *            字段名
	 * @param fieldValue
	 *            字段值
	 * @param o
	 *            对象
	 * @throws Exception
	 *             异常
	 */
	public static void setFieldValueByName(String fieldName,
                                           Object fieldValue, Object o) throws Exception {

		Field field = getFieldByName(fieldName, o.getClass());
		if (field != null) {
			field.setAccessible(true);
			// 获取字段类型o
			Class<?> fieldType = field.getType();

			// 根据字段类型给字段赋值
			if (String.class == fieldType) {
				field.set(o, String.valueOf(fieldValue));
			} else if ((Integer.TYPE == fieldType)
					|| (Integer.class == fieldType)) {
				if(StringUtil.isNotBlank(fieldValue.toString()))
				field.set(o, Integer.parseInt(fieldValue.toString()));
			} else if ((Long.TYPE == fieldType) || (Long.class == fieldType)) {
				if(StringUtil.isNotBlank(fieldValue.toString()))
				field.set(o, Long.valueOf(fieldValue.toString()));
			} else if ((Float.TYPE == fieldType) || (Float.class == fieldType)) {
				field.set(o, Float.valueOf(fieldValue.toString()));
			} else if ((Short.TYPE == fieldType) || (Short.class == fieldType)) {
				field.set(o, Short.valueOf(fieldValue.toString()));
			}
            else if ((Double.TYPE == fieldType)
					|| (Double.class == fieldType)) {
				field.set(o, Double.valueOf(fieldValue.toString()));
			} else if (Character.TYPE == fieldType) {
				if ((fieldValue != null)
						&& (fieldValue.toString().length() > 0)) {
					field.set(o,
							Character.valueOf(fieldValue.toString().charAt(0)));
				}
			} else if (Date.class == fieldType) {
				if(fieldValue!=null&&StringUtil.isNotBlank(fieldValue.toString()))
				{
					String dateValue=fieldValue.toString();
					if(dateValue.contains("."))
					{
						dateValue=dateValue.replace(".","-");
					}
					SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
					Date date=sdf.parse(dateValue);

					field.set(o, date);
				}
			}else if (BigDecimal.class == fieldType) {
				if(StringUtil.isNotBlank(fieldValue.toString()))
                field.set(o, new BigDecimal(fieldValue.toString()));
            }
            else {
				field.set(o, fieldValue);
			}
		} else {
			throw new ExcelException(o.getClass().getSimpleName() + "类不存在字段名 "
					+ fieldName);
		}
	}

	/**
	 * 设置工作表自动列宽和首行加粗
	 *
	 * @param ws
	 *            要设置格式的工作表
	 * @param extraWith
	 *            额外的宽度
	 */
	public static void setColumnAutoSize(WritableSheet ws, int extraWith) {
		// 获取本列的最宽单元格的宽度
		for (int i = 0; i < ws.getColumns(); i++) {
			int colWith = 0;
			for (int j = 0; j < ws.getRows(); j++) {
				String content = ws.getCell(i, j).getContents().toString();
				int cellWith = content.length();
				if (colWith < cellWith) {
					colWith = cellWith;
				}
			}
			// 设置单元格的宽度为最宽宽度+额外宽度
			ws.setColumnView(i, colWith + extraWith);
		}

	}

	/**
	 * 向工作表中填充数据
	 *
	 * @param sheet
	 *            工作表名称
	 * @param list
	 *            数据源
	 * @param fieldMap
	 *            中英文字段对应关系的Map
	 * @param firstIndex
	 *            开始索引
	 * @param lastIndex
	 *            结束索引
	 * @throws Exception
	 *             异常
	 */
	public static <T> void fillSheet(WritableSheet sheet, List<T> list,
                                     LinkedHashMap<String, String> fieldMap, int firstIndex,
                                     int lastIndex, String dateFormatter) throws Exception {

		// 定义存放英文字段名和中文字段名的数组
		String[] enFields = new String[fieldMap.size()];
		String[] cnFields = new String[fieldMap.size()];

		// 填充数组
		int count = 0;
		for (Entry<String, String> entry : fieldMap.entrySet()) {
			enFields[count] = entry.getKey();
			cnFields[count] = entry.getValue();
			count++;
		}
		// 填充表头
		for (int i = 0; i < cnFields.length; i++) {
			Label label = new Label(i, 0, cnFields[i]);
			sheet.addCell(label);
		}

		// 填充内容
		int rowNo = 1;
		for (int index = firstIndex; index <= lastIndex; index++) {
			// 获取单个对象
			T item = list.get(index);
			for (int i = 0; i < enFields.length; i++) {
				Object objValue = getFieldValueByNameSequence(enFields[i], item, dateFormatter);
				String fieldValue = objValue == null ? "" : objValue.toString();
				Label label = new Label(i, rowNo, fieldValue);
				sheet.addCell(label);
			}

			rowNo++;
		}

		// 设置自动列宽
		setColumnAutoSize(sheet, 5);
	}

	/**
	 * 向工作表中填充数据
	 *
	 * @param sheet
	 * @param list
	 *            数据源
	 * @param normalFieldMap
	 *            普通中英文字段对应关系的Map
	 * @param title
	 *            标题
	 * @param content
	 *            内容
	 * @param firstIndex
	 *            开始索引
	 * @param lastIndex
	 *            结束索引
	 * @throws Exception
	 */
	public static <T> void fillSheet(WritableSheet sheet, List<T> list,
                                     LinkedHashMap<String, String> normalFieldMap,
                                     String collectionFieldName, String title, String content,
                                     int firstIndex, int lastIndex, String dateFormatter) throws Exception {

		// 定义存放普通英文字段名和中文字段名的数组
		String[] enFields = new String[normalFieldMap.size()];
		String[] cnFields = new String[normalFieldMap.size()];

		// 填充普通字段数组
		int count = 0;
		for (Entry<String, String> entry : normalFieldMap.entrySet()) {
			enFields[count] = entry.getKey();
			cnFields[count] = entry.getValue();
			count++;
		}

		// 填充表头（普通字段）
		for (int i = 0; i < cnFields.length; i++) {
			Label label = new Label(i, 0, cnFields[i]);
			sheet.addCell(label);
		}

		// 填充表头（行转列字段）
		T firstItem = list.get(0);
		List childList = (List) getFieldValueByName(collectionFieldName,
				firstItem, dateFormatter);

		int colCount = cnFields.length;
		for (Object obj : childList) {
			Object objValue = getFieldValueByNameSequence(title, obj, dateFormatter);
			String fieldValue = objValue == null ? "" : objValue.toString();
			Label label = new Label(colCount, 0, fieldValue);
			sheet.addCell(label);
			colCount++;
		}

		// 填充内容
		int rowNo = 1;
		for (int index = firstIndex; index <= lastIndex; index++) {
			// 获取单个对象
			T item = list.get(index);
			// 填充普通字段內容
			for (int i = 0; i < enFields.length; i++) {
				Object objValue = getFieldValueByNameSequence(enFields[i], item, dateFormatter);
				String fieldValue = objValue == null ? "" : objValue.toString();
				Label label = new Label(i, rowNo, fieldValue);
				sheet.addCell(label);
			}

			// 填充集合字段內容
			if (collectionFieldName != null && !collectionFieldName.equals("")) {
				// 拿到集合对象
				List currentList = (List) getFieldValueByName(
						collectionFieldName, item, dateFormatter);
				// 将集合对象行转列
				for (int i = 0; i < currentList.size(); i++) {
					Object objValue = getFieldValueByNameSequence(content,
							currentList.get(i), dateFormatter);
					String fieldValue = objValue == null ? "" : objValue
							.toString();
					Label label = new Label(i + cnFields.length, rowNo,
							fieldValue);
					sheet.addCell(label);
				}
			}

			rowNo++;
		}

		// 设置自动列宽
		setColumnAutoSize(sheet, 5);
	}
	
	public static <T> List<T> excelToList(InputStream in, String sheetName, Class<T> entityClass, LinkedHashMap<String, String> fieldMap) throws ExcelException {
		// 定义要返回的list
		List<T> resultList = new ArrayList<T>();

		try {

            // 根据Excel数据源创建WorkBook
            Workbook wb = Workbook.getWorkbook(in);
            // 获取工作表
            Sheet sheet = wb.getSheet(sheetName);
            if (sheet == null) {
                throw new ExcelException("Excel文件中sheet信息不匹配");
            }
            // 如果Excel中没有数据则提示错误
            if (sheet.getRows() <= 1) {
                throw new ExcelException("Excel文件中没有任何数据");
            }

			Cell[] firstRow = sheet.getRow(0);

			String[] excelFieldNames = new String[firstRow.length];

			// 获取Excel中的列名
			for (int i = 0; i < firstRow.length; i++) {
				excelFieldNames[i] = firstRow[i].getContents().toString()
						.trim();
			}

			// 判断需要的字段在Excel中是否都存在
			boolean isExist = true;
			List<String> excelFieldList = Arrays.asList(excelFieldNames);
			for (String cnName : fieldMap.keySet()) {
				if (!excelFieldList.contains(cnName)) {
					isExist = false;
					break;
				}
			}

			// 如果有列名不存在，则抛出异常，提示错误
			if (!isExist) {
				throw new ExcelException("Excel中缺少必要的字段，或字段名称有误");
			}

            // 将列名和列号放入Map中,这样通过列名就可以拿到列号
            LinkedHashMap<String, Integer> colMap = new LinkedHashMap<String, Integer>();
            for (int i = 0; i < excelFieldNames.length; i++) {
                colMap.put(excelFieldNames[i], firstRow[i].getColumn());
            }
            // 将sheet转换为list
            // 定义空行不能超过1个如果超过1个视为下面没有数据

            for (int i = 1; i < sheet.getRows(); i++) {
                // 新建要转换的对象
                T entity = entityClass.newInstance();
                int blankCount = 0;
                // 给对象中的字段赋值
                for (Entry<String, String> entry : fieldMap.entrySet()) {
                    // 获取中文字段名
                    String cnNormalName = entry.getKey();
                    // 获取英文字段名
                    String enNormalName = entry.getValue();
                    // 根据中文字段名获取列号
                    int col = colMap.get(cnNormalName);
                    if (!sheet.getName().equals("短信营销会员列表")) {
                        //检验空行
                        Cell currentCell = sheet.getCell(col, i);
                        if (currentCell == null || "".equals(currentCell.getContents().toString())) {
                            blankCount++;
                            //检查下面1行
                            currentCell = sheet.getCell(col, i + 1);
                            if (currentCell == null || "".equals(currentCell.getContents().toString())) {
                                blankCount++;
                            }
                            //如果大于1说明有连续空行下面就不做处理
                            if (blankCount > 1) {
                                break;
                            } else {
                                blankCount = 0;
                                continue;
                            }
                        }
                    }

                    // 获取当前单元格中的内容
                    String content = sheet.getCell(col, i).getContents()
                        .toString().trim();
                    // 给对象赋值
                    setFieldValueByName(enNormalName, content, entity);
                }

				//如果空行大于1就退出
				if(blankCount > 1){
					break;
				}
				//判断实体是否有rowNum属性，如果有就设置值
				if(getFieldByName("rowNum", entity.getClass()) != null){
					//先将行数设置到实体里:行数加1
					setFieldValueByName("rowNum",i+1, entity);
				}

				//保存该实体
				resultList.add(entity);
			}
		} catch (Exception e) {
			LOG.error("导入报错",e);
			// 如果是ExcelException，则直接抛出
			if (e instanceof ExcelException) {
				throw (ExcelException) e;

				// 否则将其它异常包装成ExcelException再抛出
			} else {
				LOG.error("导入报错",e);
				throw new ExcelException("导入Excel失败");
			}
		}
		return resultList;
	}

	/**
	 * 原版本空列当做空行用了，不符合公告系统excel上传需求
	 * @param in
	 * @param sheetName sheet 名称
	 * @param entityClass excel 对应的实体类信息
	 * @param fieldMap 列头信息
	 * @param <T>
	 * @return
	 * @throws ExcelException
	 */
	public static <T> List<T> excelToListNew(InputStream in, String sheetName, Class<T> entityClass, LinkedHashMap<String, String> fieldMap) throws ExcelException {
		// 定义要返回的list
		List<T> resultList = new ArrayList<T>();

		try {

			// 根据Excel数据源创建WorkBook
			Workbook wb = Workbook.getWorkbook(in);
			// 获取工作表
			Sheet sheet = wb.getSheet(sheetName);

			// 如果Excel中没有数据则提示错误
			if (sheet.getRows() <= 1) {
				throw new ExcelException("Excel文件中没有任何数据");
			}

			Cell[] firstRow = sheet.getRow(0);

			String[] excelFieldNames = new String[firstRow.length];

			// 获取Excel中的列名
			for (int i = 0; i < firstRow.length; i++) {
				excelFieldNames[i] = firstRow[i].getContents().toString()
						.trim();
			}

			// 判断需要的字段在Excel中是否都存在
			boolean isExist = true;
			List<String> excelFieldList = Arrays.asList(excelFieldNames);
			for (String cnName : fieldMap.keySet()) {
				if (!excelFieldList.contains(cnName)) {
					isExist = false;
					break;
				}
			}

			// 如果有列名不存在，则抛出异常，提示错误
			if (!isExist) {
				throw new ExcelException("Excel中缺少必要的字段，或字段名称有误");
			}

			// 将列名和列号放入Map中,这样通过列名就可以拿到列号
			LinkedHashMap<String, Integer> colMap = new LinkedHashMap<String, Integer>();
			for (int i = 0; i < excelFieldNames.length; i++) {
				colMap.put(excelFieldNames[i], firstRow[i].getColumn());
			}
			// 将sheet转换为list
			// 定义空行不能超过1个如果超过1个视为下面没有数据

			for (int i = 1; i < sheet.getRows(); i++) {
				// 新建要转换的对象
				T entity = entityClass.newInstance();
				int blankCount = 0 ;
				// 给对象中的字段赋值
				for (Entry<String, String> entry : fieldMap.entrySet()) {
					// 获取中文字段名
					String cnNormalName = entry.getKey();
					// 获取英文字段名
					String enNormalName = entry.getValue();
					// 根据中文字段名获取列号
					int col = colMap.get(cnNormalName);

					//检验空行
					/*Cell currentCell = sheet.getCell(col, i);
					if (currentCell == null || "".equals(currentCell.getContents().toString())) {
						blankCount ++;
						//检查下面1行
						currentCell = sheet.getCell(col, i+1);
						if(currentCell == null|| "".equals(currentCell.getContents().toString())){
							blankCount ++;
						}
						//如果大于1说明有连续空行下面就不做处理
						if(blankCount > 1){
							break;
						}else {
							blankCount = 0;
							continue;
						}
					}*/
					// 获取当前单元格中的内容
					String content = sheet.getCell(col, i).getContents()
							.toString().trim();
					// 给对象赋值
					if (StringUtil.isNotBlank(content)) {
						setFieldValueByName(enNormalName, content, entity);
					}
				}

				//如果空行大于1就退出
				if(blankCount > 1){
					break;
				}
				//判断实体是否有rowNum属性，如果有就设置值
				if(getFieldByName("rowNum", entity.getClass()) != null){
					//先将行数设置到实体里:行数加1
					setFieldValueByName("rowNum",i+1, entity);
				}
				//保存该实体
				resultList.add(entity);
			}
		} catch (Exception e) {
			LOG.error("导入报错",e);
			// 如果是ExcelException，则直接抛出
			if (e instanceof ExcelException) {
				throw (ExcelException) e;

				// 否则将其它异常包装成ExcelException再抛出
			} else {
				LOG.error("导入报错",e);
				throw new ExcelException("导入Excel失败");
			}
		}
		return resultList;
	}
	/*
        * 如果有错误返回带错误信息的记录，不影响下一条记录执行
        * */
	public static <T> List<T> excelToListReturnError(InputStream in, String sheetName, Class<T> entityClass, LinkedHashMap<String, String> fieldMap) throws ExcelException {
		LOG.info(String.format("[Excel辅助类]-ExcelToList：ExcelUtil.excelToListReturnError,结束时间：%s", System.currentTimeMillis()));
		// 定义要返回的list
		List<T> resultList = new ArrayList<T>();

		try {
			// 根据Excel数据源创建WorkBook
			Workbook wb = Workbook.getWorkbook(in);
			// 获取工作表
			Sheet sheet = wb.getSheet(sheetName);

			// 如果Excel中没有数据则提示错误
			if (sheet==null||sheet.getRows() <= 1) {
				return null;
				//throw new ExcelException("Excel文件中没有任何数据");
			}

			Cell[] firstRow = sheet.getRow(0);

			String[] excelFieldNames = new String[firstRow.length];

			// 获取Excel中的列名
			for (int i = 0; i < firstRow.length; i++) {
				excelFieldNames[i] = firstRow[i].getContents().toString()
						.trim();
			}

			// 判断需要的字段在Excel中是否都存在
			boolean isExist = true;
			List<String> excelFieldList = Arrays.asList(excelFieldNames);
			for (String cnName : fieldMap.keySet()) {
				if (!excelFieldList.contains(cnName)) {
					isExist = false;
					break;
				}
			}

			// 如果有列名不存在，则抛出异常，提示错误
			if (!isExist) {
				throw new ExcelException("Excel中缺少必要的字段，或字段名称有误");
			}

			// 将列名和列号放入Map中,这样通过列名就可以拿到列号
			LinkedHashMap<String, Integer> colMap = new LinkedHashMap<String, Integer>();
			for (int i = 0; i < excelFieldNames.length; i++) {
				colMap.put(excelFieldNames[i], firstRow[i].getColumn());
			}
			// 将sheet转换为list
			// 定义空行不能超过1个如果超过1个视为下面没有数据
			for (int i = 1; i < sheet.getRows(); i++) {
				// 新建要转换的对象
				T entity = entityClass.newInstance();
				int blankCount = 0 ;
				// 给对象中的字段赋值
				for (Entry<String, String> entry : fieldMap.entrySet()) {
					// 获取中文字段名
					String cnNormalName = entry.getKey();
					// 获取英文字段名
					String enNormalName = entry.getValue();
					// 根据中文字段名获取列号
					int col = colMap.get(cnNormalName);

					//检验空行
					Cell currentCell = sheet.getCell(col, i);
					if (currentCell == null || "".equals(currentCell.getContents().toString())) { //如果当前单元格内是否为空
						boolean isEmpty=true;
						for(int j=0;j<colMap.keySet().size();j++)
						{
							Cell checkCell= sheet.getCell(j,i);
							if(checkCell!=null &&  !("".equals(checkCell.getContents().toString())))
							{
								isEmpty=false;
								break;
							}
						}
						if(isEmpty)
						{
							blankCount++;
							break;
						}
					}
					// 获取当前单元格中的内容
					String content;
					if(sheet.getCell(col,i).getType()!= CellType.DATE)
					{
						content= sheet.getCell(col, i).getContents().toString().trim();
					}else{
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						TimeZone zone = TimeZone.getTimeZone("GMT");
						sdf.setTimeZone(zone);
						content= sdf.format(((DateCell)sheet.getCell(col,i)).getDate());
					}

					// 给对象赋值,如果有错误，记录错误信息，并执行下一条记录
					try
					{
						setFieldValueByName(enNormalName, content, entity);
					}catch (Exception ex)
					{
						ex.printStackTrace();
						//类型转换引起，不做任何处理
					}
				}

				//如果空行大于1就退出
				if(blankCount > 1){
					break;
				}
				try{

					//判断实体是否有rowNum属性，如果有就设置值
					if(getFieldByName("rowNum", entity.getClass()) != null){
						//先将行数设置到实体里:行数加1
						setFieldValueByName("rowNum",i+1, entity);
					}
				}
				catch(Exception ex){
					//类型转换引起，不做任何处理
				}

				//如果没有错误，则保存该实体
				if(entity!=null)
					resultList.add(entity);
			}
		} catch (Exception e) {
			LOG.error("导入报错",e);
			// 如果是ExcelException，则直接抛出
			if (e instanceof ExcelException) {
				throw (ExcelException) e;
				// 否则将其它异常包装成ExcelException再抛出
			} else {
				LOG.error("导入报错",e);
				throw new ExcelException("导入Excel失败");
			}
		}
		LOG.info(String.format("[Excel辅助类]-ExcelToList：ExcelUtil.excelToListReturnError,结束时间：%s", System.currentTimeMillis()));
		return resultList;
	}

    /*
     * 批量导入采购车解析Excel
     * */
    public  static <T> List<T> excelToListReturnErrorAndResult(InputStream in, String sheetName, Class<T> entityClass, LinkedHashMap<String, String> fieldMap, Map<String,Object> error) throws ExcelException {

		// 定义要返回的list
		List<T> resultList = new ArrayList<T>();
		try {

			// 根据Excel数据源创建WorkBook
			Workbook wb = Workbook.getWorkbook(in);
			// 获取工作表
			Sheet sheet = wb.getSheet(sheetName);

			// 如果Excel中没有数据则提示错误
			if (sheet.getRows() <= 1) {
				throw new ExcelException("Excel文件中没有任何数据");
			}

			Cell[] firstRow = sheet.getRow(0);

			String[] excelFieldNames = new String[firstRow.length];

			// 获取Excel中的列名
			for (int i = 0; i < firstRow.length; i++) {
				excelFieldNames[i] = firstRow[i].getContents().toString().trim();
			}

			// 判断需要的字段在Excel中是否都存在
			boolean isExist = true;
			List<String> excelFieldList = Arrays.asList(excelFieldNames);
			for (String cnName : fieldMap.keySet()) {
				if (!excelFieldList.contains(cnName)) {
					isExist = false;
					break;
				}
			}

			// 如果有列名不存在，则抛出异常，提示错误
			if (!isExist) {
				throw new ExcelException("Excel中缺少必要的字段，或字段名称有误");
			}

			// 将列名和列号放入Map中,这样通过列名就可以拿到列号
			LinkedHashMap<String, Integer> colMap = new LinkedHashMap<String, Integer>();
			for (int i = 0; i < excelFieldNames.length; i++) {
				colMap.put(excelFieldNames[i], firstRow[i].getColumn());
			}
			// 将sheet转换为list
			// 定义空行不能超过1个如果超过1个视为下面没有数据

			for (int i = 1; i < sheet.getRows(); i++) {
				// 新建要转换的对象
				T entity = entityClass.newInstance();
				int blankCount = 0 ;
				// 给对象中的字段赋值
				for (Entry<String, String> entry : fieldMap.entrySet()) {
					// 获取中文字段名
					String cnNormalName = entry.getKey();
					// 获取英文字段名
					String enNormalName = entry.getValue();
					// 根据中文字段名获取列号
					int col = colMap.get(cnNormalName);
					// 获取当前单元格中的内容
					String content = sheet.getCell(col, i).getContents()
							.toString().trim();
					String resContent=content;
					if(!StringUtil.isNumber(content)){
                        resContent=trimNumberBlow(content);
                    }
                    try {
						// 给对象赋值
						if(StringUtil.isNotBlank(resContent)&&!"".equals(resContent)) {
							setFieldValueByName(enNormalName, resContent, entity);
						}
						if(StringUtil.isBlank(resContent)&&"skuNum".equals(enNormalName)){
							setFieldValueByName(enNormalName,1,entity);
						}
					}catch (Exception e){
						if(StringUtil.isNotBlank(resContent)&&enNormalName.equals("sku")) {
							error.put(content, "无法识别该sku");
						}
						if(StringUtil.isNotBlank(resContent)&&enNormalName.equals("skuNum"))
						{
							error.put(sheet.getCell(colMap.get("*SKU（最多100个）"), i).getContents()
									.toString().trim(),"商品数量无效");
						}
						if(e.getMessage().equals("导入Excel出错")){
							throw new ExcelException("无效Excel");
						}
					}
				}

				//如果空行大于1就退出
				if(blankCount > 1){
					break;
				}
				//判断实体是否有rowNum属性，如果有就设置值
				if(getFieldByName("rowNum", entity.getClass()) != null){
					//先将行数设置到实体里:行数加1
					setFieldValueByName("rowNum",i+1, entity);
				}
				//保存该实体
				if(getFieldValueByName("sku",entity,null)!= null) {
					resultList.add(entity);
				}
			}
		} catch (Exception e) {
			LOG.error("导入报错",e);
			// 如果是ExcelException，则直接抛出
			if (e instanceof ExcelException) {
				throw (ExcelException) e;

				// 否则将其它异常包装成ExcelException再抛出
			} else {
				LOG.error("导入报错",e);
				throw new ExcelException("导入Excel失败");
			}
		}
		return resultList;
    }
    private static String trimNumberBlow(String str){
        String tmpstr=new String(str);
        for(int i=str.length()-1;i>1;i--){
            char c=str.charAt(i);
            int tmp=(int)c;
            int maxtmp=65533;
            if(tmp==(int)maxtmp){
                tmpstr=tmpstr.substring(0, i);
            }
        }
        return tmpstr;
    }

	/**
	 * 导出工具
	 * 	支持日期format
	 * 	支持字段根据不同的值定制化输出dataCovertUtilMap<字段名， 转换对象>
	 * @param list
	 * @param fieldMap
	 * @param sheetName
	 * @param sheetSize
	 * @param response
	 * @param fileName
	 * @param formatter
	 * @param dataCovertUtilMap
	 * @param <T>
	 * @throws ExcelException
	 */
	public static <T> void listToExcelWithDateAndDataCoverMap(List<T> list,
                                                              LinkedHashMap<String, String> fieldMap, String sheetName,
                                                              int sheetSize, HttpServletResponse response, String fileName, String formatter, Map<String, DataConvertUtil> dataCovertUtilMap)
			throws ExcelException {

		// 如果文件名没提供，则使用时间戳
		if (fileName == null || fileName.trim().equals("")) {
			// 设置默认文件名为当前时间：年月日时分秒
			fileName = new SimpleDateFormat(DateUtil.DEFAULT_DATE_PATTERN)
					.format(new Date()).toString();
		}

		// 设置response头信息
		response.reset();
		response.setContentType("application/vnd.ms-excel"); // 改成输出excel文件
		response.setHeader("Content-disposition", "attachment; filename="
				+ fileName + ".xls");

		// 创建工作簿并发送到浏览器
		OutputStream out = null;
		try {
			out = response.getOutputStream();

			if (list == null ) {
//				throw new ExcelException("数据源中没有任何数据");
				list = Lists.newArrayList();
			}


			if (sheetSize > 65535 || sheetSize < 1) {
				sheetSize = 65535;
			}

			// 创建工作簿并发送到OutputStream指定的地方
			WritableWorkbook wwb;
			wwb = Workbook.createWorkbook(out);

			// 因为2003的Excel一个工作表最多可以有65536条记录，除去列头剩下65535条
			// 所以如果记录太多，需要放到多个工作表中，其实就是个分页的过程
			// 1.计算一共有多少个工作表
			double sheetNum = Math.ceil(list.size()
					/ Integer.valueOf(sheetSize).doubleValue());

			// 2.创建相应的工作表，并向其中填充数据
			WritableSheet sheet = wwb.createSheet(sheetName, 0);
			// 定义存放英文字段名和中文字段名的数组
			String[] enFields = new String[fieldMap.size()];
			String[] cnFields = new String[fieldMap.size()];

			// 填充数组
			int count = 0;
			for (Entry<String, String> entry : fieldMap.entrySet()) {
				enFields[count] = entry.getKey();
				cnFields[count] = entry.getValue();
				count++;
			}
			// 填充表头
			for (int i = 0; i < cnFields.length; i++) {
				Label label = new Label(i, 0, cnFields[i]);
				sheet.addCell(label);
			}

			// 填充内容
			int rowNo = 1;
			for (int index = 0; index < list.size(); index++) {
				// 获取单个对象
				T item = list.get(index);
				for (int i = 0; i < enFields.length; i++) {
					Object objValue = null;

					//增加判断是否为map
					if(item instanceof Map){
						objValue = ((Map) item).get(enFields[i]);
						if(objValue == null){
							objValue = "";
						}
					}else {
						Field field = getFieldByName(enFields[i], item.getClass());
						if (field != null) {
							field.setAccessible(true);
							if(field.getType()==Date.class && field.get(item)!=null)
							{
								try{
									SimpleDateFormat sdf=new SimpleDateFormat(formatter);
									objValue=sdf.format(field.get(item));
								}catch(Exception ex)
								{


								}
							}else{
								objValue = field.get(item);
							}
							//看某些字段是否需要转换输出
							if(MapUtils.isNotEmpty(dataCovertUtilMap) && dataCovertUtilMap.containsKey(enFields[i])) {
								DataConvertUtil dataCovertUtil = dataCovertUtilMap.get(enFields[i]);
								objValue = dataCovertUtil.covertData(objValue);
							}
						} else {
							throw new ExcelException(item.getClass().getSimpleName() + "类不存在字段名 "
									+ enFields[i]);
						}
					}
					String fieldValue = objValue == null ? "" : objValue.toString();
					Label label = new Label(i, rowNo, fieldValue);
					sheet.addCell(label);
				}

				rowNo++;
			}

			// 设置自动列宽
			setColumnAutoSize(sheet, 5);

			wwb.write();
			wwb.close();
		} catch (Exception e) {
			LOG.error("导出报错",e);

			// 如果是ExcelException，则直接抛出
			if (e instanceof ExcelException) {
				throw (ExcelException) e;

				// 否则将其它异常包装成ExcelException再抛出
			} else {
				throw new ExcelException("导出Excel失败");
			}
		}finally{
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
					LOG.error("导出报错",e);
				}
			}
		}
	}
}
