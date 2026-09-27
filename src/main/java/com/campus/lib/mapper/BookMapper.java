package com.campus.lib.mapper;

import com.campus.lib.entity.Book;
import com.campus.lib.query.BookQuery;
import org.apache.ibatis.annotations.Param;

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

    /** 只更新非空字段 */
    int updateSelective(Book book);

    /** 删一条 */
    int deleteOne(Integer id);

    /** 多条件查询：关键词 / 分类 / 状态 / 价格区间 + 指定排序 */
    List<Book> selectByCondition(BookQuery query);

    /** 分支查询：优先书名精确，其次分类，都没有则返回在架图书 */
    List<Book> selectByBranch(BookQuery query);

    /** 批量删除，多参数必须加 @Param 显式起名 */
    int deleteBatch(@Param("ids") List<Integer> ids);

    /** 批量插入 */
    int insertBatch(@Param("list") List<Book> books);

    /** 在架图书数量 */
    int countOnShelf();
}
