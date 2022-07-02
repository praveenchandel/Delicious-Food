package com.food.foodforyou.ViewHolder;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.food.foodforyou.Interface.itemClickListener;
import com.food.foodforyou.Model.Rating;
import com.food.foodforyou.R;

public class RatingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtUserName, txtRatingDate, txtReview;
    public RatingBar ratingBar;
    public itemClickListener itemClickListner;

    public RatingViewHolder(@NonNull View itemView) {
        super(itemView);

        txtUserName = (TextView) itemView.findViewById(R.id.review_layout_user_name);
        txtRatingDate = (TextView) itemView.findViewById(R.id.review_layout_review_date);
        txtReview = (TextView) itemView.findViewById(R.id.product_details_user_review);
        ratingBar = (RatingBar) itemView.findViewById(R.id.user_ratingBar);

    }

    public void setItemClickListner(itemClickListener itemClickListner) {
        this.itemClickListner = itemClickListner;
    }

    @Override
    public void onClick(View v) {
        itemClickListner.onClick(v, getAdapterPosition(), false);
    }
}