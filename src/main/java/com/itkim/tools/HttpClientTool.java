package com.itkim.tools;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.CookiePolicy;

/**
 * @description: TODO
 * @author: KimJun
 * @date: 19/1/10 17:44
 */
public class HttpClientTool {
    /**
     * 访问接口的请求
     * @param url 接口的链接
     * @return 返回json的数据
     * @throws IOException
     */
    public static String get(String url) throws IOException {
    	
    	// 创建httpClient实例
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建httpGet实例
        HttpGet httpGet = new HttpGet(url);       
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        CloseableHttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        // response自动关闭
        return EntityUtils.toString(entity);
    }
    
    //使用代理ip
    public static String useProxyGetConnection(String url,String ip,int port,String protocol) throws IOException {
    	
    	// 创建httpClient实例
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建httpGet实例
        HttpGet httpGet = new HttpGet(url);
        //设置代理
        HttpHost proxy = new HttpHost(ip,port,protocol);
        //设置10秒连接超时
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10000).setProxy(proxy).build();
        httpGet.setConfig(requestConfig);
        //模拟浏览器
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        CloseableHttpResponse response = httpClient.execute(httpGet);
        String statusCode = response.getStatusLine().toString();
        HttpEntity entity = response.getEntity();
        // response自动关闭
        return EntityUtils.toString(entity);
    }
}
