package com;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class crossrefDoi {

	public static void main(String[] args) throws IOException {
		int rows = 100;
		int successive_errors = 1;
		String dt = "2018-09-05";
		String urls = "https://api.crossref.org/works";
		String nextcursor = "*";
		String lsnextcursor = "";
		File file = new File("D:\\crossref\\" + dt);
		FileWriter writer = new FileWriter(file, true);
		boolean bo = true;
		while (bo) {
			String json =""; 
			try {
				if (nextcursor.indexOf("+") > -1 || nextcursor.indexOf(" ") > -1) {
					//这句话比较重要/一定要替换。不然报游标不对
					nextcursor=nextcursor.replace(" ", "%2B").replace("+", "%2B");
				}
				String param = "filter=from-created-date:"+dt+",until-created-date:"+dt+"&rows=" + rows
						+ "&cursor=";
				String Urls = urls + "?" + param + nextcursor;
				CloseableHttpClient httpClient = null;
				CloseableHttpResponse response = null;
				try {
					httpClient = HttpClients.createDefault(); // 创建httpClient实例
					HttpGet httpGet = new HttpGet(Urls); // 创建httpget实例
					RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(100000)
							.setSocketTimeout(100000).setConnectTimeout(100000).build();
					httpGet.setHeader("User-Agent",
							"Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0"); // 设置请求头消息User-Agent
					httpGet.setConfig(requestConfig);
					response = httpClient.execute(httpGet); // 执行http get请求
				} catch (Exception e) {
					continue;
				}
				HttpEntity entity = response.getEntity();
				json = EntityUtils.toString(entity, "utf-8");
				response.close();
				httpClient.close();
			} catch (Exception e) {
				System.out.println("发�?? POST 请求出现异常�?" + e);
				e.printStackTrace();
			}

			if (!nextcursor.equals(lsnextcursor)) {
				lsnextcursor = nextcursor;
				JSONObject jsonObject = JSON.parseObject(json).getJSONObject("message");
				nextcursor = jsonObject.getString("next-cursor");
				System.out.println(nextcursor);
				JSONArray datas = jsonObject.getJSONArray("items");
				if (datas.size() == 0 || datas.size() < rows) {
					bo = false;
				}
				successive_errors = 1;
				rows = 100;
				for (int j = 0; j < datas.size(); j++) {
					writer.write(datas.getJSONObject(j).toJSONString() + "\n");
				}
			} else {
				JSONObject jsonObject = JSON.parseObject(json).getJSONObject("message");
				nextcursor = jsonObject.getString("next-cursor");
			}
		}
		writer.close();
	}
}
