package com.campus.lib.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

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
            return new SqlSessionFactoryBuilder().build(in);
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
