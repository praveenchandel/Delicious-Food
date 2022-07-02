package com.food.foodforyou;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.food.foodforyou.Model.products;
import com.food.foodforyou.ViewHolder.ProductViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

public class SearchByCategory extends AppCompatActivity {

    private String category_type="";
    private LinearLayoutManager layoutManager;
    private RecyclerView recycleView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_by_category);

        category_type = getIntent().getStringExtra("category_name");

        Toolbar toolbar = findViewById(R.id.toolbar_search_by_category);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);

        toolbar.setTitle(category_type);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initializingViews();

        mAuth= FirebaseAuth.getInstance();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null){
            // check that email is verified or not
//            if (currentUser.isEmailVerified()) {
            Intent intent = new Intent(SearchByCategory.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
//            }else{
//                Intent intent = new Intent(LoginActivity.this, verifyUser.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
//                finish();
//            }
        }

        if (isConnectionAvailable(SearchByCategory.this)){
            accessingAllProducts();
        }else {
            new LovelyStandardDialog(SearchByCategory.this)
                    .setTopColorRes(R.color.colorPrimary)
                    .setIcon(R.drawable.ic_baseline_signal_cellular_connected_no_internet_4_bar_24)
                    .setTitle("No Internet Connection")
                    .setMessage("It seems like you don't have any internet connection.")
                    .setPositiveButton(android.R.string.ok,null)
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
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

    protected void accessingAllProducts() {

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Products").child("JODHPUR");

        FirebaseRecyclerOptions<products> options;
        options = new FirebaseRecyclerOptions.Builder<products>()
                .setQuery(reference.orderByChild("category").equalTo(category_type),products.class).build();

        FirebaseRecyclerAdapter<products, ProductViewHolder> adapter=
                new FirebaseRecyclerAdapter<products, ProductViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ProductViewHolder productViewHolder, int i, @NonNull final products products) {
                        productViewHolder.textProductName.setText(products.getProductName());
                        productViewHolder.textProductDescription.setText(products.getDescription());
                        productViewHolder.textProductPrice.setText("₹" + products.getProductPrice() );

                        if (products.getProductImage().equals("default")){
                            Picasso.get().load(R.drawable.food_for_you).into(productViewHolder.imageView);
                        }else {
                            Picasso.get().load(products.getProductImage()).placeholder(R.drawable.food_for_you).into(productViewHolder.imageView);
                        }

                        productViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent(SearchByCategory.this,ProductDetails.class);
                                intent.putExtra("Pid",products.getProductId());
                                startActivity(intent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
                        ProductViewHolder holder = new ProductViewHolder(view);
                        return holder;
                    }
                };
        recycleView.setAdapter(adapter);
        adapter.startListening();
    }

    private void initializingViews(){

        recycleView=(RecyclerView)findViewById(R.id.search_by_category_recycler_view);
        recycleView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recycleView.setLayoutManager(layoutManager);
    }


}