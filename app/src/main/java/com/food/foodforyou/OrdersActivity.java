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
import android.widget.ImageView;
import android.widget.TextView;

import com.food.foodforyou.Model.orderHistory;
import com.food.foodforyou.ViewHolder.orderHistoryViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recycleView;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseAuth mAuth;
    private String userID;

    private ImageView nothingFoundImage;
    private TextView nothingFoundText;

    private ImageView back_arrow_icon;
    private ImageView cart_icon;
    private TextView toolbar_name;

    private boolean isAnyThingFound=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        initializingViews();

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

        toolbar_name.setText("My Orders");

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(OrdersActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        if (isConnectionAvailable(OrdersActivity.this)) {
            accessOrders();
        } else {
            new LovelyStandardDialog(OrdersActivity.this)
                    .setTopColorRes(R.color.colorPrimary)
                    .setIcon(R.drawable.ic_baseline_signal_cellular_connected_no_internet_4_bar_24)
                    .setTitle("No Internet Connection")
                    .setMessage("It seems like you don't have any internet connection.")
                    .setPositiveButton(android.R.string.ok, null)
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

    private void accessOrders() {

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("Orders");

        FirebaseRecyclerOptions<orderHistory> options =
                new FirebaseRecyclerOptions.Builder<orderHistory>()
                        .setQuery(cartListRef, orderHistory.class)
                        .build();

        final FirebaseRecyclerAdapter<orderHistory, orderHistoryViewHolder> adapter =
                new FirebaseRecyclerAdapter<orderHistory, orderHistoryViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull orderHistoryViewHolder holder, int position, @NonNull final orderHistory model) {
                        holder.txtCustomerName.setText(model.getName());
                        holder.txtTotalPrice.setText("₹ " + model.getAmount());
                        holder.txtSoldDate.setText(model.getDate());

                        nothingFoundImage.setVisibility(View.GONE);
                        nothingFoundText.setVisibility(View.GONE);

                        holder.layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(OrdersActivity.this, OrderDetailsActivity.class);
                                intent.putExtra("orderDetailsPath", "Orders/" + model.getYear() + "/Months/" + model.getMonth() + "/Days/" + model.getDate() + "/Orders/" + model.getNodeID());
                                startActivity(intent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public orderHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_layout, parent, false);
                        orderHistoryViewHolder holder = new orderHistoryViewHolder(view);
                        return holder;
                    }

                    @Override
                    public int getItemCount() {
                        return super.getItemCount();
                    }
                };

        recycleView.setAdapter(adapter);
        adapter.startListening();
    }

    private void initializingViews() {
        recycleView = (RecyclerView) findViewById(R.id.orders_recyclerView);
        recycleView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycleView.setLayoutManager(layoutManager);

        nothingFoundImage = (ImageView) findViewById(R.id.orders_activity_nothing_found_image);
        nothingFoundText = (TextView) findViewById(R.id.orders_activity_nothing_found_text);
    }

}
