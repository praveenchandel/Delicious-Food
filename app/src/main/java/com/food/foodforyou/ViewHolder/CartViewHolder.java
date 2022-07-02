package com.food.foodforyou.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.food.foodforyou.Interface.itemClickListener;
import com.food.foodforyou.R;

public class CartViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtProductName, txtProductPrice, txtProductQuantity,txtProductSellerName,txtItemStatus;
    public itemClickListener itemClickListner;

    public CartViewHolder(@NonNull View itemView) {
        super(itemView);

        txtProductName = (TextView) itemView.findViewById(R.id.cart_product_name);
        txtProductPrice = (TextView) itemView.findViewById(R.id.cart_product_price);
        txtProductQuantity = (TextView) itemView.findViewById(R.id.cart_product_quantity);
        txtProductSellerName = (TextView) itemView.findViewById(R.id.cart_product_seller);
        txtItemStatus=(TextView)itemView.findViewById(R.id.cart_product_item_status);

    }

    public void setItemClickListner(itemClickListener itemClickListner) {
        this.itemClickListner = itemClickListner;
    }

    @Override
    public void onClick(View v) {
        itemClickListner.onClick(v, getAdapterPosition(), false);
    }
}
