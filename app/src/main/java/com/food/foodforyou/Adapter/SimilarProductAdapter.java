package com.food.foodforyou.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.food.foodforyou.Model.products;
import com.food.foodforyou.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SimilarProductAdapter extends ArrayAdapter<products> {

    public SimilarProductAdapter(Activity context, ArrayList<products> Products){
        super(context,0,Products);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null)
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.product_item, parent, false);

        final products currentProduct = getItem(position);

        TextView productName=(TextView)listItemView.findViewById(R.id.product_name);
        TextView restaurantName=(TextView)listItemView.findViewById(R.id.product_description);
        TextView productPrice=(TextView)listItemView.findViewById(R.id.product_price);
        ImageView productImage=(ImageView) listItemView.findViewById(R.id.product_image);

        productName.setText(currentProduct.getProductName());
        restaurantName.setText(currentProduct.getRestaurantName());
        productPrice.setText(currentProduct.getProductPrice());

        Picasso.get().load(currentProduct.getProductImage()).placeholder(R.drawable.food_for_you).into(productImage);

        return listItemView;
    }
}