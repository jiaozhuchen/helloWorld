package com.example.demo.util;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

/**
 * excel导入工具
 */
public class ExcelFilePOIUtil {

    private static Logger logger = LoggerFactory.getLogger(ExcelFilePOIUtil.class);
    /**
     * excel行解析
     */
    public interface RowParser<T> {
        /**
         * 解析一行数据
         *
         * @param row excel行
         */
        T parse(int rowNum, HSSFRow row);
    }

    /**
     * 单元格解析器
     * @param <T>
     */
    public interface CellParser<T> {
        /**
         * 解析一个单元格数据
         * @param rowObject 行对象
         * @param rowNum 行号
         * @param cellNum 列号
         * @param cellValue 单元格值【已经做了排空处理】
         */
        void parse(T rowObject, int rowNum, int cellNum, String cellValue);
    }

    /**
     * 读取excel文件并转换为指定的对象列表
     * @param file 上传文件
     * @param headers 表头, 如果表头存在就会校验第一列是否与表头一致
     * @throws IllegalArgumentException 读取失败, 返回失败原因
     */
    public static <T> List<T> readFromExcel(MultipartFile file, String[] headers, RowParser<T> parser) {
        String fileName = file.getOriginalFilename();
        Assert.isTrue(fileName.endsWith(".xls"), "文件格式不对，请上传扩展名是xls的文件");
        Assert.isTrue(!file.isEmpty(), "文件不存在");
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            logger.error("IO异常, 解析Excel文件失败", e);
            throw new IllegalArgumentException("IO异常, 解析Excel文件失败");
        }
        return readFromExcel(inputStream, headers, parser);
    }

    /**
     * 读取excel文件并转换为指定的对象列表
     * @param file 上传文件
     * @param headers 表头, 如果表头存在就会校验第一列是否与表头一致
     * @throws IllegalArgumentException 读取失败, 返回失败原因
     */
    public static <T> List<T> readFromExcel(MultipartFile file, String[] headers, final Class<T> clazz,
                                            final CellParser<T> parser) {
        return readFromExcel(file, headers, getRowParser(clazz, parser));
    }

    /**
     * 读取excel文件并转换为指定的对象列表
     * @param inputStream 输入流
     * @param headers 表头, 如果表头存在就会校验第一列是否与表头一致
     */
    public static <T> List<T> readFromExcel(InputStream inputStream, String[] headers, RowParser<T> parser) {
        HSSFWorkbook workbook;
        try {
            workbook = new HSSFWorkbook(inputStream);
        } catch (IOException e) {
            logger.error("IO异常, 解析Excel文件失败", e);
            throw new IllegalArgumentException("IO异常, 解析Excel文件失败");
        }

        HSSFSheet sheet = workbook.getSheetAt(workbook.getFirstVisibleTab());
        List<T> rows = Lists.newArrayList();
        try {
            boolean validated = false;
            List<String> msgs = Lists.newArrayList();
            for (int rowNum = sheet.getFirstRowNum(), maxRowNum = sheet.getLastRowNum(); rowNum <= maxRowNum; rowNum++) {
                HSSFRow row = sheet.getRow(rowNum);
                if (row == null) {
                    continue;
                }

                if (!validated) {
                    validated = true;
                    validate(headers, row);
                    continue;
                }

                try {
                    T rowObject = parser.parse(rowNum, row);
                    if (rowObject == null) {
                        continue;
                    }
                    rows.add(rowObject);
                } catch (Exception e) {
                    msgs.add("行" + rowNum + ":" + e.getMessage());
                }
            }

            if (CollectionUtils.isEmpty(msgs)) {
                return rows;
            } else {
                throw new IllegalArgumentException(StringUtils.join(msgs, "\n"));
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("IO异常, 解析Excel文件失败", e);
            throw new IllegalArgumentException("IO异常, 解析Excel文件失败");
        }
    }

    /**
     * 读取excel文件并转换为指定的对象列表
     * @param inputStream 输入流
     * @param headers 表头, 如果表头存在就会校验第一列是否与表头一致
     */
    public static <T> List<T> readFromExcel(InputStream inputStream, String[] headers, final Class<T> clazz,
            final CellParser<T> parser) {
        return readFromExcel(inputStream, headers, getRowParser(clazz, parser));
    }

    /**
     * 创建行解析器
     * @param clazz 行类型
     * @param parser 单元格解析器
     * @param <T> 泛型类型
     * @return 行解析器
     */
    private static <T> RowParser<T> getRowParser(final Class<T> clazz, final CellParser<T> parser) {
        return new RowParser<T>() {
            @Override
            public T parse(int rowNum, HSSFRow row) {
                T rowObject;
                try {
                    rowObject = clazz.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("对象初始化失败");
                }

                List<String> msgs = Lists.newArrayList();
                for (int cellNum = row.getFirstCellNum(), maxCellNum = row.getLastCellNum();
                     cellNum < maxCellNum; cellNum ++) {
                    try {
                        HSSFCell cell = row.getCell(cellNum);
                        String cellValue = getCellValue(cell);
                        if (StringUtils.isNotEmpty(cellValue)) {
                            parser.parse(rowObject, rowNum, cellNum, cellValue);
                        }
                    } catch (Exception e) {
                        msgs.add("[列" + cellNum + ":" + e.getMessage() + "]");
                    }
                }
                if (CollectionUtils.isEmpty(msgs)) {
                    return rowObject;
                } else {
                    throw new IllegalArgumentException(StringUtils.join(msgs, ""));
                }
            }
        };
    }

    /**
     * 读取excel文件并转换为指定的对象列表
     * @param fileName excel源
     * @param headers 表头, 如果表头存在就会校验第一列是否与表头一致
     */
    public static <T> List<T> readFromExcel(String fileName, String[] headers, RowParser<T> parser) {
        Assert.hasText(fileName, "文件路径错误");
        File file = new File(fileName);
        Assert.isTrue(file.exists(), "文件不存在");
        Assert.isTrue(file.isFile(), "文件格式错误, 不能解析文件夹");
        Assert.isTrue(file.getName().endsWith(".xls"), "文件格式不对，请上传扩展名是xls的文件");
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.error("IO异常, 解析Excel文件失败", e);
            throw new IllegalArgumentException("IO异常, 解析Excel文件失败");
        }
        return readFromExcel(inputStream, headers, parser);
    }

    /**
     * 校验头部信息.
     * <pre>
     *     只有当headers有效的时候才会进行头部信息校验
     * </pre>
     */
    private static void validate(String[] headers, HSSFRow row) {
        if (ArrayUtils.isNotEmpty(headers)) {
            int actualNum = row.getPhysicalNumberOfCells();
            String message = "文件头部信息不一致, 校验失败, 期望值:\n" + Arrays.toString(headers);
            Assert.isTrue(headers.length == actualNum, message);
            for (int cellNum = row.getFirstCellNum(), maxCellNum = row.getLastCellNum(), index = 0;
                 cellNum < maxCellNum; cellNum++, index++) {
                String actual = getCellValue(row.getCell(cellNum));
                Assert.hasText(actual, message);

                String except = headers[index];
                Assert.hasText(except, message);

                Assert.isTrue(except.equals(actual), message);
            }
        }
    }

    /**
     * 模板下载
     * @param headers 表头
     * @param fileName 模板名称
     */
    public static void downTemplate(HttpServletRequest request, HttpServletResponse response, String[] headers,
            List<DataValidation> validations, String fileName) {
        try {
            Workbook workbook = createTemplate(headers, validations);
            workbook.write(response.getOutputStream());

            String userAgent = request.getHeader("User-Agent");
            //针对IE或者以IE为内核的浏览器：
            if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
                fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
            } else {
                //非IE浏览器的处理：
                fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
            }

            response.setHeader("Content-Disposition","attachment;filename=" + fileName + ".xls");
            response.setHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
            workbook.close();
        } catch (Exception e) {
            logger.error("模板导出失败:{}", fileName, e);
        }
    }

    private static Workbook createTemplate(String[] headers, List<DataValidation> validations) {
        Assert.isTrue(headers != null && headers.length > 0, "模板头部信息为空");
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row temRow = sheet.createRow(0);
        int column = 0;
        for (String header : headers) {
            if (StringUtils.isEmpty(header)) {
                continue;
            }
            temRow.createCell(column++).setCellValue(header);
        }

        if (CollectionUtils.isNotEmpty(validations)) {
            for (DataValidation validation : validations) {
                sheet.addValidationData(validation);
            }
        }

        return workbook;
    }

    public static String getCellValue(HSSFCell cell) {
        String value = null;
        if (cell != null) {
            switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_FORMULA:
                try {
                    value = String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    value = "";
                }
                break;
            case HSSFCell.CELL_TYPE_NUMERIC:
                DecimalFormat df = new DecimalFormat("0");
                value = df.format(cell.getNumericCellValue());
                //value = String.valueOf(cell.getNumericCellValue());
                break;
            case HSSFCell.CELL_TYPE_STRING:
                value = String.valueOf(cell.getStringCellValue());
                break;

            default:
                value = "";
                break;
            }
        }
        return value;
    }

}
