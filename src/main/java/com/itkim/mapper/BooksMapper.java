package com.itkim.mapper;

import java.util.List;

import com.itkim.pojo.Books;

public interface BooksMapper {
	
	int insertBooksId(Integer booksId);
	
	int updateBooksById(Books books);
	
    List<Integer>  getAllBookId();
}
