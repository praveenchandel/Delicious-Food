package com.food.foodforyou;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.food.foodforyou.Model.HomeRestaurants;
import com.food.foodforyou.Model.restaurants;

import com.food.foodforyou.ViewHolder.restaurantViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DatabaseReference restaurantsRef;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    private FirebaseAuth mAuth;
    private String userID;

    private long backPressTime=0;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth= FirebaseAuth.getInstance();
        userID=mAuth.getCurrentUser().getUid();

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        restaurantsRef = FirebaseDatabase.getInstance().getReference().child("Restaurants").child("JODHPUR");

        recyclerView = findViewById(R.id.recycler_menu);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAuth=FirebaseAuth.getInstance();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null){

            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        // setting notification feature
        if(isConnectionAvailable(HomeActivity.this)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                NotificationChannel channel=
                        new NotificationChannel("MyNotifications","MyNotifications", NotificationManager.IMPORTANCE_DEFAULT);

                NotificationManager manager=getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }

            FirebaseMessaging.getInstance().subscribeToTopic("general")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            String msg = "Successful";
                            if (!task.isSuccessful()) {
                                msg = "Failed";
                            }
                            Log.i("HomeActivity : ","Notification message : " + msg);
                        }
                    });
        }



        if (isConnectionAvailable(HomeActivity.this)){
            accessRestaurants();
        }else {
            new LovelyStandardDialog(HomeActivity.this)
                    .setTopColorRes(R.color.colorPrimary)
                    .setIcon(R.drawable.ic_baseline_signal_cellular_connected_no_internet_4_bar_24)
                    .setTitle("No Internet Connection")
                    .setMessage("It seems like you don't have any internet connection.")
                    .setPositiveButton(android.R.string.ok,null)
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        final TextView userNameTextView =(TextView) headerView.findViewById(R.id.user_profile_name);


        // setting the user name
            final DatabaseReference RootRef;
            RootRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("name");

            RootRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userNameTextView.setText(snapshot.getValue().toString());
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
    protected void onStart() {
        super.onStart();

        // checking for update
        AppUpdateChecker appUpdateChecker=new AppUpdateChecker(this);  //pass the activity in constructure
        appUpdateChecker.checkForUpdate(false); //mannual check false here
    }


    // showing toast message to confirm again before closing the app
    @Override
    public void onBackPressed() {

        if(backPressTime+2000>System.currentTimeMillis()){
            backToast.cancel();
            super.onBackPressed();
        }else {
            backToast=Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressTime=System.currentTimeMillis();
    }

    private void accessRestaurants() {

        FirebaseRecyclerOptions<restaurants> options =
                new FirebaseRecyclerOptions.Builder<restaurants>()
                        .setQuery(restaurantsRef,restaurants.class)
                        .build();


        FirebaseRecyclerAdapter<restaurants, restaurantViewHolder> adapter =
                new FirebaseRecyclerAdapter<restaurants, restaurantViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final restaurantViewHolder holder, int position, @NonNull final restaurants model)
                    {

                        holder.textRestaurantName.setText(model.getRestaurant_name());
                        holder.textAboutRestaurant.setText(model.getRestaurant_description());

                        if (Integer.parseInt(model.getTotal_reviews())>0){

                            DecimalFormat df = new DecimalFormat("#.#");

                            double fullRating = (double) Integer.parseInt(model.getTotal_reviews());

                            double overAll = Integer.parseInt(model.getTotal_ratings()) / fullRating;
                            overAll = Double.parseDouble(df.format(overAll));

                            holder.restaurantTotalRatings.setText(String.valueOf(overAll) + "/5");
                        }else {
                            holder.restaurantTotalRatings.setText(" - ");
                        }

                        Picasso.get().load(model.getRestaurant_image()).placeholder(R.drawable.delicious_food_background_less).into(holder.imageView);

                        holder.textRecycle.setVisibility(model.getStatus());

                        if(model.getStatus()==8) {
                            // restaurant is available
                            holder.restaurantCardView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent intent = new Intent(getApplicationContext(), RestaurantItemsActivity.class);
                                    intent.putExtra("restaurant_name", model.getRestaurant_name());
                                    intent.putExtra("restaurant_description",model.getRestaurant_description());
                                    intent.putExtra("restaurant_image",model.getRestaurant_image());
                                    startActivity(intent);
                                }
                            });

                        }else{
                            // restaurant is not available

                            holder.restaurantCardView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Toast.makeText(HomeActivity.this, "Sorry but, This restaurant is not Currently available...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        //  we don't want to show offers right now
            //holder.offersLinearLayout.setVisibility(model.getShowFlat());


            // right now we don't want to show any offers
                        holder.offersLinearLayout.setVisibility(View.GONE);

            holder.offerOneImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(getApplicationContext(), OffersWithMinimumOff.class);
                    intent.putExtra("minimum_off","offers");
                    startActivity(intent);
                }
            });

            holder.offerTwoImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(getApplicationContext(),OffersWithMinimumOff.class);
                    intent.putExtra("minimum_off","5");
                    startActivity(intent);
                }
            });

            holder.offerThreeImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(getApplicationContext(),OffersWithMinimumOff.class);
                    intent.putExtra("minimum_off","10");
                    startActivity(intent);
                }
            });

            holder.offerFourImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(getApplicationContext(),OffersWithMinimumOff.class);
                    intent.putExtra("minimum_off","15");
                    startActivity(intent);
                }
            });
                    }

                    @NonNull
                    @Override
                    public restaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_layout, parent, false);
                        restaurantViewHolder holder = new restaurantViewHolder(view);
                        return holder;
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id=item.getItemId();

        if (id==R.id.search){
            Intent intent=new Intent(getApplicationContext(),SearchActivity.class);
            startActivity(intent);
        }else if (id==R.id.cart){
            Intent intent=new Intent(getApplicationContext(),CartViewActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_cart){
                Intent intent = new Intent(HomeActivity.this, CartViewActivity.class);
                startActivity(intent);
        }
        else if (id == R.id.nav_shop_by_category)
        {
            Intent intent = new Intent(HomeActivity.this, ShopByCategory.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_orders)
        {
            Intent intent=new Intent(HomeActivity.this,OrdersActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_account)
        {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
        }
        else if (id == R.id.nav_share)
        {
            shareThisApp();
        }
        else if (id == R.id.nav_rules)
        {
            Intent intent=new Intent(getApplicationContext(),RulesActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_rate_app)
        {
            rateApp();
        }
        else if (id == R.id.nav_contact_support)
        {
            Intent intent=new Intent(getApplicationContext(),ContactUsActivity.class);
            startActivity(intent);
        }
        else if(id==R.id.nav_about_us){
            Intent intent=new Intent(getApplicationContext(),AboutUs.class);
            startActivity(intent);
        }else if(id==R.id.nav_report_bug){
            reportBug();
        }
        else if (id == R.id.nav_logout)
        {
            new LovelyStandardDialog(HomeActivity.this)
                    .setTopColorRes(R.color.colorPrimary)
                    .setIcon(R.drawable.logout)
                    .setTitle("Confirm Log Out")
                    .setMessage("You are logging out of your Food For You account from this device.")
                    .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent=new Intent(HomeActivity.this,LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // intent g-mail for reporting bug
    private void reportBug(){
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("*/*");
        intent.setData(Uri.parse("mailto:praveenchandel87695@gmail.com")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, "Report Bug");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    // this function will share this app link
    private void shareThisApp(){
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
            String shareMessage= "\nLet me recommend you this application\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch(Exception e) {
            //e.toString();
        }
    }

    /*
     * Start with rating the app
     * Determine if the Play Store is installed on the device
     *
     * */
    public void rateApp()
    {
        try
        {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        }
        catch (ActivityNotFoundException e)
        {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }

    private Intent rateIntentForUrl(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
        {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
        else
        {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }

}