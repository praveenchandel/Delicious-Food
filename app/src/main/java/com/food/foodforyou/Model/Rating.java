package com.food.foodforyou.Model;

public class Rating {

    private String userName,Stars,Review,Date;

    public Rating(String userName, String stars, String review, String date) {
        this.userName = userName;
        Stars = stars;
        Review = review;
        Date = date;
    }

    public Rating() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStars() {
        return Stars;
    }

    public void setStars(String stars) {
        Stars = stars;
    }

    public String getReview() {
        return Review;
    }

    public void setReview(String review) {
        Review = review;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
}
