package com.itkim.mapper;

import com.itkim.pojo.Book;

import java.util.List;

public interface BookMapper {
    
    int deleteByPrimaryKey(Integer id);

    int insert(Book record);

    int insertSelective(Book record);

    Book selectByPrimaryKey(Integer id);
    
    List<Integer> getAllBookId();

    int updateByPrimaryKeySelective(Book record);

    int updateByPrimaryKey(Book record);
}