package com.campus.lib;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lib.entity.Book;
import com.campus.lib.mapper.BookMapper;
import com.campus.lib.util.SqlSessionHolder;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

/**
 * 任务 4：MyBatis-Plus 通用 CRUD、条件构造器与分页。
 * <p>
 * 这些方法一行 SQL 都没写，全在 BaseMapper 里，接口继承过来就能直接调。
 */
public class BookPlusCrudTest {

    @Test
    public void plusSelectByIdAndCount() {
        try (SqlSession session = SqlSessionHolder.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);

            Book book = mapper.selectById(1);
            Assert.assertNotNull(book);
            System.out.println("通用方法查到的记录：" + book);

            Long total = mapper.selectCount(null);
            Assert.assertTrue(total >= 10L);
            System.out.println("表内总记录数 = " + total);
        }
    }

    @Test
    public void plusQueryWrapperWithConditionFlags() {
        try (SqlSession session = SqlSessionHolder.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);

            String category = "计算机";
            String keyword = null;          // 故意留空，看条件是怎么被跳过的
            BigDecimal minPrice = new BigDecimal("60");

            QueryWrapper<Book> wrapper = new QueryWrapper<>();
            wrapper.like(keyword != null && !keyword.isEmpty(), "book_name", keyword)
                    .eq(category != null && !category.isEmpty(), "category", category)
                    .ge(minPrice != null, "price", minPrice)
                    .orderByDesc("price");

            List<Book> books = mapper.selectList(wrapper);
            Assert.assertFalse(books.isEmpty());
            books.forEach(System.out::println);
        }
    }

    /** 字段名写成硬编码字符串容易拼错，Lambda 写法能在编译期就挡住 */
    @Test
    public void plusLambdaWrapper() {
        try (SqlSession session = SqlSessionHolder.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);

            LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<Book>()
                    .eq(Book::getState, 1)
                    .like(Book::getCategory, "文")
                    .orderByAsc(Book::getPrice);

            List<Book> books = mapper.selectList(wrapper);
            Assert.assertFalse(books.isEmpty());
            books.forEach(System.out::println);
        }
    }

    @Test
    public void plusPaginationWorks() {
        try (SqlSession session = SqlSessionHolder.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);

            LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<Book>()
                    .orderByAsc(Book::getId);

            Page<Book> page1 = new Page<>(1, 4);
            IPage<Book> result1 = mapper.selectPage(page1, wrapper);

            Assert.assertEquals(4, result1.getRecords().size());
            Assert.assertTrue(result1.getTotal() >= 10);
            Assert.assertTrue(result1.getPages() >= 3);

            // 分页真正生效的标志：第二页的编号和第一页完全不重叠
            Page<Book> page2 = new Page<>(2, 4);
            IPage<Book> result2 = mapper.selectPage(page2, wrapper);
            Assert.assertNotEquals(result1.getRecords().get(0).getId(),
                    result2.getRecords().get(0).getId());

            System.out.println("总记录数 = " + result1.getTotal() + "，总页数 = " + result1.getPages());
            System.out.println("--- 第 1 页 ---");
            result1.getRecords().forEach(System.out::println);
            System.out.println("--- 第 2 页 ---");
            result2.getRecords().forEach(System.out::println);
        }
    }

    /** 页码超出范围时返回空列表，而不是把全表倒出来 */
    @Test
    public void plusPaginationOverflowIsEmpty() {
        try (SqlSession session = SqlSessionHolder.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);

            Page<Book> farPage = new Page<>(999, 4);
            IPage<Book> result = mapper.selectPage(farPage, new LambdaQueryWrapper<Book>());

            Assert.assertTrue(result.getRecords().isEmpty());
        }
    }

    @Test
    public void plusUpdateByIdOnlyChangesGivenColumns() {
        try (SqlSession session = SqlSessionHolder.openSession(false)) {
            BookMapper mapper = session.getMapper(BookMapper.class);

            Book before = mapper.selectById(6);
            Book patch = new Book();
            patch.setId(6);
            patch.setPublisher("三联书店（修订版）");

            Assert.assertEquals(1, mapper.updateById(patch));

            Book after = mapper.selectById(6);
            Assert.assertEquals("三联书店（修订版）", after.getPublisher());
            Assert.assertEquals(before.getBookName(), after.getBookName());

            session.rollback();
        }
    }

    @Test
    public void plusSaveAndRemoveById() {
        try (SqlSession session = SqlSessionHolder.openSession(false)) {
            BookMapper mapper = session.getMapper(BookMapper.class);

            Book book = new Book("深度工作", "Cal Newport", "江西人民出版社",
                    "经济", new BigDecimal("45.00"), 6, java.time.LocalDate.of(2017, 9, 1), 1);

            Assert.assertEquals(1, mapper.insert(book));
            Assert.assertNotNull(book.getId());

            Assert.assertEquals(1, mapper.deleteById(book.getId()));
            Assert.assertNull(mapper.selectById(book.getId()));

            session.rollback();
        }
    }
}
