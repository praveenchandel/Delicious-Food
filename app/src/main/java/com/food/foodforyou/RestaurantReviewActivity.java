package com.food.foodforyou;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.food.foodforyou.Model.offers;
import com.food.foodforyou.Model.products;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.text.DecimalFormat;
import java.util.HashMap;

public class RestaurantReviewActivity extends AppCompatActivity {

    private String restaurantName="";

    private DatabaseReference restaurantsRef;
    private FirebaseAuth mAuth;
    private String userID;

    private LinearLayout userReviewLayout;
    private TextView reviewDate,reviewDetails;
    private RatingBar reviewRatingBar;
    private TextView editRatingButton;
    private TextView deleteRatingButton;

    private ProgressDialog loadingBar;

    // for all ratings
    private TextView overAllRatingsText;
    private RatingBar overAllRatingsRatingBar;

    private ProgressBar fiveStarProgressBar;
    private ProgressBar fourStarProgressBar;
    private ProgressBar threeStarProgressBar;
    private ProgressBar twoStarProgressBar;
    private ProgressBar oneStarProgressBar;

    private TextView fiveTotalReviewsTextView;
    private TextView fourTotalReviewsTextView;
    private TextView threeTotalReviewsTextView;
    private TextView twoTotalReviewsTextView;
    private TextView oneTotalReviewsTextView;

    private TextView totalRatingsTextView;

    private Button rateItButton;
    private Button seeAllRatingsButton;

    private int totalRatings=0;
    private int givenRatings=0;

    private ImageView back_arrow_icon;
    private ImageView cart_icon;
    private TextView toolbar_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_review);

        restaurantName=getIntent().getStringExtra("restaurant_name");

         back_arrow_icon=(ImageView)findViewById(R.id.custom_toolbar_back_arrow);
        cart_icon=(ImageView)findViewById(R.id.custom_toolbar_cart_icon);
        toolbar_name=(TextView)findViewById(R.id.custom_toolbar_toolbar_name);

        back_arrow_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cart_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),CartViewActivity.class);
                startActivity(intent);
            }
        });

        toolbar_name.setText("Restaurants Reviews");

        restaurantsRef = FirebaseDatabase.getInstance().getReference().child("Restaurants").child("JODHPUR");

        initializingViews();

        mAuth=FirebaseAuth.getInstance();
        userID=mAuth.getCurrentUser().getUid();

        accessRestaurantRatings();

        rateItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),RatingActivity.class);
                intent.putExtra("Pid",restaurantName);
                intent.putExtra("Work","giveRating");
                intent.putExtra("type","Restaurants");
                startActivity(intent);
            }
        });

        editRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),RatingActivity.class);
                intent.putExtra("Pid",restaurantName);
                intent.putExtra("Work","editRating");
                intent.putExtra("type","Restaurants");
                startActivity(intent);
               }
        });

        seeAllRatingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),AllProductReviewActivity.class);
                intent.putExtra("Pid",restaurantName);
                intent.putExtra("type","Restaurants");
                if (totalRatings>0){
                    intent.putExtra("hasRatings","YES");
                }else {
                    intent.putExtra("hasRatings","NO");
                }
                startActivity(intent);
            }
        });

        deleteRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnectionAvailable(RestaurantReviewActivity.this)) {

                    new LovelyStandardDialog(RestaurantReviewActivity.this)
                            .setTopColorRes(R.color.colorPrimary)
                            .setIcon(R.drawable.ic_baseline_warning_24)
                            .setTitle("Are you sure ")
                            .setMessage("Your review will be permanently deleted.")
                            .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    // deleting the review
                                    DeleteUserRating();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();

                } else {
                    new LovelyStandardDialog(RestaurantReviewActivity.this)
                            .setTopColorRes(R.color.colorPrimary)
                            .setIcon(R.drawable.ic_baseline_signal_cellular_connected_no_internet_4_bar_24)
                            .setTitle("No Internet Connection")
                            .setMessage("It seems like you don't have any internet connection.")
                            .setPositiveButton(android.R.string.ok, null)
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
            }
        });
    }

    private void DeleteUserRating(){

        loadingBar.setTitle("Deleting your review");
        loadingBar.setMessage("Please wait while we are deleting your review");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        restaurantsRef.child(restaurantName).child("Review").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int deletedRatingTargetIs=1;

                    final int one=Integer.parseInt(snapshot.child("1star").getValue().toString());
                    final int two=Integer.parseInt(snapshot.child("2star").getValue().toString());
                    final int three=Integer.parseInt(snapshot.child("3star").getValue().toString());
                    final int four=Integer.parseInt(snapshot.child("4star").getValue().toString());
                    final int five=Integer.parseInt(snapshot.child("5star").getValue().toString());

                    final HashMap<String,Object> deleteRatingMap=new HashMap<>();

                    if (givenRatings==1){
                        deletedRatingTargetIs=Integer.parseInt(snapshot.child("1star").getValue().toString());
                        deletedRatingTargetIs=deletedRatingTargetIs-1;
                        deleteRatingMap.put("1star",String.valueOf(deletedRatingTargetIs));

                    }else if (givenRatings==2){
                        deletedRatingTargetIs=Integer.parseInt(snapshot.child("2star").getValue().toString());
                        deletedRatingTargetIs=deletedRatingTargetIs-1;
                        deleteRatingMap.put("2star",String.valueOf(deletedRatingTargetIs));

                    }else if (givenRatings==3){
                        deletedRatingTargetIs=Integer.parseInt(snapshot.child("3star").getValue().toString());
                        deletedRatingTargetIs=deletedRatingTargetIs-1;
                        deleteRatingMap.put("3star",String.valueOf(deletedRatingTargetIs));

                    }else if (givenRatings==4){
                        deletedRatingTargetIs=Integer.parseInt(snapshot.child("4star").getValue().toString());
                        deletedRatingTargetIs=deletedRatingTargetIs-1;
                        deleteRatingMap.put("4star",String.valueOf(deletedRatingTargetIs));

                    }else if (givenRatings==5){
                        deletedRatingTargetIs=Integer.parseInt(snapshot.child("5star").getValue().toString());
                        deletedRatingTargetIs=deletedRatingTargetIs-1;
                        deleteRatingMap.put("5star",String.valueOf(deletedRatingTargetIs));
                    }

                    // managing the ratings
                    restaurantsRef.child(restaurantName).child("Review").updateChildren(deleteRatingMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        // deleting the user review
                                        restaurantsRef.child(restaurantName).child("Review").child("Reviews").child(userID)
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){

                                                    final HashMap<String,Object> MainRatingMap=new HashMap<>();

                                                    MainRatingMap.put("total_reviews",String.valueOf(one+two+three+four+five-1));
                                                    MainRatingMap.put("total_ratings",String.valueOf(one+two*2+three*3+four*4+five*5-givenRatings));

                                                    restaurantsRef.child(restaurantName).updateChildren(MainRatingMap)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){

                                                                        loadingBar.dismiss();
                                                                        Intent intent=new Intent(getApplicationContext(),RestaurantReviewActivity.class);
                                                                        intent.putExtra("restaurant_name", restaurantName);
                                                                        startActivity(intent);
                                                                        finish();
                                                                        Toast.makeText(RestaurantReviewActivity.this, "Review deleted successfully...", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                }else {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(RestaurantReviewActivity.this, "some error occurred try again later...", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }else {
                                        Toast.makeText(RestaurantReviewActivity.this, "some error occurred try again later...", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    private void accessRestaurantRatings(){

        restaurantsRef.child(restaurantName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Review").exists()){

                    // showing all reviews
                        int one=Integer.parseInt(snapshot.child("Review").child("1star").getValue().toString());
                        int two=Integer.parseInt(snapshot.child("Review").child("2star").getValue().toString());
                        int three=Integer.parseInt(snapshot.child("Review").child("3star").getValue().toString());
                        int four=Integer.parseInt(snapshot.child("Review").child("4star").getValue().toString());
                        int five=Integer.parseInt(snapshot.child("Review").child("5star").getValue().toString());

                        int total=one+two+three+four+five;
                        totalRatings=total;

                        // to get fro rid of divide by zero exception
                        if (total>0) {

                            totalRatingsTextView.setText(String.valueOf(total) + " Reviews");

                            oneTotalReviewsTextView.setText(String.valueOf(one));
                            twoTotalReviewsTextView.setText(String.valueOf(two));
                            threeTotalReviewsTextView.setText(String.valueOf(three));
                            fourTotalReviewsTextView.setText(String.valueOf(four));
                            fiveTotalReviewsTextView.setText(String.valueOf(five));

                            // for average ratings
                            two = two * 2;
                            three = three * 3;
                            four = four * 4;
                            five = five * 5;

                            DecimalFormat df = new DecimalFormat("#.#");

                            double fullRating = (double) total;

                            double overAll = (one + two + three + four + five) / fullRating;
                            overAll = Double.parseDouble(df.format(overAll));
                            overAllRatingsText.setText(String.valueOf(overAll));
                            overAllRatingsRatingBar.setRating((float) overAll);

                            one = one * 100;
                            one = one / total;
                            oneStarProgressBar.setProgress(Math.max(one, 5));

                            two = two * 100;
                            two = two / total;
                            twoStarProgressBar.setProgress(Math.max(two, 5));

                            three = three * 100;
                            three = three / total;
                            threeStarProgressBar.setProgress(Math.max(three, 5));

                            four = four * 100;
                            four = four / total;
                            fourStarProgressBar.setProgress(Math.max(four, 5));

                            five = five * 100;
                            five = five / total;
                            fiveStarProgressBar.setProgress(Math.max(five, 5));

                        }

                    if (snapshot.child("Review").child("Reviews").child(userID).exists()){
                        rateItButton.setVisibility(View.GONE);
                        userReviewLayout.setVisibility(View.VISIBLE);
                        reviewDate.setText(snapshot.child("Review").child("Reviews").child(userID).child("Date").getValue().toString());
                        reviewDetails.setText(snapshot.child("Review").child("Reviews").child(userID).child("Review").getValue().toString());

                        int stars=Integer.parseInt(snapshot.child("Review").child("Reviews").child(userID).child("Stars").getValue().toString());
                        givenRatings=stars;

                        reviewRatingBar.setRating((float) stars);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializingViews(){

        // for user rating
        userReviewLayout=(LinearLayout)findViewById(R.id.restaurant_details_user_review_layout);
        reviewDate=(TextView)findViewById(R.id.restaurant_details_review_date);
        reviewDetails=(TextView)findViewById(R.id.restaurant_details_user_review);
        reviewRatingBar=(RatingBar)findViewById(R.id.restaurant_user_ratingBar);
        editRatingButton=(TextView)findViewById(R.id.restaurant_details_review_edit_button);
        deleteRatingButton=(TextView)findViewById(R.id.restaurant_details_review_delete_button);

        loadingBar=new ProgressDialog(this);

        // for all ratings

        overAllRatingsText=(TextView)findViewById(R.id.restaurant_details_over_all_ratings);
        overAllRatingsRatingBar=(RatingBar)findViewById(R.id.restaurant_ratingBar);

        fiveStarProgressBar=(ProgressBar)findViewById(R.id.restaurant_details_five_star_progressBar);
        fourStarProgressBar=(ProgressBar)findViewById(R.id.restaurant_details_four_star_progressBar);
        threeStarProgressBar=(ProgressBar)findViewById(R.id.restaurant_details_three_star_progressBar);
        twoStarProgressBar=(ProgressBar)findViewById(R.id.restaurant_details_two_star_progressBar);
        oneStarProgressBar=(ProgressBar)findViewById(R.id.restaurant_details_one_star_progressBar);

        fiveTotalReviewsTextView=(TextView) findViewById(R.id.restaurant_details_five_star_ratings);
        fourTotalReviewsTextView=(TextView) findViewById(R.id.restaurant_details_four_star_ratings);
        threeTotalReviewsTextView=(TextView) findViewById(R.id.restaurant_details_three_star_ratings);
        twoTotalReviewsTextView=(TextView) findViewById(R.id.restaurant_details_two_star_ratings);
        oneTotalReviewsTextView=(TextView) findViewById(R.id.restaurant_details_one_star_ratings);

        totalRatingsTextView=(TextView) findViewById(R.id.restaurant_details_total_ratings);

        rateItButton=(Button)findViewById(R.id.restaurant_details_give_product_rating);
        seeAllRatingsButton=(Button)findViewById(R.id.restaurant_details_see_all_ratings_button);
    }
}