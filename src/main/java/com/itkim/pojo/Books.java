package com.itkim.pojo;

public class Books {
	   private Integer booksId;  
	   private String booksName;
	   private String author;
	   private String pubdate;
	   private String tags;
	   private String originTtitle;
	   private String translator;
	   private String catalog;
	   private Integer pages;
	   private String imagesSmallUrl;
	   private String imagesLargeUrl;
	   private String imagesMediumUrl;
	   private String bookUrl;
	   private String publisher;
	   private String isbn10;
	   private String isbn13;
	   private String title;
	   private String authorIntro;
	   private String summary;
	   private String price;
	
	   
	
	public Integer getBooksId() {
		return booksId;
	}
	public void setBooksId(Integer booksId) {
		this.booksId = booksId;
	}
	public String getBooksName() {
		return booksName;
	}
	public void setBooksName(String booksName) {
		this.booksName = booksName;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getPubdate() {
		return pubdate;
	}
	public void setPubdate(String pubdate) {
		this.pubdate = pubdate;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public String getOriginTtitle() {
		return originTtitle;
	}
	public void setOriginTtitle(String originTtitle) {
		this.originTtitle = originTtitle;
	}
	public String getTranslator() {
		return translator;
	}
	public void setTranslator(String translator) {
		this.translator = translator;
	}
	public String getCatalog() {
		return catalog;
	}
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}
	public Integer getPages() {
		return pages;
	}
	public void setPages(Integer pages) {
		this.pages = pages;
	}
	public String getImagesSmallUrl() {
		return imagesSmallUrl;
	}
	public void setImagesSmallUrl(String imagesSmallUrl) {
		this.imagesSmallUrl = imagesSmallUrl;
	}
	public String getImagesLargeUrl() {
		return imagesLargeUrl;
	}
	public void setImagesLargeUrl(String imagesLargeUrl) {
		this.imagesLargeUrl = imagesLargeUrl;
	}
	public String getImagesMediumUrl() {
		return imagesMediumUrl;
	}
	public void setImagesMediumUrl(String imagesMediumUrl) {
		this.imagesMediumUrl = imagesMediumUrl;
	}
	public String getBookUrl() {
		return bookUrl;
	}
	public void setBookUrl(String bookUrl) {
		this.bookUrl = bookUrl;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public String getIsbn10() {
		return isbn10;
	}
	public void setIsbn10(String isbn10) {
		this.isbn10 = isbn10;
	}
	public String getIsbn13() {
		return isbn13;
	}
	public void setIsbn13(String isbn13) {
		this.isbn13 = isbn13;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthorIntro() {
		return authorIntro;
	}
	public void setAuthorIntro(String authorIntro) {
		this.authorIntro = authorIntro;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	@Override
	public String toString() {
		return "Books [booksId=" + booksId + ", booksName=" + booksName
				+ ", author=" + author + ", pubdate=" + pubdate + ", tags="
				+ tags + ", originTtitle=" + originTtitle + ", translator="
				+ translator + ", catalog=" + catalog + ", pages=" + pages
				+ ", imagesSmallUrl=" + imagesSmallUrl + ", imagesLargeUrl="
				+ imagesLargeUrl + ", imagesMediumUrl=" + imagesMediumUrl
				+ ", bookUrl=" + bookUrl + ", publisher=" + publisher
				+ ", isbn10=" + isbn10 + ", isbn13=" + isbn13 + ", title="
				+ title + ", authorIntro=" + authorIntro + ", summary="
				+ summary + ", price=" + price + "]";
	}
	
	   
}
