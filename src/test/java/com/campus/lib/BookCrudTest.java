package com.campus.lib;

import com.campus.lib.entity.Book;
import com.campus.lib.mapper.BookMapper;
import com.campus.lib.util.SqlSessionHolder;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 任务 2：基础 CRUD 用例。
 * <p>
 * 所有写操作都跑在 openSession(false) 上，断言完 rollback()，
 * 这样反复跑测试也不会把 init.sql 的初始数据搞得乱七八糟，
 * 顺手把"事务提交与回滚"也验证了一遍。
 */
public class BookCrudTest {

    @Test
    public void selectAllShouldReturnSeedData() {
        try (SqlSession session = SqlSessionHolder.openSession()) {
            List<Book> books = session.getMapper(BookMapper.class).selectAll();
            Assert.assertNotNull(books);
            Assert.assertTrue("初始数据至少有 10 条", books.size() >= 10);
            books.forEach(System.out::println);
        }
    }

    @Test
    public void findByIdShouldCarryPublishDate() {
        try (SqlSession session = SqlSessionHolder.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            Book book = mapper.findById(1);

            Assert.assertNotNull(book);
            Assert.assertNotNull(book.getBookName());
            // 这一行专门用来确认驼峰映射生效：publish_date -> publishDate
            Assert.assertNotNull("publishDate 为 null 说明驼峰映射没开", book.getPublishDate());
        }
    }

    @Test
    public void addBookShouldFillGeneratedKey() {
        try (SqlSession session = SqlSessionHolder.openSession(false)) {
            BookMapper mapper = session.getMapper(BookMapper.class);

            Book book = new Book("重构：改善既有代码的设计", "Martin Fowler", "人民邮电出版社",
                    "计算机", new BigDecimal("99.00"), 8, LocalDate.of(2019, 5, 1), 1);

            int rows = mapper.addBook(book);
            Assert.assertEquals(1, rows);
            Assert.assertNotNull("useGeneratedKeys 没配好的话这里就是 null", book.getId());

            Book reloaded = mapper.findById(book.getId());
            Assert.assertEquals("重构：改善既有代码的设计", reloaded.getBookName());

            session.rollback();
        }
    }

    @Test
    public void updateWholeShouldOverwriteAllColumns() {
        try (SqlSession session = SqlSessionHolder.openSession(false)) {
            BookMapper mapper = session.getMapper(BookMapper.class);

            Book book = mapper.findById(2);
            BigDecimal oldPrice = book.getPrice();
            book.setPrice(new BigDecimal("85.00"));

            Assert.assertEquals(1, mapper.updateWhole(book));
            Assert.assertEquals(0, new BigDecimal("85.00").compareTo(mapper.findById(2).getPrice()));

            System.out.println("编号 2 的定价由 " + oldPrice + " 改为 85.00");

            session.rollback();
            Assert.assertEquals(0, oldPrice.compareTo(mapper.findById(2).getPrice()));
        }
    }

    @Test
    public void deleteOneShouldRemoveRow() {
        try (SqlSession session = SqlSessionHolder.openSession(false)) {
            BookMapper mapper = session.getMapper(BookMapper.class);

            Book book = new Book("临时测试图书", "无名氏", "测试出版社",
                    "测试", new BigDecimal("1.00"), 1, LocalDate.now(), 1);
            mapper.addBook(book);

            Assert.assertEquals(1, mapper.deleteOne(book.getId()));
            Assert.assertNull(mapper.findById(book.getId()));

            session.rollback();
        }
    }
}
