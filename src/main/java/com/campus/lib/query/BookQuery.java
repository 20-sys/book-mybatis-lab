package com.campus.lib.query;

import java.math.BigDecimal;

/**
 * 图书检索条件。字段全用包装类型，因为"没填"和"填了 0"是两回事，
 * 用 int 接的话没填就是 0，动态 SQL 的 &lt;if test="state != null"&gt; 永远为真。
 */
public class BookQuery {

    /** 关键词，同时匹配书名和作者 */
    private String keyword;

    private String category;

    private Integer state;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    /** 排序字段，只接受 price / stock 两个值，其它一律走默认排序 */
    private String orderColumn;

    private boolean desc = true;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public BigDecimal getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice) {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getOrderColumn() {
        return orderColumn;
    }

    public void setOrderColumn(String orderColumn) {
        this.orderColumn = orderColumn;
    }

    public boolean isDesc() {
        return desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "BookQuery{keyword=" + keyword + ", category=" + category + ", state=" + state
                + ", 价格区间=[" + minPrice + ", " + maxPrice + "], orderColumn=" + orderColumn + "}";
    }
}
