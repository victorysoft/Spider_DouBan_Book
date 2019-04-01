package com.itkim.pojo;

public class Comments {
	
	int bookId;
	
	String bookName;
	
	int totalComment;
	
	String userId;
	
	String userName;
	
	String score;
	
	String time;
	
	String comment;
	
	int voteCount;

	public int getBookId() {
		return bookId;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public int getTotalComment() {
		return totalComment;
	}

	public void setTotalComment(int totalComment) {
		this.totalComment = totalComment;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}


	public int getVoteCount() {
		return voteCount;
	}

	public void setVoteCount(int voteCount) {
		this.voteCount = voteCount;
	}

	@Override
	public String toString() {
		return "Comments [bookId=" + bookId + ", bookName=" + bookName
				+ ", totalComment=" + totalComment + ", userId=" + userId
				+ ", userName=" + userName + ", score=" + score + ", time="
				+ time + ", comment=" + comment + ", voteCount=" + voteCount
				+ "]";
	}
	 
	
}
