package com.food.foodforyou.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.food.foodforyou.Interface.itemClickListener;
import com.food.foodforyou.R;

public class restaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView textRestaurantName,textAboutRestaurant,textRecycle;
    public ImageView imageView;
    public LinearLayout offersLinearLayout;
    public ImageView offerOneImage;
    public ImageView offerTwoImage;
    public ImageView offerThreeImage;
    public ImageView offerFourImage;
    public CardView restaurantCardView;
    public TextView restaurantTotalRatings;
    public com.food.foodforyou.Interface.itemClickListener listener;

    public restaurantViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView=(ImageView)itemView.findViewById(R.id.restaurant_image_view);
        textRestaurantName=(TextView) itemView.findViewById(R.id.restaurant_name_text);
        textAboutRestaurant=(TextView)itemView.findViewById(R.id.restaurant_about_text);
        textRecycle=(TextView)itemView.findViewById(R.id.restaurant_currently_not_available);

        restaurantCardView=(CardView)itemView.findViewById(R.id.restaurant_details_card_view);
        offersLinearLayout=(LinearLayout)itemView.findViewById(R.id.offers_view);

        offerOneImage=(ImageView)itemView.findViewById(R.id.offer_one_image);
        offerTwoImage=(ImageView)itemView.findViewById(R.id.offer_two_image);
        offerThreeImage=(ImageView)itemView.findViewById(R.id.offer_three_image);
        offerFourImage=(ImageView)itemView.findViewById(R.id.offer_four_image);

        restaurantTotalRatings=(TextView)itemView.findViewById(R.id.restaurants_total_ratings);
    }

    public void setItemClickListener(itemClickListener listener){
        this.listener=listener;
    }

    @Override
    public void onClick(View v) {
        listener.onClick(v,getAdapterPosition(),false);
    }
}