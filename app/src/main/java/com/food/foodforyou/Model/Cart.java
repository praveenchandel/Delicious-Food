package com.food.foodforyou.Model;

public class Cart {

    private String ProductId,Quantity,selected_type;

    public Cart() {
    }

    public Cart(String productId, String quantity, String selected_type) {
        ProductId = productId;
        Quantity = quantity;
        this.selected_type = selected_type;
    }

    public String getSelected_type() {
        return selected_type;
    }

    public void setSelected_type(String selected_type) {
        this.selected_type = selected_type;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }
}
