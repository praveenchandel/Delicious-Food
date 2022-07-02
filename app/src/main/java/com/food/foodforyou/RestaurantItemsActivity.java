package com.food.foodforyou;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.media.tv.TvContract;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.food.foodforyou.Adapter.RestaurantItemsAdapter;
import com.food.foodforyou.Adapter.SimilarProductAdapter;
import com.food.foodforyou.Model.isHasToShowCategory;
import com.food.foodforyou.Model.products;
import com.food.foodforyou.ViewHolder.ProductViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class RestaurantItemsActivity extends AppCompatActivity {

    private ListView listView;
    private String restaurantName="",restaurant_description="",restaurantImageUrl;

    private TextView noRatingTextView;
    private TextView overAllRating;
    private RatingBar restaurantRatings;
    private TextView totalRatingsTextView;
    private TextView nothingFoundText;
    private ImageView nothingFoundImage;
    private ImageView restaurantImage;

    private TextView restaurantDescription;
    private RelativeLayout reviewRelativeLayout;

    private DatabaseReference restaurantsRef;
    private DatabaseReference productRef;
    private FirebaseAuth mAuth;

     private ProgressBar progressBar;

     private ArrayList<products> allProducts=new ArrayList<products>();
     public static ArrayList<isHasToShowCategory> allProductsOfRestaurant=new ArrayList<isHasToShowCategory>();
     private RestaurantItemsAdapter listVi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_items);

        // this code will change the color of the status bar
        Window window = this.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimaryDarkCollapsing));

        restaurantName=getIntent().getStringExtra("restaurant_name");
        restaurant_description=getIntent().getStringExtra("restaurant_description");
        restaurantImageUrl=getIntent().getStringExtra("restaurant_image");

        initializingViews();

        Picasso.get().load(restaurantImageUrl).into(restaurantImage);

        productRef = FirebaseDatabase.getInstance().getReference().child("Products").child("JODHPUR");
        restaurantsRef=FirebaseDatabase.getInstance().getReference().child("Restaurants").child("JODHPUR");


        ImageView back_arrow_icon = (ImageView) findViewById(R.id.restaurant_items_toolbar_back_arrow);
        ImageView cart_icon = (ImageView) findViewById(R.id.restaurant_items_toolbar_cart_icon);
        TextView toolbar_name = (TextView) findViewById(R.id.restaurant_items_toolbar_toolbar_name);
        ImageView search_image_icon=(ImageView)findViewById(R.id.restaurant_items_toolbar_search_icon);

        back_arrow_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allProductsOfRestaurant.clear();
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

        search_image_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),SearchInsideRestaurant.class);
                startActivity(intent);
            }
        });

        // setting restaurant name is it's size is greater than 20
        int n=restaurantName.length();
        String name="";
        if (n>20){
             name=restaurantName.substring(0,20);
             toolbar_name.setText(name + "...");
        }else {
            toolbar_name.setText(restaurantName);
        }

        restaurantDescription.setText(restaurant_description);

        mAuth= FirebaseAuth.getInstance();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null){
            Intent intent = new Intent(RestaurantItemsActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        if (isConnectionAvailable(RestaurantItemsActivity.this)){
            accessingAllProductOfRestaurant();
            accessingRatingsOfRestaurant();
        }else {
            new LovelyStandardDialog(RestaurantItemsActivity.this)
                    .setTopColorRes(R.color.colorPrimary)
                    .setIcon(R.drawable.ic_baseline_signal_cellular_connected_no_internet_4_bar_24)
                    .setTitle("No Internet Connection")
                    .setMessage("It seems like you don't have any internet connection.")
                    .setPositiveButton(android.R.string.ok,null)
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }

        noRatingTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),RestaurantReviewActivity.class);
                intent.putExtra("restaurant_name",restaurantName);
                startActivity(intent);
            }
        });

        reviewRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),RestaurantReviewActivity.class);
                intent.putExtra("restaurant_name",restaurantName);
                startActivity(intent);
            }
        });
    }


    // accessing restaurants ratings
    private void accessingRatingsOfRestaurant(){

           restaurantsRef.child(restaurantName).child("Review")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if (snapshot.exists()){
                                            int one=Integer.parseInt(snapshot.child("1star").getValue().toString());
                                            int two=Integer.parseInt(snapshot.child("2star").getValue().toString());
                                            int three=Integer.parseInt(snapshot.child("3star").getValue().toString());
                                            int four=Integer.parseInt(snapshot.child("4star").getValue().toString());
                                            int five=Integer.parseInt(snapshot.child("5star").getValue().toString());

                                            int total=one+two+three+four+five;

                                            if (total>0) {

                                                // for average ratings
                                                two = two * 2;
                                                three = three * 3;
                                                four = four * 4;
                                                five = five * 5;

                                                DecimalFormat df = new DecimalFormat("#.#");

                                                double fullRating = (double) total;

                                                double overAll = (one + two + three + four + five) / fullRating;
                                                overAll = Double.parseDouble(df.format(overAll));

                                                reviewRelativeLayout.setVisibility(View.VISIBLE);
                                                totalRatingsTextView.setText("  (" + String.valueOf(total) + " Reviews)");
                                                overAllRating.setText(" " + String.valueOf(overAll));
                                                restaurantRatings.setRating((float) overAll);

                                            }else {
                                                noRatingTextView.setVisibility(View.VISIBLE);
                                            }
                                        }else {
                                            noRatingTextView.setVisibility(View.VISIBLE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.restaurant_items,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();

        if (id==R.id.cart_items){
            Intent intent=new Intent(getApplicationContext(),CartViewActivity.class);
            startActivity(intent);
        }
        return true;
    }

    // accessing all the products of the restaurant
    private void accessingAllProductOfRestaurant(){

        progressBar.setVisibility(View.VISIBLE);

        productRef.orderByChild("restaurantName").equalTo(restaurantName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()){

                            for (DataSnapshot ds : snapshot.getChildren()) {

                                products product=ds.getValue(products.class);
                                if(ds.child("status").getValue().toString().equals("available")){
                                    product.setStatus("");
                                }else{
                                    product.setStatus("Currently Unavailable");
                                }

                                allProducts.add(product);
                            }

                            // calling sorting algorithm
                            sortingAccordingToAlphabetical();
                        }else{

                            // showing nothing found
                            progressBar.setVisibility(View.GONE);
                            nothingFoundImage.setVisibility(View.VISIBLE);
                            nothingFoundText.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);

                    }
                });
    }

    private void sortingAccordingToAlphabetical(){
        //Sorting according to category wise
        for (int i = 0; i < allProducts.size(); i++) {
            for (int j = i + 1; j < allProducts.size(); j++) {
                if (allProducts.get(i).getCategory().compareTo(allProducts.get(j).getCategory())>0)
                {
                    products temp= new products(allProducts.get(i).getProductId(),allProducts.get(i).getProductImage()
                            ,allProducts.get(i).getRestaurantName(),allProducts.get(i).getProductPrice(),allProducts.get(i).getProductName()
                            ,allProducts.get(i).getDescription(),allProducts.get(i).getCategory(),allProducts.get(i).getConsumerType()
                            ,allProducts.get(i).getHalf_price(),allProducts.get(i).getSelected_type(),allProducts.get(i).getStatus());

                    allProducts.set(i,allProducts.get(j));
                    allProducts.set(j,temp);
                }
            }
        }

        decideThatHasToShowCategory();
    }

    private void decideThatHasToShowCategory(){
        String cat="NO";
        allProductsOfRestaurant.clear();
        for (int i=0;i<allProducts.size();i++){

            if (cat.equals(allProducts.get(i).getCategory())){
                allProductsOfRestaurant.add(new isHasToShowCategory(allProducts.get(i),View.GONE));
            }else {
                allProductsOfRestaurant.add(new isHasToShowCategory(allProducts.get(i),View.VISIBLE));
            }
            cat=allProducts.get(i).getCategory();
        }

        progressBar.setVisibility(View.GONE);

        // show all the products
        listVi = new RestaurantItemsAdapter(RestaurantItemsActivity.this, allProductsOfRestaurant);
        listView.setAdapter(listVi);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(RestaurantItemsActivity.this, ProductDetails.class);
                    intent.putExtra("Pid", allProducts.get(position).getProductId());
                startActivity(intent);
            }
        });
    }


    // on back pressed clearing all the products the the variable has
     @Override
     public void onBackPressed() {
         super.onBackPressed();

         allProductsOfRestaurant.clear();
     }

     private void initializingViews(){

        listView=(ListView)findViewById(R.id.items_in_restaurants_list_view);

        restaurantDescription=(TextView)findViewById(R.id.restaurants_items_restaurant_description);

        noRatingTextView=(TextView)findViewById(R.id.restaurants_items_no_rating_text);
        restaurantRatings=(RatingBar)findViewById(R.id.restaurants_items_restaurants_rating_bar);
        totalRatingsTextView=(TextView)findViewById(R.id.restaurants_items_total_ratings);

        restaurantImage=(ImageView)findViewById(R.id.restaurants_items_restaurant_image);

        nothingFoundText=(TextView)findViewById(R.id.restaurant_inside_activity_nothing_found_text);
        nothingFoundImage=(ImageView)findViewById(R.id.restaurant_inside_activity_nothing_found_image);

        reviewRelativeLayout=(RelativeLayout)findViewById(R.id.restaurants_items_rating_layout);
        overAllRating=(TextView)findViewById(R.id.restaurant_items_over_all_ratings);

        progressBar=(ProgressBar)findViewById(R.id.items_in_restaurants_progress_bar);
    }
}