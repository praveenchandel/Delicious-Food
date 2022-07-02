package com.food.foodforyou.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.food.foodforyou.Model.isHasToShowCategory;
import com.food.foodforyou.Model.products;
import com.food.foodforyou.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RestaurantItemsAdapter extends ArrayAdapter<isHasToShowCategory> {

    public RestaurantItemsAdapter(Activity context, ArrayList<isHasToShowCategory> Products){
        super(context,0,Products);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null)
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.restaurant_category_wise_items, parent, false);

        final isHasToShowCategory currentProduct = getItem(position);

        TextView available=(TextView)listItemView.findViewById(R.id.category_wise_available);
        available.setText(currentProduct.product.getStatus());

        TextView productName=(TextView)listItemView.findViewById(R.id.category_wise_product_name);
        TextView restaurantName=(TextView)listItemView.findViewById(R.id.category_wise_product_description);
        TextView productPrice=(TextView)listItemView.findViewById(R.id.category_wise_product_price);
        ImageView productImage=(ImageView) listItemView.findViewById(R.id.category_wise_product_image);

        LinearLayout linearLayout=(LinearLayout)listItemView.findViewById(R.id.category_wise_category_name_linear_layout);
        TextView category_name=(TextView)listItemView.findViewById(R.id.category_wise_category_name);

        productName.setText(currentProduct.product.getProductName());
        restaurantName.setText(currentProduct.product.getDescription());
        productPrice.setText(currentProduct.product.getProductPrice());

        Picasso.get().load(currentProduct.product.getProductImage()).placeholder(R.drawable.food_for_you).into(productImage);

        // we have to show the category of the products
            linearLayout.setVisibility(currentProduct.getHasTo());
            category_name.setText(currentProduct.product.getCategory());

        return listItemView;
    }
}