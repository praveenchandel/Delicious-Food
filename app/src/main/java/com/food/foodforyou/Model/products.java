package com.food.foodforyou.Model;

public class products {

    private String productId,productImage,restaurantName,productPrice,productName,description,category,consumerType,half_price,selected_type,status;

    public products() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public products(String productId, String productImage, String restaurantName, String productPrice, String productName, String description, String category, String consumerType, String half_price, String selected_type,String status) {
        this.productId = productId;
        this.productImage = productImage;
        this.restaurantName = restaurantName;
        this.productPrice = productPrice;
        this.productName = productName;
        this.description = description;
        this.category = category;
        this.consumerType = consumerType;
        this.half_price = half_price;
        this.selected_type = selected_type;
        this.status=status;
    }



    public String getSelected_type() {
        return selected_type;
    }

    public void setSelected_type(String selected_type) {
        this.selected_type = selected_type;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getHalf_price() {
        return half_price;
    }

    public void setHalf_price(String half_price) {
        this.half_price = half_price;
    }
}
