package com.food.foodforyou.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.food.foodforyou.Model.HomeRestaurants;
import com.food.foodforyou.Model.products;
import com.food.foodforyou.Model.restaurants;
import com.food.foodforyou.OffersWithMinimumOff;
import com.food.foodforyou.R;
import com.food.foodforyou.RestaurantItemsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class RestaurantAdapter extends ArrayAdapter<HomeRestaurants> {

    private Activity con;

    public RestaurantAdapter(Activity context, ArrayList<HomeRestaurants> Products){
        super(context,0,Products);
        con=context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null)
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.restaurant_layout, parent, false);

        final HomeRestaurants currentRestaurant = getItem(position);

        DatabaseReference restaurantsRef;
        restaurantsRef = FirebaseDatabase.getInstance().getReference().child("Restaurants").child("JODHPUR");

        TextView textRestaurantName,textAboutRestaurant;
        ImageView imageView;
        LinearLayout offersLinearLayout;
        ImageView offerOneImage;
        ImageView offerTwoImage;
        ImageView offerThreeImage;
        ImageView offerFourImage;
        CardView restaurantCardView;
        final TextView restaurantTotalRatings;
        final RatingBar restaurantRatingBar;

        imageView=(ImageView)listItemView.findViewById(R.id.restaurant_image_view);
        textRestaurantName=(TextView) listItemView.findViewById(R.id.restaurant_name_text);
        textAboutRestaurant=(TextView)listItemView.findViewById(R.id.restaurant_about_text);

        restaurantCardView=(CardView)listItemView.findViewById(R.id.restaurant_details_card_view);
        offersLinearLayout=(LinearLayout)listItemView.findViewById(R.id.offers_view);

        offerOneImage=(ImageView)listItemView.findViewById(R.id.offer_one_image);
        offerTwoImage=(ImageView)listItemView.findViewById(R.id.offer_two_image);
        offerThreeImage=(ImageView)listItemView.findViewById(R.id.offer_three_image);
        offerFourImage=(ImageView)listItemView.findViewById(R.id.offer_four_image);

        restaurantTotalRatings=(TextView)listItemView.findViewById(R.id.restaurants_total_ratings);

        textRestaurantName.setText(currentRestaurant.rest.getRestaurant_name());
        textAboutRestaurant.setText(currentRestaurant.rest.getRestaurant_description());
        Picasso.get().load(currentRestaurant.rest.getRestaurant_image()).placeholder(R.drawable.food_for_you).into(imageView);

        final String[] total_ratings = {""};
        final String[] over_all_ratings = {""};

        // accessing restaurants ratings
//        restaurantsRef.child(currentRestaurant.getRestaurant_name()).child("Review")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                        if (snapshot.exists()){
                            int one=Integer.parseInt(currentRestaurant.getOne_star());
                            int two=Integer.parseInt(currentRestaurant.getTow_star());
                            int three=Integer.parseInt(currentRestaurant.getThree_star());
                            int four=Integer.parseInt(currentRestaurant.getFive_star());
                            int five=Integer.parseInt(currentRestaurant.getFive_star());

                            int total=one+two+three+four+five;

                            if (total>0){

                            // for average ratings
                            two=two*2;
                            three=three*3;
                            four=four*4;
                            five=five*5;

                            DecimalFormat df = new DecimalFormat("#.#");

                            double fullRating=(double) total;

                            double overAll=(one+two+three+four+five)/fullRating;
                            overAll=Double.parseDouble(df.format(overAll));
                            restaurantTotalRatings.setVisibility(View.VISIBLE);
                            //restaurantRatingBar.setVisibility(View.VISIBLE);
                            restaurantTotalRatings.setText(String.valueOf(total));
                            //restaurantRatingBar.setRating((float) overAll);

                            total_ratings[0] =String.valueOf(total);
                            over_all_ratings[0] =String.valueOf(overAll);
                        }else {
                            total_ratings[0] ="0";
                            over_all_ratings[0] ="0";
                        }
                 //   }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });

        // handling offers

//        if(false){
//
//            offersLinearLayout.setVisibility(View.VISIBLE);
//
//            offerOneImage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent=new Intent(con, OffersWithMinimumOff.class);
//                    intent.putExtra("minimum_off","offers");
//                    con.startActivity(intent);
//                }
//            });
//
//            offerTwoImage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent=new Intent(con,OffersWithMinimumOff.class);
//                    intent.putExtra("minimum_off","5");
//                    con.startActivity(intent);
//                }
//            });
//
//            offerThreeImage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent=new Intent(con,OffersWithMinimumOff.class);
//                    intent.putExtra("minimum_off","10");
//                    con.startActivity(intent);
//                }
//            });
//
//            offerFourImage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent=new Intent(con,OffersWithMinimumOff.class);
//                    intent.putExtra("minimum_off","15");
//                    con.startActivity(intent);
//                }
//            });
//        }
//
//        restaurantCardView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(con, RestaurantItemsActivity.class);
//                intent.putExtra("restaurant_name", currentRestaurant.rest.getRestaurant_name());
//                intent.putExtra("restaurant_description",currentRestaurant.rest.getRestaurant_description());
//                intent.putExtra("restaurants_total_ratings",total_ratings[0]);
//                intent.putExtra("restaurants_over_all_ratings",over_all_ratings[0]);
//                con.startActivity(intent);
//            }
//        });

        return listItemView;
    }
}