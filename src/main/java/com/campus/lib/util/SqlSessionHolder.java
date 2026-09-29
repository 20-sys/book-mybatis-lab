package com.campus.lib.util;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * SqlSessionFactory 是全局唯一、线程安全的，建一次存起来就行；
 * SqlSession 正好相反，线程不安全，每次用完必须 close。
 * 这两句话背下来不亏，实验报告里问生命周期就答这个。
 */
public final class SqlSessionHolder {

    private static volatile SqlSessionFactory factory;

    private SqlSessionHolder() {
    }

    public static SqlSessionFactory factory() {
        if (factory == null) {
            synchronized (SqlSessionHolder.class) {
                if (factory == null) {
                    factory = build();
                }
            }
        }
        return factory;
    }

    private static SqlSessionFactory build() {
        try (InputStream in = Resources.getResourceAsStream("mybatis-config.xml")) {
            // 注意用的是 MP 的 MybatisSqlSessionFactoryBuilder，
            // 换成原来的 SqlSessionFactoryBuilder 虽然也能解析 XML，
            // 但配置会退化成普通 Configuration，BaseMapper 里的通用方法注入不进去。
            SqlSessionFactory sqlSessionFactory = new MybatisSqlSessionFactoryBuilder().build(in);

            /*
             * 分页插件挂在配置上。
             * 也可以写在 mybatis-config.xml 的 <plugins> 里，但那套写法跟 MP 版本绑定得紧，
             * 换个版本就失效，所以这里等配置建好之后再补一行，最稳。
             * 因为拦截器链是在每次 openSession 创建 Executor 时才应用的，
             * 所以构建之后再加进去对后面的 SqlSession 一样生效。
             */
            PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
            pagination.setMaxLimit(200L);   // 防止前端把 size 传成 100000 把库拖垮
            pagination.setOverflow(false);  // 页码越界时返回空列表，而不是自动跳回第一页

            MybatisPlusInterceptor plusInterceptor = new MybatisPlusInterceptor();
            plusInterceptor.addInnerInterceptor(pagination);
            sqlSessionFactory.getConfiguration().addInterceptor(plusInterceptor);

            return sqlSessionFactory;
        } catch (IOException e) {
            throw new IllegalStateException("读不到 mybatis-config.xml，检查 src/main/resources 目录", e);
        }
    }

    /** 自动提交，写单条数据时省去手动 commit */
    public static SqlSession openSession() {
        return factory().openSession(true);
    }

    /** 手动控事务，需要回滚或用例之间互相隔离时用这个 */
    public static SqlSession openSession(boolean autoCommit) {
        return factory().openSession(autoCommit);
    }
}
