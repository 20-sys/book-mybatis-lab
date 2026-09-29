package com.campus.lib.run;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.lib.entity.Book;
import com.campus.lib.mapper.BookMapper;
import com.campus.lib.query.BookQuery;
import com.campus.lib.util.SqlSessionHolder;
import org.apache.ibatis.session.SqlSession;

import java.math.BigDecimal;
import java.util.List;

/**
 * 直接右键 Run 这个类的 main 方法，用来截图实验报告的"运行结果"。
 * 想单独看某一节的效果，把不需要的段落注释掉即可。
 */
public class ConsoleRunner {

    public static void main(String[] args) {
        printAll();
        printOne();
        printByCondition();
        printOnShelfCount();
        printPageByPlus();
    }

    /** 任务 2：最朴素的查询，验证配置链路是否打通 */
    private static void printAll() {
        System.out.println("\n===== 1. 全部藏书 =====");
        try (SqlSession session = SqlSessionHolder.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);
            List<Book> books = mapper.selectAll();
            books.forEach(System.out::println);
        }
    }

    /** 任务 2：按主键查单条 */
    private static void printOne() {
        System.out.println("\n===== 2. 按编号查 =====");
        try (SqlSession session = SqlSessionHolder.openSession()) {
            Book book = session.getMapper(BookMapper.class).findById(3);
            System.out.println(book == null ? "没有编号为 3 的图书" : book);
        }
    }

    /** 任务 3：动态 SQL 多条件 */
    private static void printByCondition() {
        System.out.println("\n===== 3. 多条件检索（计算机类、价格 60 以上、按价格倒序）=====");
        BookQuery query = new BookQuery();
        query.setCategory("计算机");
        query.setMinPrice(new BigDecimal("60"));
        query.setOrderColumn("price");
        query.setDesc(true);

        try (SqlSession session = SqlSessionHolder.openSession()) {
            session.getMapper(BookMapper.class).selectByCondition(query).forEach(System.out::println);
        }
    }

    private static void printOnShelfCount() {
        System.out.println("\n===== 4. 在架图书数量 =====");
        try (SqlSession session = SqlSessionHolder.openSession()) {
            System.out.println("在架共 " + session.getMapper(BookMapper.class).countOnShelf() + " 种");
        }
    }

    /** 任务 4：MyBatis-Plus 条件构造器 + 分页插件 */
    private static void printPageByPlus() {
        System.out.println("\n===== 5. MyBatis-Plus 分页（每页 4 条，第 1 页）=====");
        try (SqlSession session = SqlSessionHolder.openSession()) {
            BookMapper mapper = session.getMapper(BookMapper.class);

            LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Book::getState, 1)
                    .ge(Book::getPrice, new BigDecimal("40"))
                    .orderByDesc(Book::getPrice);

            IPage<Book> page = mapper.selectPage(new Page<>(1, 4), wrapper);
            System.out.println("总记录数 = " + page.getTotal() + "，总页数 = " + page.getPages());
            page.getRecords().forEach(System.out::println);
        }
    }
}
