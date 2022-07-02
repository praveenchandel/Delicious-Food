package com.food.foodforyou;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
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

import com.food.foodforyou.BottomSheet.CustomizedBottomSheet;
import com.food.foodforyou.BottomSheet.ExampleBottomSheetDialog;
import com.food.foodforyou.Model.offers;
import com.food.foodforyou.Model.products;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.HashMap;

import static java.security.AccessController.getContext;

public class ProductDetails extends AppCompatActivity implements CustomizedBottomSheet.BottomSheetListener{

    private Button addItemToCart,increaseQuantityButton,decreaseQuantityButton;
    private Button giveRating;
    private ImageView itemImage;
    private TextView productPrice,productDescription,productName;
    private TextView quantityNumberTextView,quantityByPrice;
    private TextView consumerType,itemCategory,restaurantName;
    private TextView seeCancellationPolicy;
    private String productId;
    private int currentQuantatiy=1;
    private int quantity_price=0;
    private FirebaseAuth mAuth;
    private DatabaseReference ProductsRef;
    private String userID;
    private SwitchCompat simpleSwitch;
    private TextView offersTxt;
    private ProgressDialog loadingBar;

    private LinearLayout userReviewLayout;
    private TextView reviewDate,reviewDetails;
    private RatingBar reviewRatingBar;
    private TextView deleteRatingButton;
    private TextView editRatingButton;
    private Button seeAllRatingsButton;

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

    private ImageView back_arrow_icon;
    private ImageView cart_icon;
    private TextView toolbar_name;

    private TextView totalRatingsTextView;

    private int totalRatings=0;
    private int givenRatings=0;

    public static offers offer1,offer2;

    public static String half_quantity="";
    public static String full_quantity="";
    public static String dishName="";
    public static int fullPlatePrice=0;
    public static int halfPlatePrice=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

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

        toolbar_name.setText("Product Details");

        productId=getIntent().getStringExtra("Pid");

        ProductsRef = FirebaseDatabase.getInstance().getReference().child("Products").child("JODHPUR");

        initializeViews();

        mAuth=FirebaseAuth.getInstance();
        userID=mAuth.getCurrentUser().getUid();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null){
                Intent intent = new Intent(ProductDetails.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        }

        if (isConnectionAvailable(ProductDetails.this)){
            getProductDetails();

        }else {
            new LovelyStandardDialog(ProductDetails.this)
                    .setTopColorRes(R.color.colorPrimary)
                    .setIcon(R.drawable.ic_baseline_signal_cellular_connected_no_internet_4_bar_24)
                    .setTitle("No Internet Connection")
                    .setMessage("It seems like you don't have any internet connection.")
                    .setPositiveButton(android.R.string.ok,null)
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }

        increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQuantatiy<50) {
                    currentQuantatiy++;
                    setPrice();
                }
            }
        });

        decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQuantatiy>1) {
                    currentQuantatiy--;
                    setPrice();
                }
            }
        });

        addItemToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth= FirebaseAuth.getInstance();
                userID=mAuth.getCurrentUser().getUid();
                getIsProductDetailsInCart();
            }
        });

        simpleSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (simpleSwitch.isChecked()){
                    quantity_price=halfPlatePrice;
                }else {
                    quantity_price=fullPlatePrice;
                }
                setPrice();
            }
        });

        offersTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExampleBottomSheetDialog bottomSheet = new ExampleBottomSheetDialog();
                bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");
            }
        });

        giveRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),RatingActivity.class);
                intent.putExtra("Pid",productId);
                intent.putExtra("Work","giveRating");
                intent.putExtra("type","Products");
                startActivity(intent);
            }
        });

        seeCancellationPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),RulesActivity.class);
                startActivity(intent);
            }
        });

        editRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),RatingActivity.class);
                intent.putExtra("Pid",productId);
                intent.putExtra("Work","editRating");
                intent.putExtra("type","Products");
                startActivity(intent);
            }
        });

        seeAllRatingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),AllProductReviewActivity.class);
                intent.putExtra("Pid",productId);
                intent.putExtra("type","Products");
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

                if (isConnectionAvailable(ProductDetails.this)){

                    new LovelyStandardDialog(ProductDetails.this)
                            .setTopColorRes(R.color.colorPrimary)
                            .setIcon(R.drawable.ic_baseline_warning_24)
                            .setTitle("Confirm ?")
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

                }else {
                    new LovelyStandardDialog(ProductDetails.this)
                            .setTopColorRes(R.color.colorPrimary)
                            .setIcon(R.drawable.ic_baseline_signal_cellular_connected_no_internet_4_bar_24)
                            .setTitle("No Internet Connection")
                            .setMessage("It seems like you don't have any internet connection.")
                            .setPositiveButton(android.R.string.ok,null)
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
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


    // if product is already added in the cart
    private void getIsProductDetailsInCart(){

        loadingBar.setTitle("Checking that is item is in cart");
        loadingBar.setMessage("Please wait while we are checking that item is already in your cart or not");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        final DatabaseReference cartListRef= FirebaseDatabase.getInstance().getReference().child("Cart List");

        cartListRef.child(userID).child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    // if product is already present in cart

                    if(snapshot.child("half_quantity").exists()){
                        half_quantity=snapshot.child("half_quantity").getValue().toString();
                    }
                    if (snapshot.child("full_quantity").exists()){
                        full_quantity=snapshot.child("full_quantity").getValue().toString();
                    }

                    loadingBar.dismiss();

                    CustomizedBottomSheet bottomSheet = new CustomizedBottomSheet();
                    bottomSheet.show(getSupportFragmentManager(), "CustomizedBottomSheet");
                }else {
                    // if product was not in cart before
                    addingToCartList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // based on return from the bottom sheet we will move further
    @Override
    public void onButtonClicked(String text) {

        // if user wants to run the current operation
        if (text.equals("Run")){
            addingToCartList();
        }else {
            removeFromCart(text);
        }
    }


    // removing the product from the cart
    private void removeFromCart(String type){

        final DatabaseReference cartListRef= FirebaseDatabase.getInstance().getReference().child("Cart List");

        if (type.equals("Remove_Product")){

            loadingBar.setTitle("Removing product from your cart");
            loadingBar.setMessage("Please wait while we are removing the product from your cart");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            cartListRef.child(userID).child(productId).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ProductDetails.this, "Removed from cart successfully..", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            full_quantity="";
                            half_quantity="";
                        }
                    });
        }else if(type.equals("Delete_Half")){

            loadingBar.setTitle("Removing product from your cart");
            loadingBar.setMessage("Please wait while we are removing the product from your cart");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            cartListRef.child(userID).child(productId).child("half_quantity").removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ProductDetails.this, "Removed from cart successfully..", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            half_quantity="";
                        }
                    });
        }else if (type.equals("Delete_Full")){

            loadingBar.setTitle("Removing product from your cart");
            loadingBar.setMessage("Please wait while we are removing the product from your cart");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            cartListRef.child(userID).child(productId).child("full_quantity").removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ProductDetails.this, "Removed from cart successfully..", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            full_quantity="";
                        }
                    });
        }
    }

    private void DeleteUserRating(){

        loadingBar.setTitle("Deleting your review");
        loadingBar.setMessage("Please wait while we are deleting your review");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        ProductsRef.child(productId).child("Review").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int deletedRatingTargetIs=1;
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
                    ProductsRef.child(productId).child("Review").updateChildren(deleteRatingMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                // deleting the user review
                                ProductsRef.child(productId).child("Review").child("Reviews").child(userID)
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){
                                            Intent intent=new Intent(getApplicationContext(),ProductDetails.class);
                                            intent.putExtra("Pid", productId);
                                            startActivity(intent);
                                            finish();
                                            Toast.makeText(ProductDetails.this, "Review deleted successfully...", Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(ProductDetails.this, "some error occurred try again later...", Toast.LENGTH_SHORT).show();
                                        }
                                        loadingBar.dismiss();
                                    }
                                });
                            }else {
                                Toast.makeText(ProductDetails.this, "some error occurred try again later...", Toast.LENGTH_SHORT).show();
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


    private void addingToCartList(){

        loadingBar.setTitle("Adding Item to Cart");
        loadingBar.setMessage("Please wait while we are adding item to your cart");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        final DatabaseReference cartListRef= FirebaseDatabase.getInstance().getReference().child("Cart List");

        final HashMap<String,Object> cartMap=new HashMap<>();

        String quantity=Integer.toString(currentQuantatiy);
          cartMap.put("ProductId",productId);
          if (simpleSwitch.isChecked()) {
              cartMap.put("half_quantity",quantity);
          }else {
              cartMap.put("full_quantity",quantity);
          }

        cartListRef.child(userID).child(productId).updateChildren(cartMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProductDetails.this, "Item added to cart successfully..", Toast.LENGTH_SHORT).show();
                        }
                        loadingBar.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(ProductDetails.this, "enable to add item to cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getProductDetails(){

        ProductsRef.child(productId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    products mProduct=snapshot.getValue(products.class);

                    fullPlatePrice=Integer.parseInt(mProduct.getProductPrice());
                    quantity_price=fullPlatePrice;
                    setPrice();
                    productName.setText(mProduct.getProductName());
                    dishName=mProduct.getProductName();
                    if (mProduct.getHalf_price().equals("no")) {
                        productPrice.setText("₹" + mProduct.getProductPrice());
                    }else {
                        productPrice.setText("Full ₹" + mProduct.getProductPrice() + ", Half ₹" + mProduct.getHalf_price());
                        simpleSwitch.setVisibility(View.VISIBLE);
                        halfPlatePrice=Integer.parseInt(mProduct.getHalf_price());
                    }

                    consumerType.setText(mProduct.getConsumerType());
                    restaurantName.setText(mProduct.getRestaurantName());
                    itemCategory.setText(mProduct.getCategory());

                    if(mProduct.getStatus().equals("unavailable")){
                        addItemToCart.setClickable(false);
                        addItemToCart.setText("Currently Unavailable");
                    }

                    // showing all reviews
                    if (snapshot.child("Review").exists()) {
                        int one = Integer.parseInt(snapshot.child("Review").child("1star").getValue().toString());
                        int two = Integer.parseInt(snapshot.child("Review").child("2star").getValue().toString());
                        int three = Integer.parseInt(snapshot.child("Review").child("3star").getValue().toString());
                        int four = Integer.parseInt(snapshot.child("Review").child("4star").getValue().toString());
                        int five = Integer.parseInt(snapshot.child("Review").child("5star").getValue().toString());

                        int total = one + two + three + four + five;
                        totalRatings = total;

                        // to getting rid of from divide by zero exception
                        if (total > 0) {

                            totalRatingsTextView.setText(String.valueOf(total) + " Reviews");

                            oneTotalReviewsTextView.setText(String.valueOf(one));
                            twoTotalReviewsTextView.setText(String.valueOf(two));
                            threeTotalReviewsTextView.setText(String.valueOf(three));
                            fourTotalReviewsTextView.setText(String.valueOf(four));
                            fiveTotalReviewsTextView.setText(String.valueOf(five));

                            DecimalFormat df = new DecimalFormat("#.#");

                            double fullRating = (double) total;

                            double overAll = (one + two * 2 + three * 3 + four * 4 + five * 5) / fullRating;
                            overAll = Double.parseDouble(df.format(overAll));
                            overAllRatingsText.setText(String.valueOf(overAll));
                            overAllRatingsRatingBar.setRating((float) overAll);

                            one = one * 100;
                            one = one / total;
                            // one=one/5;
                            oneStarProgressBar.setProgress(Math.max(one, 5));

                            two = two * 100;
                            two = two / total;
                            //two=two/5;
                            twoStarProgressBar.setProgress(Math.max(two, 5));

                            three = three * 100;
                            three = three / total;
                            // three=three/5;
                            threeStarProgressBar.setProgress(Math.max(three, 5));

                            four = four * 100;
                            four = four / total;
                            //four=four/5;
                            fourStarProgressBar.setProgress(Math.max(four, 5));

                            five = five * 100;
                            five = five / total;
                            // five=five/5;
                            fiveStarProgressBar.setProgress(Math.max(five, 5));
                        }
                    }

                    if (snapshot.child("Review").child("Reviews").child(userID).exists()){
                        giveRating.setVisibility(View.GONE);
                        userReviewLayout.setVisibility(View.VISIBLE);
                        reviewDate.setText(snapshot.child("Review").child("Reviews").child(userID).child("Date").getValue().toString());
                        reviewDetails.setText(snapshot.child("Review").child("Reviews").child(userID).child("Review").getValue().toString());

                        int stars=Integer.parseInt(snapshot.child("Review").child("Reviews").child(userID).child("Stars").getValue().toString());
                        givenRatings=stars;
                        reviewRatingBar.setRating(stars);
                    }

                    productDescription.setText(mProduct.getDescription());
                    Picasso.get().load(mProduct.getProductImage()).placeholder(R.drawable.food_for_you).into(itemImage);

                    // getting offers details
                    offer1=new offers(snapshot.child("offers").child("offer1").getValue(offers.class));
                    offer2=new offers(snapshot.child("offers").child("offer2").getValue(offers.class));

                }else {
                    Toast.makeText(ProductDetails.this, "details not available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setPrice(){
        int price=currentQuantatiy*quantity_price;
        quantityByPrice.setText("₹" + Integer.toString(price));
        quantityNumberTextView.setText(Integer.toString(currentQuantatiy));
    }

    public void initializeViews(){
        addItemToCart=(Button)findViewById(R.id.pd_add_to_cart_button);
        increaseQuantityButton=(Button)findViewById(R.id.increaseQuantityButton);
        decreaseQuantityButton=(Button)findViewById(R.id.decrease_quantity_button);
        giveRating=(Button)findViewById(R.id.product_details_give_product_rating);

        productPrice=(TextView) findViewById(R.id.product_price_details);
        productDescription=(TextView)findViewById(R.id.product_description_details);
        productName=(TextView)findViewById(R.id.product_name_details);
        quantityNumberTextView=(TextView)findViewById(R.id.currentQuantity);
        quantityByPrice=(TextView)findViewById(R.id.quantity_by_price);
        consumerType=(TextView)findViewById(R.id.details_about_consumer_type);
        itemCategory=(TextView)findViewById(R.id.details_about_item_category);
        restaurantName=(TextView)findViewById(R.id.product_restaurant_details);
        seeCancellationPolicy=(TextView)findViewById(R.id.add_to_card_view_cancellation_text);

        itemImage=(ImageView)findViewById(R.id.product_image_details);
        simpleSwitch=(SwitchCompat)findViewById(R.id.half_price_product_switch);

        loadingBar=new ProgressDialog(this);

        offersTxt=(TextView)findViewById(R.id.add_to_card_see_offers_text);

        // for user rating
        userReviewLayout=(LinearLayout)findViewById(R.id.product_details_user_review_layout);
        reviewDate=(TextView)findViewById(R.id.product_details_review_date);
        reviewDetails=(TextView)findViewById(R.id.product_details_user_review);
        reviewRatingBar=(RatingBar)findViewById(R.id.user_ratingBar);
        deleteRatingButton=(TextView) findViewById(R.id.product_details_review_delete_button);
        editRatingButton=(TextView) findViewById(R.id.product_details_review_edit_button);

        // for all ratings

        overAllRatingsText=(TextView)findViewById(R.id.product_details_over_all_ratings);
        overAllRatingsRatingBar=(RatingBar)findViewById(R.id.ratingBar);

        fiveStarProgressBar=(ProgressBar)findViewById(R.id.product_details_five_star_progressBar);
        fourStarProgressBar=(ProgressBar)findViewById(R.id.product_details_four_star_progressBar);
        threeStarProgressBar=(ProgressBar)findViewById(R.id.product_details_three_star_progressBar);
        twoStarProgressBar=(ProgressBar)findViewById(R.id.product_details_two_star_progressBar);
        oneStarProgressBar=(ProgressBar)findViewById(R.id.product_details_one_star_progressBar);

        fiveTotalReviewsTextView=(TextView) findViewById(R.id.product_details_five_star_ratings);
        fourTotalReviewsTextView=(TextView) findViewById(R.id.product_details_four_star_ratings);
        threeTotalReviewsTextView=(TextView) findViewById(R.id.product_details_three_star_ratings);
        twoTotalReviewsTextView=(TextView) findViewById(R.id.product_details_two_star_ratings);
        oneTotalReviewsTextView=(TextView) findViewById(R.id.product_details_one_star_ratings);

        totalRatingsTextView=(TextView) findViewById(R.id.product_details_total_ratings);
        seeAllRatingsButton=(Button)findViewById(R.id.product_details_see_all_ratings_button);
    }
}