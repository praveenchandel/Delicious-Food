package com.food.foodforyou.Model;

public class orderDetails {

    private String ProductId,productName,Quantity,productPrice,restaurantName,category,consumerType,status,selected_type;

    public orderDetails() {
    }

    public orderDetails(String productId, String productName, String quantity, String productPrice, String restaurantName, String category, String consumerType, String status, String selected_type) {
        ProductId = productId;
        this.productName = productName;
        Quantity = quantity;
        this.productPrice = productPrice;
        this.restaurantName = restaurantName;
        this.category = category;
        this.consumerType = consumerType;
        this.status = status;
        this.selected_type = selected_type;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getConsumerType() {
        return consumerType;
    }

    public void setConsumerType(String consumerType) {
        this.consumerType = consumerType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSelected_type() {
        return selected_type;
    }

    public void setSelected_type(String selected_type) {
        this.selected_type = selected_type;
    }
}
