package com.campus.lib.mapper;

import com.campus.lib.entity.Book;

import java.util.List;

/**
 * 藏书持久层。方法只声明，实现由 MyBatis 动态代理生成。
 */
public interface BookMapper {

    /** 查全部 */
    List<Book> selectAll();

    /** 按主键查一条 */
    Book findById(Integer id);

    /** 新增，回填自增主键 */
    int addBook(Book book);

    /** 整行覆盖式更新（没赋值的字段会被写成 null） */
    int updateWhole(Book book);

    /** 删一条 */
    int deleteOne(Integer id);
}
