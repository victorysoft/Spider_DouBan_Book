package com.itkim.Main;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.ibatis.session.SqlSession;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jdom2.JDOMException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.itkim.mapper.BookMapper;
import com.itkim.mapper.BooksMapper;
import com.itkim.mapper.CommentMapper;
import com.itkim.mapper.CommentsMapper;
import com.itkim.mapper.ProxyIpMapper;
import com.itkim.pojo.Book;
import com.itkim.pojo.Books;
import com.itkim.pojo.Comment;
import com.itkim.pojo.Comments;
import com.itkim.pojo.ProxyIp;
import com.itkim.tools.DBTools;
import com.itkim.tools.HttpClientTool;
import com.vdurmont.emoji.EmojiParser;

/**
 * @description: 写了三个方法去爬豆瓣的图书信息 为了效率必须先到豆瓣图书的主页爬取书的id 拿到书的id后会存到数据库去重
 *               然后再去爬图书的详细信息和热门短评
 * @author: 周末的自习课
 * @date: 3/10/19 00:56
 */
public class Main {

	private static SqlSession session = DBTools.getSession();
	// mybatis面向接口的编程 可以不用实现接口BookMapper
	private static BookMapper mapper = session.getMapper(BookMapper.class);
	private static BooksMapper booksMapper = session.getMapper(BooksMapper.class);
	private static CommentsMapper commentsMapper = session.getMapper(CommentsMapper.class);
	// 创建任务队列，可以根据你JVM堆内存大小，修改这里的容量。
	private static ArrayBlockingQueue<String> bookQueue = new ArrayBlockingQueue(8000);
	private static ArrayBlockingQueue<String> commentQueue = new ArrayBlockingQueue(8000);

	/**
	 * 爬取主函数
	 */
	public static void main(String[] args) throws Exception {

		// 从主目录中爬取书的id 总共爬取21页
		for (int start = 0; start <= 200; start = start + 20) {
			String url = "https://book.douban.com/tag/%E5%8E%86%E5%8F%B2?start=" + start + "&type=T";
			sclawByIndex(url);
		}
		// 爬取书的详细信息和热门评论   采用线程池
		List<Integer> allBookIdList = booksMapper.getAllBookId();
		for (int i = 0; i < allBookIdList.size(); i++) {
			//爬取书的详细信息url
		    String bookDetailUrl = "https://douban.uieee.com/v2/book/"+ allBookIdList.get(i);
		    bookQueue.offer(bookDetailUrl);
		    
		  //书的热门短评url     每本书只爬取第一页的热门短评
			String hotCommentUrl = "https://book.douban.com/subject/" + allBookIdList.get(i) + "/comments/hot?p=1";
			commentQueue.offer(hotCommentUrl);
		}
			
			//创建线程池 爬取书的详细信息
			final ExecutorService bookThreadPool = Executors.newFixedThreadPool(3);
			while(!bookQueue.isEmpty()){
				bookThreadPool.execute(new Runnable(){
					public void run(){
						try {
							sclawByAPIJson(bookQueue.poll());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
			bookThreadPool.shutdown();
			
			//线程池  热门短评url
			final ExecutorService commentThreadPool = Executors.newFixedThreadPool(3);
			while(!commentQueue.isEmpty()){
				commentThreadPool.execute(new Runnable(){
					public void run(){
						try {
							sclawByDetail(commentQueue.poll());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
			commentThreadPool.shutdown();	
	}

	/**
	 * 根据标签页去爬取书的id
	 * 
	 * @param url https://book.douban.com/tag/%E5%8E%86%E5%8F%B2?start=0&type=T
	 * @throws IOException
	 */
	public static void sclawByIndex(String url) throws IOException {
		Document document = Jsoup.parse(HttpClientTool.get(url));
		Elements elements = document.select("#subject_list > ul > li");

		// 将书的id存放在数组中，然后插入数据库
		List<Integer> bookIdList = new ArrayList<Integer>();
		System.out.println(elements.size());
		for (int i = 1; i <= elements.size(); i++) {
			int bookId = Integer.parseInt(elements.select("li:nth-child(" + i + ") > div.info > h2 > a")
								.attr("href").toString()
								.replace("https://book.douban.com/subject/", "")
								.replace("/", ""));
			bookIdList.add(bookId);
		}
		for (int i = 0; i < bookIdList.size(); i++) {
			booksMapper.insertBooksId(bookIdList.get(i));
			session.commit();
		}
	}

	/**
	 * 根据url去api中爬数据，解析其中xml文档
	 * 
	 * @param url
	 *            http://api.douban.com/book/subject/4123377
	 * 
	 *            问：为什么sclawByIndex()方法是用jsoup解析，而sclawByDetail()采用解析xml方式爬取数据
	 *            答： 路径=>
	 *            https://book.douban.com/tag/%E5%8E%86%E5%8F%B2?start=0&type=T
	 *            页面比较规范 ，可以用jsoup来解析爬取书的id和名字 但是 具体到某个书的页面 如：
	 *            https://book.douban
	 *            .com/subject/26953606/，页面之间存在差异，用jsoup解析数据容易出错
	 * 
	 *            路径=> http://api.douban.com/book/subject/4123377
	 *            为豆瓣的开发者接口，数据格式规范，所以采用解析xml方法爬取数据，而且IP不易被封。
	 *            但是2019-03-22验证时，此api借口被豆瓣封掉，具体开放时间不详。
	 *            可参考https://www.douban.com/group/345245/网址了解具体情况。
	 * 
	 *            对于封掉API借口问题，可以爬取另一个页面 https://douban.uieee.com/v2/book/4123377
	 *            返回json数据，可以自己解析
	 * 
	 */
	public static void sclawByAPI(String url) throws IOException,
			JDOMException, DocumentException {
		String xml = HttpClientTool.get(url)
				.replace("This XML file does not appear to have any style information associated with it. The document tree is shown below.","");
		SAXReader saxReader = new SAXReader();
		org.dom4j.Document document = saxReader.read(new ByteArrayInputStream(
				xml.getBytes("UTF-8")));
		Book book = new Book();

		// 得到根节点
		Element root = document.getRootElement();

		// 书的id
		int bookId = Integer.parseInt(root.element("id").getText()
				.replace("http://api.douban.com/book/subject/", ""));
		book.setId(bookId);

		// 图书封面的url
		List<Element> list = root.elements("link");
		for (Element element : list) {
			if (element.attributeValue("rel").equals("image")) {
				String link = element.attributeValue("href");
				book.setImgUrl(link);
				break;
			}
		}

		// 内容简介
		try {
			Element child = root.element("summary");
			book.setBriefIntroduction(child.getText());
		} catch (Exception e) {

		}

		List<Element> list2 = root.elements("attribute");
		for (Element element : list2) {
			// 书的ISBM13
			if (element.attributeValue("name").equals("isbn13")) {
				book.setIsbn(element.getText());
			}

			// 页数
			if (element.attributeValue("name").equals("pages")) {
				try {
					book.setPages(Integer.parseInt(element.getText()
							.replace("页", "").replace("？", "0").trim()));
				} catch (Exception e) {

				}
			}

			// 书名
			if (element.attributeValue("name").equals("title")) {
				book.setBookName(element.getText());
			}

			// 作者
			if (element.attributeValue("name").equals("author")) {
				book.setAuthor(element.getText());
			}

			// 译者
			if (element.attributeValue("name").equals("translator")) {
				book.setTranslator(element.getText());
			}

			// 原价
			if (element.attributeValue("name").equals("price")) {
				book.setOriginalPrice(element.getText());
			}

			// 出版社
			if (element.attributeValue("name").equals("publisher")) {
				book.setPublishingHouse(element.getText());
			}

			// 出版时间
			if (element.attributeValue("name").equals("pubdate")) {
				book.setYearOfPublication(element.getText());
			}

			// 作者介绍
			if (element.attributeValue("name").equals("author-intro")) {
				book.setAuthorIntroduction(element.getText());
			}
		}

		// 图书标签
		List<Element> list3 = root.elements("tag");
		StringBuilder builder = new StringBuilder();
		for (Element element : list3) {
			builder.append(element.attributeValue("name") + "/");
		}
		book.setLabel(builder.toString());

		// 图书标签
		List<Element> list4 = root.elements("rating");
		for (Element element : list4) {
			if (!element.attributeValue("average").isEmpty()) {
				book.setScore(Float.parseFloat(element
						.attributeValue("average")));
			}
			book.setNumberOfPeople(Integer.parseInt(element
					.attributeValue("numRaters")));
		}

		mapper.updateByPrimaryKey(book);
		session.commit();

	}

	/**
	 * 解决豆瓣API被封问题 爬取豆瓣另一个网址
	 * 
	 * @param url
	 *            https://douban.uieee.com/v2/book/4123377 url
	 *            https://douban.uieee.com/v2/book/4123377 返回json数据
	 * @throws IOException
	 * 
	 */
	public static void sclawByAPIJson(String url) throws IOException {
		String doc = HttpClientTool.get(url);
		JSONObject jsonObject = new JSONObject(doc);

		// 图书id
		int bookID = jsonObject.getInt("id");
		System.out.println("---" + bookID);

		// 图书的名字
		String booksName = jsonObject.getString("title");

		// 作者
		String author = "";
		JSONArray authorList = jsonObject.getJSONArray("author");
		if (authorList.length() > 0) {
			for (int i = 0; i < authorList.length(); i++) {
				author = author + authorList.get(i) + ",";
			}
			author = author.substring(0, author.length() - 1);
		}

		// 出版日期
		String pubdate = jsonObject.getString("pubdate");

		// 标签
		String tagCountName = "";
		JSONArray tagList = jsonObject.getJSONArray("tags");
		for (int i = 0; i < tagList.length(); i++) {
			JSONObject tagObject = tagList.getJSONObject(i);
			// int tagCount = tagObject.getInt("count");
			tagCountName = tagCountName + tagObject.getString("name") + ",";
		}
		tagCountName = tagCountName.substring(0, tagCountName.length() - 1);

		// 原书名
		String originTitle = jsonObject.getString("origin_title");

		// 翻译
		String translator = "";
		JSONArray translatorList = jsonObject.getJSONArray("translator");
		if (translatorList.length() > 0) {
			for (int i = 0; i < translatorList.length(); i++) {
				translator = translator + translatorList.getString(i) + ",";
			}
			translator = translator.substring(0, translator.length() - 1);
		}

		// 目录
		String catalog = jsonObject.getString("catalog");
		if (catalog.length() > 3000) {
			catalog = catalog.substring(0, 2999);
		}

		// 图书页数
		int pages = 0;
		if (jsonObject.get("pages").toString().length() > 0) {
			pages = jsonObject.getInt("pages");
		}

		// 图书封面图片url 分为三种图片 大 中 小
		JSONObject imageUrlObject = jsonObject.getJSONObject("images");
		String largeImageUrl = imageUrlObject.getString("large");
		String mediumImageUrl = imageUrlObject.getString("medium");
		String smallImageUrl = imageUrlObject.getString("small");

		// 图书详细信息url
		String bookDetailUrl = jsonObject.getString("alt");

		// 出版单位
		String publisher = jsonObject.getString("publisher");

		// ISBN10
		String isbn10 = jsonObject.getString("isbn10");

		// ISBN13
		String isbn13 = jsonObject.getString("isbn13");

		// 书名
		String title = jsonObject.getString("title");

		// 作者简介
		String authorIntro = jsonObject.getString("author_intro");
		if (authorIntro.length() > 3000) {
			authorIntro = authorIntro.substring(0, 2999);
		}

		// 简介
		String summary = jsonObject.getString("summary").replace("\n", "")
				.replace(" ", "");
		if (summary.length() > 3000) {
			summary = summary.substring(0, 2999);
		}

		// 价格
		String price = jsonObject.getString("price");

		Books books = new Books();

		books.setBooksId(bookID);
		books.setBooksName(booksName);
		books.setAuthor(author);
		books.setPubdate(pubdate);
		books.setTags(tagCountName);
		books.setOriginTtitle(originTitle);
		books.setTranslator(translator);
		books.setCatalog(catalog);
		books.setPages(pages);
		books.setImagesSmallUrl(smallImageUrl);
		books.setImagesLargeUrl(largeImageUrl);
		books.setImagesMediumUrl(mediumImageUrl);
		books.setBookUrl(bookDetailUrl);
		books.setPublisher(publisher);
		books.setIsbn10(isbn10);
		books.setIsbn13(isbn13);
		books.setTitle(title);
		books.setAuthorIntro(authorIntro);
		books.setSummary(summary);
		books.setPrice(price);

		booksMapper.updateBooksById(books);

		session.commit();
	}

	/**
	 * 爬取热门评论
	 * 
	 * @param url https://book.douban.com/subject/25862578/comments/hot?p=1
	 * @throws IOException
	 */
	public static void sclawByDetail(String url) throws IOException {
		
		// 爬取评论   只爬取热评的第一页
		Document document = Jsoup.parse(HttpClientTool.get(url));
		//书的id   
		int bookId = Integer.parseInt(url.replace("https://book.douban.com/subject/", "").replace("/comments/hot?p=1", ""));
		//书名
		String bookName = document.select("div#content > h1").get(0).text().replace("短评","").trim();	
		//评论总数
		String tmpTotalComent = document.select("span#total-comments").get(0).text()
				.replace("全部共", "").replace("条", "").trim();
		int totalComment = Integer.parseInt(tmpTotalComent);
		
		//抽取网友信息和评论等
		Elements elements = document.select("div#comments > ul:not(.fold-bd) > li");
		for(int i = 1;i <= elements.size();i++){
			Comments comments = new Comments();
			//用户id
			String tmpUserId = elements.select("li:nth-child(" + i + ") > div.comment > h3 > span.comment-info > a[href]").attr("href");
			String userId = tmpUserId.replace("https://www.douban.com/people/", "").replace("/", "");
			//用户名
			String userName = elements.select("li:nth-child(" + i + ") > div.comment > h3 > span.comment-info > a[href]").text();
			//评分
			String score = elements.select("li:nth-child(" + i + ") > div > h3 > span.comment-info > span.user-stars.allstar50.rating").attr("title");
			if(score.isEmpty()){
				score = elements.select("li:nth-child(" + i + ") > div.comment > h3 > span.comment-info > span.user-stars.allstar40.rating").attr("title");
				if(score.isEmpty()){
					score = elements.select("li:nth-child(" + i + ") > div.comment > h3 > span.comment-info > span.user-stars.allstar30.rating").attr("title");
				}
				if(score.isEmpty()){
					score = elements.select("li:nth-child(" + i + ") > div.comment > h3 > span.comment-info > span.user-stars.allstar20.rating").attr("title");
				}
				if(score.isEmpty()){
					score = elements.select("li:nth-child(" + i + ") > div.comment > h3 > span.comment-info > span.user-stars.allstar10.rating").attr("title");
				}
				if(score.isEmpty()){
					score = elements.select("li:nth-child(" + i + ") > div.comment > h3 > span.comment-info > span.user-stars.allstar0.rating").attr("title");
				}
			}	 
			//评论时间
			String time = elements.select("li:nth-child(" + i + ") > div.comment > h3 > span.comment-info > span:nth-child(3)").text();
			//评论内容
			String comment = elements.select("li:nth-child(" + i + ") > div.comment > p.comment-content > span.short").text();
			//点赞数量
			String tmpVoteCount = elements.select("li:nth-child(" + i + ") > div.comment > h3 > span.comment-vote > span.vote-count").text();
			int voteCount = Integer.parseInt(tmpVoteCount);
			
			comments.setBookId(bookId);
			comments.setBookName(bookName);			
			comments.setTotalComment(totalComment);			
			comments.setUserId(userId);			
			comments.setUserName(userName);			
			comments.setScore(score);
			comments.setTime(time);
			comments.setComment(comment);		
			comments.setVoteCount(voteCount);
			
			commentsMapper.insertComment(comments);
			session.commit();
		}
	}

}