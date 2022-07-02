package com.food.foodforyou.Model;

public class offers {
    private String min_quantity,max_off,percentage_off,status;
    private String productName,product_price,product_quantity;

    public offers() {
    }

    public offers(offers value) {
        this.min_quantity = value.getMin_quantity();
        this.max_off = value.getMax_off();
        this.percentage_off = value.getPercentage_off();
        this.status = value.getStatus();
    }

    public String getProduct_quantity() {
        return product_quantity;
    }

    public void setProduct_quantity(String product_quantity) {
        this.product_quantity = product_quantity;
    }

    public String getProduct_price() {
        return product_price;
    }

    public void setProduct_price(String product_price) {
        this.product_price = product_price;
    }

    public String getMin_quantity() {
        return min_quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }


    public void setMin_quantity(String min_quantity) {
        this.min_quantity = min_quantity;
    }

    public String getMax_off() {
        return max_off;
    }

    public void setMax_off(String max_off) {
        this.max_off = max_off;
    }

    public String getPercentage_off() {
        return percentage_off;
    }

    public void setPercentage_off(String percentage_off) {
        this.percentage_off = percentage_off;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
