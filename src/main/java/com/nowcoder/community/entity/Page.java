package com.nowcoder.community.entity;

// 分页
public class Page {
    // 当前页码
    private int current=1;
    // 一页显示条数
    private int limit = 10;
    // 数据总数(用于计算总的页数)
    private int rows;
    // 查询路径(用于复用分页链接)
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getOffset() {
        return (current - 1) * limit;
    }

    public int getTotal() {
        return rows / limit + (rows % limit == 0 ? 0 : 1);
    }

    public int getFrom() {
        int from = current - 2;
        return Math.max(from, 1);
    }

    public int getTo() {
        int to = current + 2;
        return Math.min(getTotal(), to);
    }
}
