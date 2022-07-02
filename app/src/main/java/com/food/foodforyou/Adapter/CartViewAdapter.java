package com.food.foodforyou.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.food.foodforyou.Model.orderProductDetails;
import com.food.foodforyou.R;

import java.util.ArrayList;

public class CartViewAdapter extends ArrayAdapter<orderProductDetails> {


    public CartViewAdapter(Activity context, ArrayList<orderProductDetails> ProductDetails){
        super(context,0,ProductDetails);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null)
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.cart_layout, parent, false);

        final orderProductDetails currentProduct = getItem(position);

        TextView txtProductName, txtProductPrice, txtProductQuantity,txtProductSellerName,txtItemStatus;

        txtProductName = (TextView) listItemView.findViewById(R.id.cart_product_name);
        txtProductPrice = (TextView) listItemView.findViewById(R.id.cart_product_price);
        txtProductQuantity = (TextView) listItemView.findViewById(R.id.cart_product_quantity);
        txtProductSellerName = (TextView) listItemView.findViewById(R.id.cart_product_seller);
        txtItemStatus=(TextView)listItemView.findViewById(R.id.cart_product_item_status);

        int oneProductFullPrice=0;

        txtProductName.setText(currentProduct.userCart.getProductName());
        txtItemStatus.setText(currentProduct.userCart.getStatus());

        if (currentProduct.userCart.getSelected_type().equals("Half")){
           // mProduct.setSelected_type(ds.child("selected_type").getValue().toString());
            oneProductFullPrice=(Integer.parseInt(currentProduct.userCart.getHalf_price())*Integer.parseInt(currentProduct.getQuantity()));
            txtProductPrice.setText("₹ " + currentProduct.userCart.getHalf_price() + " half");
           // mProductOffers1.setProduct_price(mProduct.getHalf_price());
           // mProductOffers2.setProduct_price(mProduct.getHalf_price());
        }else {
            oneProductFullPrice=(Integer.parseInt(currentProduct.userCart.getProductPrice())*Integer.parseInt(currentProduct.getQuantity()));
            txtProductPrice.setText("₹ " + currentProduct.userCart.getProductPrice());
            //mProductOffers1.setProduct_price(mProduct.getProductPrice());
            //mProductOffers2.setProduct_price(mProduct.getProductPrice());
        }

        txtProductSellerName.setText(currentProduct.userCart.getRestaurantName());

        txtProductQuantity.setText("x" + currentProduct.getQuantity() + "\n ₹ " + oneProductFullPrice);

        return listItemView;
    }
}