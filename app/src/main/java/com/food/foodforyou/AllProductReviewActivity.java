package com.food.foodforyou;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.food.foodforyou.Model.Rating;
import com.food.foodforyou.Model.restaurants;
import com.food.foodforyou.ViewHolder.RatingViewHolder;
import com.food.foodforyou.ViewHolder.restaurantViewHolder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class AllProductReviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private TextView nothingFoundTxt;
    private ImageView nothingFoundImage;

    private DatabaseReference ReviewsRef;
    private String productId="";
    private String type="";
    private String has_ratings="";

    private ImageView back_arrow_icon;
    private ImageView cart_icon;
    private TextView toolbar_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_product_review);

        productId=getIntent().getStringExtra("Pid");
        type=getIntent().getStringExtra("type");
        has_ratings=getIntent().getStringExtra("hasRatings");

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

        toolbar_name.setText("All Reviews");

        recyclerView = findViewById(R.id.all_product_review_activity_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        nothingFoundTxt=(TextView)findViewById(R.id.all_reviews_activity_nothing_found_text);
        nothingFoundImage=(ImageView)findViewById(R.id.all_reviews_activity_nothing_found_image);

        if (type.equals("Restaurants")){
            ReviewsRef = FirebaseDatabase.getInstance().getReference().child("Restaurants").child("JODHPUR").child(productId).child("Review").child("Reviews");
        }else {
            ReviewsRef = FirebaseDatabase.getInstance().getReference().child("Products").child("JODHPUR").child(productId).child("Review").child("Reviews");
        }

        if (has_ratings.equals("YES")){
            accessAllReviews();
        }else {
            // showing nothing found
            nothingFoundImage.setVisibility(View.VISIBLE);
            nothingFoundTxt.setVisibility(View.VISIBLE);
        }
    }

    private void accessAllReviews() {

        FirebaseRecyclerOptions<Rating> options =
                new FirebaseRecyclerOptions.Builder<Rating>()
                        .setQuery(ReviewsRef,Rating.class)
                        .build();


        FirebaseRecyclerAdapter<Rating, RatingViewHolder> adapter =
                new FirebaseRecyclerAdapter<Rating, RatingViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull RatingViewHolder holder, int position, @NonNull final Rating model)
                    {
                        holder.txtUserName.setText(model.getUserName());
                        holder.txtRatingDate.setText(model.getDate());
                        holder.txtReview.setText(model.getReview());

                        holder.ratingBar.setRating(Float.parseFloat(model.getStars()));
                    }

                    @NonNull
                    @Override
                    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_review_layout, parent, false);
                        RatingViewHolder holder = new RatingViewHolder(view);
                        return holder;
                    }
                };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }
}