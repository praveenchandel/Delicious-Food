package com.food.foodforyou.Model;

public class isHasToShowCategory {
    public products product;
    private int hasTo;

    public isHasToShowCategory() {
    }

    public isHasToShowCategory(products product, int hasTo) {
        this.product = product;
        this.hasTo = hasTo;
    }

    public products getProduct() {
        return product;
    }

    public void setProduct(products product) {
        this.product = product;
    }

    public int getHasTo() {
        return hasTo;
    }

    public void setHasTo(int hasTo) {
        this.hasTo = hasTo;
    }
}
