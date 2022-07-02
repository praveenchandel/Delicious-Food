package com.food.foodforyou;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import com.food.foodforyou.Model.CategoriesName;
import com.food.foodforyou.ViewHolder.CategoriesNameViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

public class ShopByCategory extends AppCompatActivity {

    private RecyclerView recycleView;
    private RecyclerView.LayoutManager layoutManager;
    private DatabaseReference CategoriesRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_by_category);

        initializingViews();

        mAuth= FirebaseAuth.getInstance();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null){
            // check that email is verified or not
//            if (currentUser.isEmailVerified()) {
            Intent intent = new Intent(ShopByCategory.this, LoginActivity.class);
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
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isConnectionAvailable(ShopByCategory.this)){

            CategoriesRef = FirebaseDatabase.getInstance().getReference().child("Categories");

            FirebaseRecyclerOptions<CategoriesName> options =
                    new FirebaseRecyclerOptions.Builder<CategoriesName>()
                            .setQuery(CategoriesRef, CategoriesName.class)
                            .build();


            FirebaseRecyclerAdapter<CategoriesName, CategoriesNameViewHolder> adapter =
                    new FirebaseRecyclerAdapter<CategoriesName, CategoriesNameViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull CategoriesNameViewHolder holder, int position, @NonNull final CategoriesName model)
                        {
                            holder.category_name.setText(model.getCategory());

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent intent = new Intent(ShopByCategory.this, SearchByCategory.class);
                                    intent.putExtra("category_name",model.getCategory());
                                    startActivity(intent);
                                }
                            });
                        }

                        @NonNull
                        @Override
                        public CategoriesNameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                        {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shop_by_category_list, parent, false);
                            CategoriesNameViewHolder holder = new CategoriesNameViewHolder(view);
                            return holder;
                        }
                    };
            recycleView.setAdapter(adapter);
            adapter.startListening();

        }else {
            new LovelyStandardDialog(ShopByCategory.this)
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

    private void initializingViews() {
        recycleView=(RecyclerView)findViewById(R.id.shop_by_category_recycler_view);
        recycleView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recycleView.setLayoutManager(layoutManager);
    }

}