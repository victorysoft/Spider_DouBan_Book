package com.itkim.Main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.ibatis.session.SqlSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.itkim.mapper.BookMapper;
import com.itkim.mapper.ProxyIpMapper;
import com.itkim.pojo.ProxyIp;
import com.itkim.tools.DBTools;
import com.itkim.tools.HttpClientTool;

/**
 * @description: 从多个网站上爬取代理ip
 * @author: 周末的自习课
 * @date: 2019-3-28
 */
public class GetProxyIP {
	
	private static SqlSession session = DBTools.getSession();
	// mybatis面向接口的编程 可以不用实现接口BookMapper
	private static ProxyIpMapper proxyIpMapper = session.getMapper(ProxyIpMapper.class);
	
	private static ArrayBlockingQueue<String> proxyIpQueue = new ArrayBlockingQueue(8000);
	
	public static void main(String[] args) throws IOException{
		for(int i = 1;i <= 1;i++){
			String url = "https://www.kuaidaili.com/free/intr/" + i;
			System.out.println("现在爬取的页面是："+url);
			saveProxyIp(url);
		}
		//System.out.println(checkProxyIp("223.85.196.75", 9797,"HTTP"));
	}
	
	//爬取免费ip 网站：快代理网站    url=> https://www.kuaidaili.com/free/intr/
	public static List<String> getProxyIpMethodOne(String url) throws IOException{
		
		List<String> listOne = new ArrayList<String>();
		Document document = Jsoup.parse(HttpClientTool.get(url));
		Elements elements = document.select("div#list > table.table.table-bordered.table-striped > tbody > tr");
		for(int i = 1;i <= elements.size();i++){
			//ip
			String ip = elements.select("tr:nth-child("+ i +") > td:nth-child(1)").text().trim();
			if(ip == null || ip.length() < 8){
				continue;
			}
			//端口 
			String tmpPort = elements.select("tr:nth-child("+ i +") > td:nth-child(2)").text().trim();
			int port = 80;
			if(tmpPort != null && !("".equals(tmpPort))){
				 port = Integer.parseInt(tmpPort);
			}
			//协议 http或https
			String protocol = elements.select("tr:nth-child("+ i +") > td:nth-child(4)").text().trim();
			//地址
			String address = elements.select("tr:nth-child("+ i +") > td:nth-child(5)").text().trim();
			if(address == null || "".equals(address)){
				address = "未知";
			}
			//拼接字符串   存入list中   后期取出在多行程中用
			String str = ip + "," + port + "," + protocol + "," + address;
			listOne.add(str);
		}
		return listOne;
	}
	//爬取免费ip 网站：西刺网站   url=> https://www.xicidaili.com/wn
	public static List<String> getProxyIpMethodTwo(String url) throws IOException{
		List<String> listTwo = new ArrayList<String>();
		Document document = Jsoup.parse(HttpClientTool.get(url));
		Elements elements= document.select("div#wrapper > div#body > table#ip_list > tbody > tr");
		
		for(int i = 1;i<=elements.size();i++){
			//ip
			String ip = elements.select("tr:nth-child(" + i + ") > td:nth-child(2)").text().trim();
			if(ip == null || ip.length() < 8){
				continue;
			}
			//port  设置默认端口80
			String tmpPort = elements.select("tr:nth-child(" + i + ") > td:nth-child(3)").text().trim();
			int port = 80;
			if(tmpPort != null && !("".equals(tmpPort))){
				 port = Integer.parseInt(tmpPort);
			}
			//此网页全部为https协议
			String protocol = "https";
			//地址
			String address = elements.select("tr:nth-child("+ i +") > td:nth-child(4)").text().trim();
			if(address == null || "".equals(address)){
				address = "未知";
			}
			//拼接字符串   存入list中   后期取出在多行程中用
			String str = ip + "," + port + "," + protocol + "," + address;
			listTwo.add(str);
		}
		return listTwo;
	}
	//检查爬取代理ip是否可用
	public static boolean checkProxyIp(String ip,int port,String protocol){
		// 创建httpClient实例
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建httpGet实例    用豆瓣来测试代理ip
        HttpGet httpGet = new HttpGet("https://book.douban.com/tag/%E5%8E%86%E5%8F%B2");
        //设置代理
        HttpHost proxy = new HttpHost(ip,port,protocol);
        //设置4秒连接超时  响应4秒超时
        RequestConfig requestConfig = RequestConfig.custom()
        		     								.setConnectTimeout(4000)
        		     								.setSocketTimeout(4000)
        		     								.setProxy(proxy).build();
        httpGet.setConfig(requestConfig);
        //模拟浏览器
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        CloseableHttpResponse response;
		try {
				System.out.println("--检查ip链接----开始-- ip= " + ip + " prot= " + port +  " protocol=" + protocol);
				response = httpClient.execute(httpGet);
				String statusCode = response.getStatusLine().toString();
				System.out.println("--检查ip链接----结束--");
		        if(statusCode.contains("200")){
		        	return true;
		        }else{
		        	return false;
		        }
			} catch (ClientProtocolException e) {	
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			

    }
	
	//创建代理ip池 将爬取来的ip存储到数据库中
	public static void saveProxyIp(String url) throws IOException{
		//快代理ip代理网站
		//String kdlUrl = "https://www.kuaidaili.com/free/intr/";
		List<String> list = getProxyIpMethodOne(url);
		
		//西刺ip代理网站   https类型的ip
		//String xcUrl = "https://www.xicidaili.com/wn/";
		//List<String> list = getProxyIpMethodTwo(url);
		
		//所有代理ip都放到list1中
		//list1.addAll(list2);
		for(String str:list){
			//拼接的url入队列
			proxyIpQueue.offer(str);
		}
		//创建线程池
		final ExecutorService proxyIpThreadPool = Executors.newFixedThreadPool(3);
		while(proxyIpQueue.size() > 0){
			System.out.println("队列中剩余：" + proxyIpQueue.size());
			proxyIpThreadPool.execute(new Runnable(){
				public void run(){
					System.out.println(Thread.currentThread().getName() + "--------开始");
					String str = proxyIpQueue.poll();
					String[] tmpUrl = str.split(",");
					String ip = tmpUrl[0];
					int port = Integer.parseInt(tmpUrl[1]);
					String protocol = tmpUrl[2];
					String address = tmpUrl[3];
					//检验爬取的ip是否可用
					if(checkProxyIp(ip,port,protocol)){
						ProxyIp proxyIp = new ProxyIp();
						proxyIp.setIp(ip);
						proxyIp.setPort(port);
						proxyIp.setProtocol(protocol);
						proxyIp.setAddress(address);
						//ip入库
						proxyIpMapper.insertProxyIp(proxyIp);
						session.commit();
					}
					System.out.println(Thread.currentThread().getName() + "--------一次执行结束");
				}
			});
		}
		proxyIpThreadPool.shutdown();
		System.out.println("--------线程池已关闭--------");
	}
}
