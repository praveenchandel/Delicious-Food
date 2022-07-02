package com.food.foodforyou;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class RatingActivity extends AppCompatActivity {

    private Button submitRatingButton;
    private RatingBar ratingBar;
    private EditText review;
    private ProgressDialog loadingBar;
    private String productId="";
    private DatabaseReference ProductsRef;

    private FirebaseAuth mAuth;
    private String userID;
    private String userName="";
    private String workIs="";
    private String type="";
    private int previousRating=5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        Toolbar toolbar = findViewById(R.id.toolbar_rating_activity);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        productId=getIntent().getStringExtra("Pid");
        workIs=getIntent().getStringExtra("Work");
        type=getIntent().getStringExtra("type");

        mAuth= FirebaseAuth.getInstance();
        userID= mAuth.getCurrentUser().getUid();

        ProductsRef = FirebaseDatabase.getInstance().getReference().child(type).child("JODHPUR");

        initializingViews();
        if (workIs.equals("editRating")){
            accessPreviousRatings();
        }

        ratingBar.setRating(5.0f);

        submitRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Getting the rating and displaying it on the toast

                if (!TextUtils.isEmpty(review.getText().toString()) && !review.getText().toString().equals("") ){
                    accessingUserInformation();
                }else {
                    Toast.makeText(RatingActivity.this, "Please write review...", Toast.LENGTH_SHORT).show();
                }
            }
        });

      //  final LayerDrawable drawable = (LayerDrawable)ratingBar.getProgressDrawable();

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override public void onRatingChanged(RatingBar ratingBar, float rating,
                                                  boolean fromUser) {
                if(rating<1.0f)
                    ratingBar.setRating(1.0f);
            }
        });
    }

    private void setRatingStarColor(Drawable drawable, @ColorInt int color)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            DrawableCompat.setTint(drawable, color);
        }
        else
        {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    // accessing previously given rating
    private void accessPreviousRatings(){

        ProductsRef.child(productId).child("Review").child("Reviews").child(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){

                                review.setText(snapshot.child("Review").getValue().toString());

                                int stars=Integer.parseInt(snapshot.child("Stars").getValue().toString());
                                previousRating=stars;
                                ratingBar.setRating(stars);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void updateRatings(){

        loadingBar.setTitle("Submitting your review");
        loadingBar.setMessage("Please wait while we are submitting your review");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        ProductsRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Review").exists()){

                    // if there is already ratings available
                    final int givenRating=(int)ratingBar.getRating();
                    String currentValue="";

                    HashMap<String,Object> RatingMap=new HashMap<>();

                    if(givenRating==1){
                        currentValue=snapshot.child("Review").child("1star").getValue().toString();
                        int val=Integer.parseInt(currentValue)+1;
                        RatingMap.put("1star",String.valueOf(val));
                    }else if(givenRating==2){
                        currentValue=snapshot.child("Review").child("2star").getValue().toString();
                        int val=Integer.parseInt(currentValue)+1;
                        RatingMap.put("2star",String.valueOf(val));
                    }else if(givenRating==3){
                        currentValue=snapshot.child("Review").child("3star").getValue().toString();
                        int val=Integer.parseInt(currentValue)+1;
                        RatingMap.put("3star",String.valueOf(val));
                    }else if(givenRating==4){
                        currentValue=snapshot.child("Review").child("4star").getValue().toString();
                        int val=Integer.parseInt(currentValue)+1;
                        RatingMap.put("4star",String.valueOf(val));
                    }else {
                        currentValue=snapshot.child("Review").child("5star").getValue().toString();
                        int val=Integer.parseInt(currentValue)+1;
                        RatingMap.put("5star",String.valueOf(val));
                    }

                    final int one=Integer.parseInt(snapshot.child("Review").child("1star").getValue().toString());
                    final int two=Integer.parseInt(snapshot.child("Review").child("2star").getValue().toString());
                    final int three=Integer.parseInt(snapshot.child("Review").child("3star").getValue().toString());
                    final int four=Integer.parseInt(snapshot.child("Review").child("4star").getValue().toString());
                    final int five=Integer.parseInt(snapshot.child("Review").child("5star").getValue().toString());

                    HashMap<String,Object> ReviewMap=new HashMap<>();

                    ReviewMap.put("userName",userName);
                    ReviewMap.put("Stars",String.valueOf(givenRating));
                    ReviewMap.put("Review",review.getText().toString());

                    Calendar calendar = Calendar.getInstance();

                    SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,yyyy");
                    String date = currentDate.format(calendar.getTime());

                    ReviewMap.put("Date",date);

                    RatingMap.put("Reviews/" + userID,ReviewMap);

                    ProductsRef.child(productId).child("Review").updateChildren(RatingMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        if (type.equals("Restaurants")){

                                            int totalRev=one+two+three+four+five+1;
                                            int totalRat=one+two*2+three*3+4*four+five*5+givenRating;

                                            // updating ratings on restaurants main page
                                            HashMap<String,Object> restaurantReviewMap=new HashMap<>();
                                            restaurantReviewMap.put("total_ratings",String.valueOf(totalRat));
                                            restaurantReviewMap.put("total_reviews",String.valueOf(totalRev));

                                            ProductsRef.child(productId).updateChildren(restaurantReviewMap)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                Toast.makeText(RatingActivity.this, "reviews submitted successfully at restaurant", Toast.LENGTH_SHORT).show();
                                                            }else {
                                                                Toast.makeText(RatingActivity.this, "Review submitted but some error occurred during sending to restaurant", Toast.LENGTH_SHORT).show();
                                                            }
                                                            loadingBar.dismiss();
                                                            finish();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(RatingActivity.this, "Review submitted but some error occurred during sending to restaurant", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            });
                                        }else {
                                            loadingBar.dismiss();
                                            finish();
                                        }
                                    }else {
                                        loadingBar.dismiss();
                                        Toast.makeText(RatingActivity.this, "Some Error Occurred...", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingBar.dismiss();
                            Toast.makeText(RatingActivity.this, "Some Error Occurred...", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }else {

                    final int givenRating=(int)ratingBar.getRating();

                    // if initially there is no ratings then we will do this
                     HashMap<String,Object> RatingMap=new HashMap<>();

                    if (givenRating==1){
                        RatingMap.put("1star","1");
                        RatingMap.put("2star","0");
                        RatingMap.put("3star","0");
                        RatingMap.put("4star","0");
                        RatingMap.put("5star","0");
                    }else if(givenRating==2){
                        RatingMap.put("1star","0");
                        RatingMap.put("2star","1");
                        RatingMap.put("3star","0");
                        RatingMap.put("4star","0");
                        RatingMap.put("5star","0");
                    }else if (givenRating==3){
                        RatingMap.put("1star","0");
                        RatingMap.put("2star","0");
                        RatingMap.put("3star","1");
                        RatingMap.put("4star","0");
                        RatingMap.put("5star","0");
                    }else if (givenRating==4){
                        RatingMap.put("1star","0");
                        RatingMap.put("2star","0");
                        RatingMap.put("3star","0");
                        RatingMap.put("4star","1");
                        RatingMap.put("5star","0");
                    }else {
                        RatingMap.put("1star","0");
                        RatingMap.put("2star","0");
                        RatingMap.put("3star","0");
                        RatingMap.put("4star","0");
                        RatingMap.put("5star","1");
                    }

                    HashMap<String,Object> ReviewMap=new HashMap<>();

                    ReviewMap.put("userName",userName);
                    ReviewMap.put("Stars",String.valueOf(givenRating));
                    ReviewMap.put("Review",review.getText().toString());

                    Calendar calendar = Calendar.getInstance();

                    SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,yyyy");
                    String date = currentDate.format(calendar.getTime());

                    ReviewMap.put("Date",date);

                    RatingMap.put("Reviews/" + userID,ReviewMap);

                    // combining all the task together from be free of bugs
//                    HashMap<String,Object> finalUpdateMap=new HashMap<>();
//                    finalUpdateMap.put(productId + "/Review",RatingMap);
//
//                    if (type.equals("Restaurants")){
//                        HashMap<String,Object> restaurantReviewMap=new HashMap<>();
//                        restaurantReviewMap.put("total_ratings",String.valueOf(givenRating));
//                        restaurantReviewMap.put("total_reviews","1");
//
//                        finalUpdateMap.put(productId,restaurantReviewMap);
//                    }

//                    ProductsRef.updateChildren(finalUpdateMap)
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()){
//                                        Toast.makeText(RatingActivity.this, "Review submitted successfully..", Toast.LENGTH_SHORT).show();
//                                    }else {
//                                        Toast.makeText(RatingActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(RatingActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
//                        }
//                    });

                    ProductsRef.child(productId).child("Review").updateChildren(RatingMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        // if user gives the rating to the restaurant

                                        if (type.equals("Restaurants")){

                                            // updating ratings on restaurants main page
                                            HashMap<String,Object> restaurantReviewMap=new HashMap<>();
                                            restaurantReviewMap.put("total_ratings",String.valueOf(givenRating));
                                            restaurantReviewMap.put("total_reviews","1");

                                            ProductsRef.child(productId).updateChildren(restaurantReviewMap)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                Toast.makeText(RatingActivity.this, "reviews submitted successfully at restaurant", Toast.LENGTH_SHORT).show();
                                                            }else {
                                                                Toast.makeText(RatingActivity.this, "Review submitted but some error occurred during sending to restaurant", Toast.LENGTH_SHORT).show();
                                                            }
                                                            loadingBar.dismiss();
                                                            finish();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(RatingActivity.this, "Review submitted but some error occurred during sending to restaurant", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            });
                                        }else {
                                            loadingBar.dismiss();
                                            Toast.makeText(RatingActivity.this, "Review submitted successfully...", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }else {
                                        Toast.makeText(RatingActivity.this, "Some Error Occurred...", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                        finish();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingBar.dismiss();
                            Toast.makeText(RatingActivity.this, "Some Error Occurred...", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    // accessing user information
    private void accessingUserInformation() {
        final DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference();

        UsersRef.child("Users").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    // String image = dataSnapshot.child("image").getValue().toString();
                    userName = snapshot.child("name").getValue().toString();

                    if (workIs.equals("editRating")){
                        updatePreviousRatings();
                    }else {
                        updateRatings();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RatingActivity.this, "Enable to access your information...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // update the previous rating
    private void updatePreviousRatings(){

        loadingBar.setTitle("Updating your review");
        loadingBar.setMessage("Please wait while we are updating your review");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        ProductsRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("Review").exists()) {

                    final int givenRating = (int) ratingBar.getRating();
                    String currentValue = "";

                    HashMap<String, Object> RatingMap = new HashMap<>();

                    // if previous rating is different from current rating
                    if (previousRating != givenRating) {

                    if (givenRating == 1 || previousRating==1) {
                        currentValue = snapshot.child("Review").child("1star").getValue().toString();
                        int val = Integer.parseInt(currentValue);
                        if (givenRating==1){
                            val=val+1;
                        }else {
                            val=val-1;
                        }
                        RatingMap.put("1star", String.valueOf(val));
                    }
                    if (givenRating == 2 || previousRating==2) {
                        currentValue = snapshot.child("Review").child("2star").getValue().toString();
                        int val = Integer.parseInt(currentValue);
                        if (givenRating==2){
                            val=val+1;
                        }else {
                            val=val-1;
                        }
                        RatingMap.put("2star", String.valueOf(val));
                    }
                    if (givenRating == 3 || previousRating==3) {
                        currentValue = snapshot.child("Review").child("3star").getValue().toString();
                        int val = Integer.parseInt(currentValue);
                        if (givenRating==3){
                            val=val+1;
                        }else {
                            val=val-1;
                        }
                        RatingMap.put("3star", String.valueOf(val));
                    }
                    if (givenRating == 4 || previousRating==4) {
                        currentValue = snapshot.child("Review").child("4star").getValue().toString();
                        int val = Integer.parseInt(currentValue);
                        if (givenRating==4){
                            val=val+1;
                        }else {
                            val=val-1;
                        }
                        RatingMap.put("4star", String.valueOf(val));
                    }
                    if (givenRating==5 || previousRating==5){
                        currentValue = snapshot.child("Review").child("5star").getValue().toString();
                        int val = Integer.parseInt(currentValue);
                        if (givenRating==5){
                            val=val+1;
                        }else {
                            val=val-1;
                        }
                        RatingMap.put("5star", String.valueOf(val));
                    }
                }

                    final int one=Integer.parseInt(snapshot.child("Review").child("1star").getValue().toString());
                    final int two=Integer.parseInt(snapshot.child("Review").child("2star").getValue().toString());
                    final int three=Integer.parseInt(snapshot.child("Review").child("3star").getValue().toString());
                    final int four=Integer.parseInt(snapshot.child("Review").child("4star").getValue().toString());
                    final int five=Integer.parseInt(snapshot.child("Review").child("5star").getValue().toString());

                    HashMap<String,Object> ReviewMap=new HashMap<>();

                    ReviewMap.put("userName",userName);
                    ReviewMap.put("Stars",String.valueOf(givenRating));
                    ReviewMap.put("Review",review.getText().toString());

                    Calendar calendar = Calendar.getInstance();

                    SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,yyyy");
                    String date = currentDate.format(calendar.getTime());

                    ReviewMap.put("Date",date);

                    RatingMap.put("Reviews/" + userID,ReviewMap);

                    ProductsRef.child(productId).child("Review").updateChildren(RatingMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        if (type.equals("Restaurants")){

                                            int totalRev=one+two+three+four+five;
                                            int totalRat=one+two*2+three*3+four*4+five*5+givenRating-previousRating;

                                            // updating ratings on restaurants main page
                                            HashMap<String,Object> restaurantReviewMap=new HashMap<>();
                                            restaurantReviewMap.put("total_ratings",String.valueOf(totalRat));
                                            restaurantReviewMap.put("total_reviews",String.valueOf(totalRev));

                                            ProductsRef.child(productId).updateChildren(restaurantReviewMap)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                Toast.makeText(RatingActivity.this, "reviews submitted successfully at restaurant", Toast.LENGTH_SHORT).show();
                                                            }else {
                                                                Toast.makeText(RatingActivity.this, "Review submitted but some error occurred during sending to restaurant", Toast.LENGTH_SHORT).show();
                                                            }
                                                            loadingBar.dismiss();
                                                            finish();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    loadingBar.dismiss();
                                                    Toast.makeText(RatingActivity.this, "Review submitted but some error occurred during sending to restaurant", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            });
                                        }else {
                                            loadingBar.dismiss();
                                            Toast.makeText(RatingActivity.this, "Review submitted successfully...", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    }else {
                                        loadingBar.dismiss();
                                        Toast.makeText(RatingActivity.this, "Some Error Occurred...", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingBar.dismiss();
                            Toast.makeText(RatingActivity.this, "Some Error Occurred...", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void initializingViews(){

        submitRatingButton=(Button)findViewById(R.id.rating_activity_submit_button);
        ratingBar=(RatingBar)findViewById(R.id.rating_activity_ratingBar);
        review=(EditText)findViewById(R.id.rating_activity_review_editText);

        loadingBar=new ProgressDialog(this);
    }

}