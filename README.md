# book-mybatis-lab

《JAVA框架技术（一）》实验一 —— AI 赋能 MyBatis 与 MyBatis-Plus 环境搭建及 CRUD 实操。

业务载体换成**图书馆藏书管理**，库 `lib_center`、表 `book`，跑通「原生 MyBatis 基础 CRUD → 动态 SQL / 批量操作 → MyBatis-Plus 通用 CRUD 与分页」这条主线。

---

## 1. 目录结构

```
book-mybatis-lab
├── pom.xml                                Maven 依赖（MyBatis-Plus 3.5.5 / MySQL 驱动 / Log4j / JUnit）
├── .gitignore
├── sql
│   └── init.sql                           建库建表 + 初始数据
└── src
    ├── main
    │   ├── java/com/campus/lib
    │   │   ├── entity/Book.java           实体类，字段与 book 表一一对应
    │   │   ├── query/BookQuery.java       多条件检索参数对象
    │   │   ├── mapper/BookMapper.java     持久层接口（含 MP 的 BaseMapper）
    │   │   ├── util/SqlSessionHolder.java 封装 SqlSessionFactory，控制生命周期
    │   │   └── run/ConsoleRunner.java     控制台入口，用来截图
    │   └── resources
    │       ├── mybatis-config.xml         全局配置
    │       ├── jdbc.properties            数据库连接四要素
    │       ├── log4j.properties            SQL 日志输出规则
    │       └── com/campus/lib/mapper/BookMapper.xml   映射文件
    └── test/java/com/campus/lib
        ├── BookCrudTest.java              任务 2：基础 CRUD
        ├── BookDynamicSqlTest.java        任务 3：动态 SQL、批量操作、分支查询
        └── BookPlusCrudTest.java          任务 4：MP 通用 CRUD、条件构造器、分页
```

> 映射文件放在 `resources` 下**同名的包路径**里，`mybatis-config.xml` 里注册时不用一行行写
> `<mapper resource="...">`，直接 `<package name="com.campus.lib.mapper"/>` 就能把接口和 XML 一起扫进去。

## 2. 数据表

| 字段 | 类型 | 含义 |
| --- | --- | --- |
| id | INT PK AUTO_INCREMENT | 图书编号 |
| book_name | VARCHAR(80) NOT NULL | 书名（建了普通索引） |
| author | VARCHAR(40) | 作者 |
| publisher | VARCHAR(60) | 出版社 |
| category | VARCHAR(30) | 分类（建了普通索引） |
| price | DECIMAL(8,2) | 定价 |
| stock | INT | 库存 |
| publish_date | DATE | 出版日期 |
| state | TINYINT | 1 在架 / 0 下架 |

下划线列名靠 `mapUnderscoreToCamelCase=true` 映射到驼峰属性，同时 `resultMap` 里也显式写了一遍，出问题好定位。

## 3. 环境要求

| 组件 | 版本 |
| --- | --- |
| JDK | 8 或 11 |
| Maven | 3.8+ （建议配阿里云镜像） |
| MySQL | 8.0 |
| IntelliJ IDEA | 2022.x 及以上 |

## 4. 快速跑通

```bash
# 1. 建库建表
mysql -u root -p < sql/init.sql

# 2. 改成本机数据库口令
#    src/main/resources/jdbc.properties

# 3. 编译 + 跑测试
mvn test

# 4. 跑控制台入口看输出
mvn -q compile exec:java -Dexec.mainClass=com.campus.lib.run.ConsoleRunner
```

在 IDEA 里则是直接右键 `ConsoleRunner` → Run，以及在 `src/test/java` 下的测试类里点方法左边的绿色三角。

## 5. 三个任务分别对应哪些代码

| 任务 | 代码位置 |
| --- | --- |
| 任务 2 基础 CRUD | `BookMapper` 的 selectAll / findById / addBook / updateWhole / deleteOne + `BookMapper.xml` 对应节点 + `BookCrudTest` |
| 任务 3 动态 SQL 与批量 | `selectByCondition`（if + where）、`updateSelective`（set）、`deleteBatch` / `insertBatch`（foreach）、`selectByBranch`（choose）+ `BookDynamicSqlTest` |
| 任务 4 MyBatis-Plus | `BookMapper extends BaseMapper<Book>`、`QueryWrapper` / `LambdaQueryWrapper`、`selectPage` 分页 + `BookPlusCrudTest` |

## 6. 几个刻意的设计选择

**自定义方法名避开了通用方法名。** 继承 `BaseMapper` 后会注入 `selectById` / `insert` / `updateById` / `deleteById` 等方法，如果 XML 里再写一个同名的 `selectById`，statement id 就撞了，启动直接报
`Mapped Statements collection already contains value for ...`。所以自定义方法统一用 `findById`、`addBook`、`deleteOne` 这类名字。

**排序字段用 choose 白名单，不用 `${}`。** `ORDER BY ${orderColumn}` 是把前端传来的字符串直接拼进 SQL，传个 `id; drop table book` 就出事了。这里改成只认 `price` 和 `stock` 两个值，其它一律走默认排序。

**分页插件用代码注册，不写在 XML 里。** `MybatisPlusInterceptor` 写在 `mybatis-config.xml` 的 `<plugins>` 里跟版本绑定得比较紧，换个版本就失效；在 `SqlSessionHolder` 里等配置构建完再挂上去更稳。拦截器链是在每次 `openSession` 创建 Executor 时才应用的，构建之后再加同样生效。

**MyBatis-Plus 用 3.5.5。** 3.5.3.1 的 `GenericTypeUtils` 默认调 `org.springframework.core.GenericTypeResolver`，不引 `spring-core` 的话一启动就是 `NoClassDefFoundError`；3.5.5 已经改回不依赖 Spring 的写法。

## 7. 常见报错

| 现象 | 原因 | 处理 |
| --- | --- | --- |
| `Invalid bound statement (not found)` | XML 没被扫到：namespace 写错、XML 不在 `resources` 的同名包路径下 | 核对 namespace 与接口全限定名，XML 放到 `src/main/resources/com/campus/lib/mapper/` |
| `Parameter 'ids' not found` | 多参数方法没加 `@Param` | 接口方法参数补 `@Param("ids")` |
| 查询出来字段是 null | 驼峰映射没开，或 `resultMap` 列名写错 | 检查 `mapUnderscoreToCamelCase` 与 column |
| `Mapped Statements collection already contains value for ...` | 自定义方法与 `BaseMapper` 通用方法重名 | 自定义方法改名，或删掉重名的 XML 节点 |
| 分页返回全量数据 | 分页插件没注册 | 检查 `SqlSessionHolder` 里的 `addInterceptor` |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | 用了旧坐标 `mysql-connector-java` | 换成 `com.mysql:mysql-connector-j` |
| 中文乱码 | 连接串缺 `characterEncoding` | URL 加 `characterEncoding=utf8` |
