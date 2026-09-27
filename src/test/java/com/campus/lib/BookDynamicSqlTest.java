package com.campus.lib;

import com.campus.lib.entity.Book;
import com.campus.lib.mapper.BookMapper;
import com.campus.lib.query.BookQuery;
import com.campus.lib.util.SqlSessionHolder;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 任务 3：动态 SQL、多条件查询与批量操作。
 */
public class BookDynamicSqlTest {

    @Test
    public void conditionWithKeywordOnly() {
        try (SqlSession session = SqlSessionHolder.openSession()) {
            BookQuery query = new BookQuery();
            // 关键词会同时打在 book_name 和 author 上，初始数据里只有
            // 《深入理解计算机系统》命中"计算"两个字
            query.setKeyword("计算");

            List<Book> books = session.getMapper(BookMapper.class).selectByCondition(query);
            Assert.assertEquals(1, books.size());
            Assert.assertTrue(books.get(0).getBookName().contains("计算"));
            books.forEach(System.out::println);
        }
    }

    /** 条件全空时 where 标签不生成任何子句，等价于查全部，这是最容易踩的坑之一 */
    @Test
    public void conditionWithNothingShouldNotBlowUp() {
        try (SqlSession session = SqlSessionHolder.openSession()) {
            List<Book> books = session.getMapper(BookMapper.class).selectByCondition(new BookQuery());
            Assert.assertTrue(books.size() >= 10);
        }
    }

    @Test
    public void conditionWithPriceRangeAndOrder() {
        try (SqlSession session = SqlSessionHolder.openSession()) {
            BookQuery query = new BookQuery();
            query.setMinPrice(new BigDecimal("40"));
            query.setMaxPrice(new BigDecimal("100"));
            query.setOrderColumn("price");
            query.setDesc(false);

            List<Book> books = session.getMapper(BookMapper.class).selectByCondition(query);
            Assert.assertFalse(books.isEmpty());

            // 升序排好之后，后一条的价格不应该比前一条小
            for (int i = 1; i < books.size(); i++) {
                Assert.assertTrue(books.get(i).getPrice().compareTo(books.get(i - 1).getPrice()) >= 0);
            }
        }
    }

    /** 白名单之外的排序字段必须被忽略，这里用注入语句试探一下 */
    @Test
    public void orderColumnOutsideWhitelistFallsBack() {
        try (SqlSession session = SqlSessionHolder.openSession()) {
            BookQuery query = new BookQuery();
            query.setOrderColumn("id; drop table book --");

            List<Book> books = session.getMapper(BookMapper.class).selectByCondition(query);
            Assert.assertFalse(books.isEmpty());
        }
    }

    @Test
    public void updateSelectiveTouchesOnlyGivenFields() {
        try (SqlSession session = SqlSessionHolder.openSession(false)) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            Book before = mapper.findById(5);

            Book patch = new Book();
            patch.setId(5);
            patch.setStock(99);

            Assert.assertEquals(1, mapper.updateSelective(patch));

            Book after = mapper.findById(5);
            Assert.assertEquals(Integer.valueOf(99), after.getStock());
            // 没传的字段必须原样保留，这正是 set 标签相对整行更新的价值
            Assert.assertEquals(before.getBookName(), after.getBookName());
            Assert.assertEquals(0, before.getPrice().compareTo(after.getPrice()));

            session.rollback();
        }
    }

    @Test
    public void insertBatchAndDeleteBatch() {
        try (SqlSession session = SqlSessionHolder.openSession(false)) {
            BookMapper mapper = session.getMapper(BookMapper.class);

            List<Book> batch = new ArrayList<>();
            batch.add(new Book("批量测试-A", "张三", "测试出版社", "测试",
                    new BigDecimal("10.00"), 1, LocalDate.of(2020, 1, 1), 1));
            batch.add(new Book("批量测试-B", "李四", "测试出版社", "测试",
                    new BigDecimal("20.00"), 2, LocalDate.of(2020, 2, 2), 1));
            batch.add(new Book("批量测试-C", "王五", "测试出版社", "测试",
                    new BigDecimal("30.00"), 3, LocalDate.of(2020, 3, 3), 0));

            Assert.assertEquals(3, mapper.insertBatch(batch));

            // 多行 VALUES 的批量插入不做主键回填：不同驱动 getGeneratedKeys 的行为不一致，
            // 想拿主键就按书名把它们查回来，这样跨数据库都稳
            BookQuery probe = new BookQuery();
            probe.setKeyword("批量测试");
            List<Book> inserted = mapper.selectByCondition(probe);
            Assert.assertEquals(3, inserted.size());

            List<Integer> ids = Arrays.asList(inserted.get(0).getId(), inserted.get(1).getId());
            Assert.assertEquals(2, mapper.deleteBatch(ids));
            Assert.assertEquals(1, mapper.selectByCondition(probe).size());

            session.rollback();
        }
    }

    /** choose 只会命中第一个成立的分支：给了书名就不再管分类 */
    @Test
    public void branchPrefersBookNameOverCategory() {
        try (SqlSession session = SqlSessionHolder.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);

            BookQuery byName = new BookQuery();
            byName.setKeyword("活着");
            byName.setCategory("计算机");
            List<Book> hitName = mapper.selectByBranch(byName);
            Assert.assertEquals(1, hitName.size());
            Assert.assertEquals("活着", hitName.get(0).getBookName());

            BookQuery byCategory = new BookQuery();
            byCategory.setCategory("历史");
            List<Book> hitCategory = mapper.selectByBranch(byCategory);
            Assert.assertTrue(hitCategory.size() >= 2);

            // 两个条件都不给，进 otherwise 分支：只看在架的书
            List<Book> fallback = mapper.selectByBranch(new BookQuery());
            Assert.assertFalse(fallback.isEmpty());
            fallback.forEach(b -> Assert.assertEquals(Integer.valueOf(1), b.getState()));
        }
    }

    @Test
    public void countOnShelfMatchesQuery() {
        try (SqlSession session = SqlSessionHolder.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);

            BookQuery query = new BookQuery();
            query.setState(1);

            Assert.assertEquals(mapper.selectByCondition(query).size(), mapper.countOnShelf());
        }
    }
}
