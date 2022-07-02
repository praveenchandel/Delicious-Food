package com.food.foodforyou.Model;

public class orderProductDetails {

    public products userCart;
    private String quantity;

    public orderProductDetails(products userCart, String quantity) {
        this.userCart = userCart;
        this.quantity = quantity;
    }

    public String getQuantity() {
        return quantity;
    }
}
