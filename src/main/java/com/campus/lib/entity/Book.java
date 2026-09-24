package com.campus.lib.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Book {

    private Integer id;
    private String bookName;
    private String author;
    private String publisher;
    private String category;
    private BigDecimal price;
    private Integer stock;
    private LocalDate publishDate;
    private Integer state;

    public Book() {
    }

    /** 新增时用得比较多的构造方式，省得一行行 set */
    public Book(String bookName, String author, String publisher, String category,
                BigDecimal price, Integer stock, LocalDate publishDate, Integer state) {
        this.bookName = bookName;
        this.author = author;
        this.publisher = publisher;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.publishDate = publishDate;
        this.state = state;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return String.format("Book{id=%s, 书名=%s, 作者=%s, 分类=%s, 定价=%s, 库存=%s, 出版=%s, 状态=%s}",
                id, bookName, author, category, price, stock, publishDate, state == null ? "-" : (state == 1 ? "在架" : "下架"));
    }
}
