package com.pro.bookpro.model;

import java.io.Serializable;
import java.util.List;

public class BookObject implements Serializable {

    private long id;
    private String name;
    private String description;
    private int price;
    private int sale;
    private String image;
    private String banner;
    private boolean popular;
    private int type;

    private int amount;

    private int sellNumber;

    private List<Image> images;

    public BookObject() {
    }

    public BookObject(long id, String name, String description, int price, int sale,
                      String image, String banner, boolean popular, int type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.sale = sale;
        this.image = image;
        this.banner = banner;
        this.popular = popular;
        this.type = type;
    }

    public int getSellNumber() {
        return sellNumber;
    }

    public void setSellNumber(int sellNumber) {
        this.sellNumber = sellNumber;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getSale() {
        return sale;
    }

    public void setSale(int sale) {
        this.sale = sale;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public boolean isPopular() {
        return popular;
    }

    public void setPopular(boolean popular) {
        this.popular = popular;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
