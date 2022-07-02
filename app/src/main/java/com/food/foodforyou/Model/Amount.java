package com.food.foodforyou.Model;

public class Amount {
    int totalPrice,totalDiscount,totalRestaurants,totalDeliveryCharge;

    public Amount() {
    }

    public Amount(int totalPrice, int totalDiscount, int totalRestaurants, int totalDeliveryCharge) {
        this.totalPrice = totalPrice;
        this.totalDiscount = totalDiscount;
        this.totalRestaurants = totalRestaurants;
        this.totalDeliveryCharge = totalDeliveryCharge;
    }

    public int getTotalDeliveryCharge() {
        return totalDeliveryCharge;
    }

    public void setTotalDeliveryCharge(int totalDeliveryCharge) {
        this.totalDeliveryCharge = totalDeliveryCharge;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(int totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public int getTotalRestaurants() {
        return totalRestaurants;
    }

    public void setTotalRestaurants(int totalRestaurants) {
        this.totalRestaurants = totalRestaurants;
    }
}
