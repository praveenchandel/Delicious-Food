package com.food.foodforyou.Model;

public class restaurants {

    private String restaurant_name,restaurant_description,restaurant_image,total_ratings,total_reviews;
    private int status;
    public offers offer1;
    public offers offer2;
    private int showFlat;

    public restaurants() {
    }

    public restaurants(String restaurant_name, String restaurant_description, String restaurant_image, String total_ratings, String total_reviews, int status, offers offer1, offers offer2, int showFlat) {
        this.restaurant_name = restaurant_name;
        this.restaurant_description = restaurant_description;
        this.restaurant_image = restaurant_image;
        this.total_ratings = total_ratings;
        this.total_reviews = total_reviews;
        this.status = status;
        this.offer1 = offer1;
        this.offer2 = offer2;
        this.showFlat = showFlat;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRestaurant_name() {
        return restaurant_name;
    }

    public void setRestaurant_name(String restaurant_name) {
        this.restaurant_name = restaurant_name;
    }

    public String getRestaurant_description() {
        return restaurant_description;
    }

    public void setRestaurant_description(String restaurant_description) {
        this.restaurant_description = restaurant_description;
    }

    public String getRestaurant_image() {
        return restaurant_image;
    }

    public void setRestaurant_image(String restaurant_image) {
        this.restaurant_image = restaurant_image;
    }

    public String getTotal_ratings() {
        return total_ratings;
    }

    public void setTotal_ratings(String total_ratings) {
        this.total_ratings = total_ratings;
    }

    public String getTotal_reviews() {
        return total_reviews;
    }

    public void setTotal_reviews(String total_reviews) {
        this.total_reviews = total_reviews;
    }

    public offers getOffer1() {
        return offer1;
    }

    public void setOffer1(offers offer1) {
        this.offer1 = offer1;
    }

    public offers getOffer2() {
        return offer2;
    }

    public void setOffer2(offers offer2) {
        this.offer2 = offer2;
    }

    public int getShowFlat() {
        return showFlat;
    }

    public void setShowFlat(int showFlat) {
        this.showFlat = showFlat;
    }
}
