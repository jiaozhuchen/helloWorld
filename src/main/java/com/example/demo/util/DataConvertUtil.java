package com.example.demo.util;

import org.apache.commons.collections.MapUtils;

import java.util.Map;

/**
 * Created on 2012-7-17
 * @author:chenchen39
 * <p>Description: [导出类根据不同值转换结果]</p>
 * @version     1.0
 */
public class DataConvertUtil {

	/**
	 * 根据不同的值返回不同的输出
	 */
	private Map<Object, String> dataCovertMap;
	private String emptyString = "";
	private String defaultString = "";

	public String covertData(Object obj) {
		if(obj == null) {
			return emptyString;
		}else {
			if(MapUtils.isEmpty(dataCovertMap)) {
				return emptyString;
			}
			for(Map.Entry<Object, String> entry : dataCovertMap.entrySet()) {
				Object o = entry.getKey();
				if(o.equals(obj)) {
					return entry.getValue();
				}
			}
			return defaultString;
		}
	}

	public Map<Object, String> getDataCovertMap() {
		return dataCovertMap;
	}

	public void setDataCovertMap(Map<Object, String> dataCovertMap) {
		this.dataCovertMap = dataCovertMap;
	}

	public String getEmptyString() {
		return emptyString;
	}

	public void setEmptyString(String emptyString) {
		this.emptyString = emptyString;
	}

	public String getDefaultString() {
		return defaultString;
	}

	public void setDefaultString(String defaultString) {
		this.defaultString = defaultString;
	}
}
