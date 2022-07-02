package com.food.foodforyou.Model;

public class HomeRestaurants {
    public restaurants rest;

    public String one_star,tow_star,three_star,four_star,five_star;

    public HomeRestaurants() {
    }

    public HomeRestaurants(restaurants rest) {
        this.rest = rest;
    }

    public HomeRestaurants(restaurants rest, String one_star, String tow_star, String three_star, String four_star, String five_star) {
        this.rest = rest;
        this.one_star = one_star;
        this.tow_star = tow_star;
        this.three_star = three_star;
        this.four_star = four_star;
        this.five_star = five_star;
    }

    public HomeRestaurants(String one_star, String tow_star, String three_star, String four_star, String five_star) {
        this.one_star = one_star;
        this.tow_star = tow_star;
        this.three_star = three_star;
        this.four_star = four_star;
        this.five_star = five_star;
    }

    public restaurants getRest() {
        return rest;
    }

    public void setRest(restaurants rest) {
        this.rest = rest;
    }

    public String getOne_star() {
        return one_star;
    }

    public void setOne_star(String one_star) {
        this.one_star = one_star;
    }

    public String getTow_star() {
        return tow_star;
    }

    public void setTow_star(String tow_star) {
        this.tow_star = tow_star;
    }

    public String getThree_star() {
        return three_star;
    }

    public void setThree_star(String three_star) {
        this.three_star = three_star;
    }

    public String getFour_star() {
        return four_star;
    }

    public void setFour_star(String four_star) {
        this.four_star = four_star;
    }

    public String getFive_star() {
        return five_star;
    }

    public void setFive_star(String five_star) {
        this.five_star = five_star;
    }
}
