-- ============================================================
-- 《JAVA框架技术（一）》实验一  图书馆藏书管理 —— 建库脚本
-- 库名：lib_center   主表：book
-- 执行方式：Navicat / IDEA Database 控制台直接跑，或者 mysql -u root -p < init.sql
-- ============================================================

DROP DATABASE IF EXISTS lib_center;
CREATE DATABASE lib_center DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE lib_center;

DROP TABLE IF EXISTS book;
CREATE TABLE book (
    id           INT           PRIMARY KEY AUTO_INCREMENT COMMENT '图书编号',
    book_name    VARCHAR(80)   NOT NULL                COMMENT '书名',
    author       VARCHAR(40)                           COMMENT '作者',
    publisher    VARCHAR(60)                           COMMENT '出版社',
    category     VARCHAR(30)                           COMMENT '分类',
    price        DECIMAL(8,2)  DEFAULT 0.00            COMMENT '定价',
    stock        INT           DEFAULT 0               COMMENT '库存',
    publish_date DATE                                  COMMENT '出版日期',
    state        TINYINT       DEFAULT 1               COMMENT '状态 1在架 0下架'
);

-- 实验三要求给检索字段建索引，这里先建上，后面查条件的时候能看出差别
CREATE INDEX idx_book_name ON book (book_name);
CREATE INDEX idx_category  ON book (category);

INSERT INTO book (book_name, author, publisher, category, price, stock, publish_date, state) VALUES
('深入理解计算机系统',   'Randal E. Bryant', '机械工业出版社',           '计算机', 139.00, 12, '2016-11-01', 1),
('Java 并发编程实战',    'Brian Goetz',      '机械工业出版社',           '计算机',  79.00,  6, '2012-02-01', 1),
('算法导论',             'Thomas H. Cormen', '机械工业出版社',           '计算机', 128.00,  7, '2012-12-01', 1),
('代码整洁之道',         'Robert C. Martin', '人民邮电出版社',           '计算机',  59.00,  0, '2010-01-01', 1),
('活着',                 '余华',             '作家出版社',               '文学',    45.00, 30, '2012-08-01', 1),
('沉默的大多数',         '王小波',           '北京十月文艺出版社',       '文学',    39.00, 18, '2017-04-01', 1),
('人类简史',             '尤瓦尔·赫拉利',    '中信出版社',               '历史',    68.00,  5, '2014-11-01', 1),
('万历十五年',           '黄仁宇',           '生活·读书·新知三联书店',   '历史',    52.00,  9, '2006-08-01', 1),
('数学分析新讲（第一册）','张筑生',           '北京大学出版社',           '数学',    55.00,  4, '1990-10-01', 0),
('经济学原理',           '曼昆',             '北京大学出版社',           '经济',    88.00, 11, '2015-05-01', 1);

SELECT COUNT(*) AS 初始化记录数 FROM book;
