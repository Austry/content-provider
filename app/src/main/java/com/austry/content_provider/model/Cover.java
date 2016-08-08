package com.austry.content_provider.model;


import java.io.Serializable;

public class Cover implements Serializable {

    private static final long serialVersionUID = 432342L;

    private long id;
    private String small;
    private String big;

    public Cover() {
    }

    public Cover(String small, String big) {
        this.small = small;
        this.big = big;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSmall() {
        return small;
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public String getBig() {
        return big;
    }

    public void setBig(String big) {
        this.big = big;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cover)) return false;

        Cover cover = (Cover) o;

        if (small != null ? !small.equals(cover.small) : cover.small != null) return false;
        return big != null ? big.equals(cover.big) : cover.big == null;

    }

    @Override
    public int hashCode() {
        int result = small != null ? small.hashCode() : 0;
        result = 31 * result + (big != null ? big.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Cover{" +
                "id=" + id +
                ", small='" + small + '\'' +
                ", big='" + big + '\'' +
                '}';
    }
}
