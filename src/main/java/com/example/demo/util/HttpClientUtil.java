/*
 * Copyright (c) 2015, www.jd.com. All rights reserved.
 *
 * 警告：本计算机程序受著作权法和国际公约的保护，未经授权擅自复制或散布本程序的部分或全部、以及其他
 * 任何侵害著作权人权益的行为，将承受严厉的民事和刑事处罚，对已知的违反者将给予法律范围内的全面制裁。
 */
package com.example.demo.util;

import net.sf.json.JSONObject;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Description: HttpClient工具类
 *
 * @author wangjinxu@jd.com
 * @version 1.0.0
 */
/*
 * =========================== 维护日志 =========================== 
 * 2015年11月23日 下午4:10:40 wangjinxu@jd.com 新建代码 
 * 2016年05月31日 上午10：47:10 lichangqing3 添加方法post(String url, Map<String, String> params, String[] charset)
 * 2016年09月14日 mengxianglei 增加fetchResponseStream方法
 * =========================== 维护日志 ===========================
 */
public class HttpClientUtil {
	public interface Callback<R> {
		R run(InputStream inputStream) throws IOException;
	}

	private final static Logger LOG =  LoggerFactory.getLogger(HttpClientUtil.class);
	private static final HttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();

	/**
	 * 发送POST请求
	 * @param url postURL
	 * @param params 请求参数
	 */
	public static JSONObject post(String url, Map<String,String> params) {
		return post(url, params, "utf-8");
	}

	/**
	 * 发送POST请求
	 * @param url postURL
	 * @param params 请求参数
	 * @param charset 编码
	 */
	public static JSONObject post(String url, Map<String,String> params, String charset) {
		JSONObject jsonResult = null;

		HttpClient httpCilent = new HttpClient();
		PostMethod postMethod = new PostMethod(url);

		try {
			int index = 0;
			NameValuePair[] param = new NameValuePair[params.size()];
			for (Map.Entry<String, String> entry : params.entrySet()) {
				param[index] = new NameValuePair(entry.getKey(), entry.getValue());
				index++;
			}
			postMethod.setRequestBody(param);

			httpCilent.executeMethod(postMethod);
			if (postMethod.getStatusCode() == HttpStatus.SC_OK) {
				String responseBody = postMethod.getResponseBodyAsString();
				jsonResult = JSONObject.fromObject(responseBody);
			}
		} catch (Exception e) {
			LOG.error("post请求提交失败:" + url, e);
		}  finally {
			postMethod.releaseConnection();
		}

		return jsonResult;
	}

	/**
	 * 发送post请求 utf-8
	 */
	public static JSONObject post(String url, Map<String, String> params, String[] charsets) {
		JSONObject jsonResult = null;
		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod(url);

		try {
			int index = 0;
			NameValuePair[] param = new NameValuePair[params.size()];
			for (Map.Entry<String, String> entry : params.entrySet()) {
				param[index] = new NameValuePair(entry.getKey(), entry.getValue());
				index++;
			}
			postMethod.setRequestBody(param);
			httpClient.executeMethod(postMethod);
			if (postMethod.getStatusCode() == HttpStatus.SC_OK) {
				String responseBody = new String(postMethod.getResponseBody(), charsets[0]);
				jsonResult = JSONObject.fromObject(responseBody);
			}
		} catch (Exception e) {
			LOG.error("post请求提交失败:" + url, e);
		} finally {
			postMethod.releaseConnection();
		}

		return jsonResult;
	}

	/**
	 * 发送GET请求
	 * @param url getURL
	 * @param params 请求参数
	 */
	public static JSONObject get(String url, Map<String,String> params) {
		return get(url, params, "utf-8");
	}

	/**
	 * 发送GET请求
	 * @param url getURL
	 * @param params 请求参数
	 * @param charset 编码
	 */
	public static JSONObject get(String url, Map<String,String> params, String charset) {
		LOG.info("params = " + params);
		JSONObject jsonResult = null;

		HttpClient httpCilent = new HttpClient();
		GetMethod getMethod = new GetMethod(url);

		try {
			int index = 0;
			NameValuePair[] param = new NameValuePair[params.size()];
			for (Map.Entry<String, String> entry : params.entrySet()) {
				param[index] = new NameValuePair(entry.getKey(), entry.getValue());
				index++;
			}
			getMethod.setQueryString(param);

			httpCilent.executeMethod(getMethod);
			if (getMethod.getStatusCode() == HttpStatus.SC_OK) {
				String responseBody = getMethod.getResponseBodyAsString();
				LOG.info("调用库存 响应结果:{}", responseBody);
				jsonResult = JSONObject.fromObject(responseBody);
			}
		} catch (Exception e) {
			LOG.error("get请求提交失败:" + url, e);
		} finally {
			getMethod.releaseConnection();
		}

		LOG.info("jsonResult = " + jsonResult);
		return jsonResult;
	}

	/**
	 * 发送GET请求
	 * @param url getURL
	 * @param params 请求参数
	 * @param charset 编码
	 */
	public static String doGet(String url, Map<String,String> params, String charset) {

		HttpClient httpCilent = new HttpClient();
		url = getUrl(url, params);
		LOG.info("搜索接口拼成的url = " + url);
		GetMethod getMethod = new GetMethod(url);
		String content = null;
		try {
			InputStream inputStream = null;
			try {
				if (httpCilent.executeMethod(getMethod) == HttpStatus.SC_OK) {
					inputStream = getMethod.getResponseBodyAsStream();
					content = IOUtils.toString(inputStream, charset);
				} else {
					LOG.info("#Http3Utils.doGet.executeMethod failed=" + getMethod.getStatusLine());
				}
			}
			catch (Exception e) {
				LOG.error("#Http3Utils.doGet.error=",e);
			}
			finally {
				IOUtils.closeQuietly(inputStream);
				getMethod.releaseConnection();
			}
		} catch (Exception e) {
			LOG.error("get请求提交失败:" + url, e);
		} finally {
			getMethod.releaseConnection();
		}

		return content;
	}

	/**
	 *
	 * getUrl:获取. <br/>
	 * @param url  请求地址
	 * @param parameterMap 请求参数
	 * @return
	 */
	private static String getUrl(String url, Map<String, String> parameterMap){
		if (parameterMap==null || parameterMap.size()==0) {
			return url;
		}

		// 设置请求参数 // 采用更有效率的entryset modified by  gaotianlin
		if(url.indexOf("?")==-1){
			url += "?";
		}
		StringBuffer bf = new StringBuffer(url);
		for (Map.Entry<String,String> key:parameterMap.entrySet()) {
			bf.append("&");
			bf.append(key.getKey());
			bf.append("=");
			bf.append(key.getValue());
		}
		return bf.toString();
	}

	public static String fetchResponseData(String url) {
		return fetchResponseData(url, "utf-8");
	}

	public static String fetchResponseData(String url, String charset) {
		String responseBody = null;
		HttpClient httpClient = new HttpClient(httpConnectionManager);
		GetMethod getMethod = new GetMethod(url);
		getMethod.getParams().setContentCharset(charset);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK) {
				LOG.error("http client error, url:{}, status:{}", url,getMethod.getStatusLine());
				throw new RuntimeException("http client error");
			}
			responseBody = getMethod.getResponseBodyAsString();
			//读取内容 
		} catch (IOException e) {
			LOG.error("http client error, url:{}" + url, e);
			throw new RuntimeException("http client exception", e);
		} finally {
			getMethod.releaseConnection();
		}
		return responseBody;
	}

	public static <R> R fetchResponseStream(String url, int connectionTimeout, int soTimeout, Callback<R> callback) throws IOException {
		HttpClient httpClient = new HttpClient();
		HttpConnectionManagerParams params = httpClient.getHttpConnectionManager().getParams();
		params.setConnectionTimeout(connectionTimeout);
		params.setSoTimeout(soTimeout);

		GetMethod getMethod = new GetMethod(url);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		InputStream responseStream = null;
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK) {
				LOG.error("http client error, url:{}, status:{}", url, getMethod.getStatusLine());
				throw new RuntimeException("http client error");
			}
			//读取内容
			responseStream = getMethod.getResponseBodyAsStream();
			return callback.run(responseStream);
		} catch (IOException e) {
			LOG.error("http client error, url:{}" + url, e);
			throw e;
		} finally {
			getMethod.releaseConnection();
			httpClient.getHttpConnectionManager().closeIdleConnections(0);
		}
	}
}
