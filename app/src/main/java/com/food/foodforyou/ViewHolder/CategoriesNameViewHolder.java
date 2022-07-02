package com.food.foodforyou.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.food.foodforyou.Interface.itemClickListener;
import com.food.foodforyou.R;

public class CategoriesNameViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView category_name;
    public itemClickListener itemClickListner;

    public CategoriesNameViewHolder(@NonNull View itemView) {
        super(itemView);

        category_name = (TextView) itemView.findViewById(R.id.shop_by_category_category_name);
    }

    public void setItemClickListner(itemClickListener itemClickListner) {
        this.itemClickListner = itemClickListner;
    }

    @Override
    public void onClick(View v) {
        itemClickListner.onClick(v, getAdapterPosition(), false);
    }
}
