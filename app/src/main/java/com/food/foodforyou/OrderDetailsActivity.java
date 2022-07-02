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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.food.foodforyou.Model.orderDetails;
import com.food.foodforyou.ViewHolder.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class OrderDetailsActivity extends AppCompatActivity {

    private TextView userName;
    private TextView userAddress;
    private TextView userNumber;
    private TextView utrNo;
    private TextView orderDate;
    private TextView orderTime;
    private TextView orderStatus;
    private TextView amountPaid;
    private TextView deliveryCharge;
    private TextView discount;
    private Button CancelOrderButton;
    private TextView refundTxt;
    private TextView timeOfCancellationTxt;
    private TextView deliveryCode;
    private TextView deliveryType;
    private TextView appliedOfferName;
    private TextView refundNoticeText;

    private String path="";
    private RecyclerView recycleView;
    private RecyclerView.LayoutManager layoutManager;
    private DatabaseReference orderDetailsRef;
    private FirebaseAuth mAuth;

    private ImageView back_arrow_icon;
    private ImageView cart_icon;
    private TextView toolbar_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        path = getIntent().getStringExtra("orderDetailsPath");

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

        toolbar_name.setText("Order Details");

        mAuth= FirebaseAuth.getInstance();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null){
            // check that email is verified or not
//            if (currentUser.isEmailVerified()) {
                Intent intent = new Intent(OrderDetailsActivity.this, LoginActivity.class);
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

        if (isConnectionAvailable(OrderDetailsActivity.this)){
            showOrderDetails();
            showMoreDetails();
        }else {
            new LovelyStandardDialog(OrderDetailsActivity.this)
                    .setTopColorRes(R.color.colorPrimary)
                    .setIcon(R.drawable.ic_baseline_signal_cellular_connected_no_internet_4_bar_24)
                    .setTitle("No Internet Connection")
                    .setMessage("It seems like you don't have any internet connection.")
                    .setPositiveButton(android.R.string.ok,null)
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }

        CancelOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isConnectionAvailable(OrderDetailsActivity.this)){
                    new LovelyStandardDialog(OrderDetailsActivity.this)
                            .setTopColorRes(R.color.colorPrimary)
                            .setIcon(R.drawable.ic_baseline_perm_device_information_24)
                            .setTitle("Do you want to cancel this order")
                            .setMessage("If refund is applicable then it will refunded within 3 working days \n for more information about refund process please read our rules and T&C ")
                            .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cancelOrder();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }else {
                    new LovelyStandardDialog(OrderDetailsActivity.this)
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

    private void cancelOrder(){

        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child(path);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,yyyy");
        String date = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        String time = currentTime.format(calendar.getTime());

        date=date + " " + time;

        HashMap<String,Object> userMAp=new HashMap<>();
        userMAp.put("status","Canceled by user");
        userMAp.put("refund","Pending");
        userMAp.put("timeOfChange",date);

        UsersRef.updateChildren(userMAp).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(OrderDetailsActivity.this, "Ordered Canceled Successfully..", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // showing all the orders that are ordered

    private void showMoreDetails() {

        orderDetailsRef = FirebaseDatabase.getInstance().getReference().child(path).child("details");

        // using orderDetails as model class and cartViewHolder as a viewHolder
        FirebaseRecyclerOptions<orderDetails> options =
                new FirebaseRecyclerOptions.Builder<orderDetails>()
                        .setQuery(orderDetailsRef, orderDetails.class)
                        .build();


        FirebaseRecyclerAdapter<orderDetails, CartViewHolder> adapter =
                new FirebaseRecyclerAdapter<orderDetails, CartViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CartViewHolder holder, int position, @NonNull final orderDetails model)
                    {

                        int oneProductFullPrice=(Integer.valueOf(model.getProductPrice())*Integer.valueOf(model.getQuantity()));

                        holder.txtProductName.setText(model.getProductName());

                        if (model.getSelected_type().equals("Half")){
                            holder.txtProductPrice.setText("₹ " + model.getProductPrice() + " Half");
                        }else {
                            holder.txtProductPrice.setText("₹ " + model.getProductPrice());
                        }
                        holder.txtProductQuantity.setText("x" + model.getQuantity()  + "\n ₹ " + Integer.toString(oneProductFullPrice));
                        holder.txtProductSellerName.setText("restaurant : " + model.getRestaurantName());

                        if (!model.getStatus().equals("Ordered")){
                            holder.txtItemStatus.setText(model.getStatus());
                        }

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                // sending user to product details page
                                Intent intent = new Intent(OrderDetailsActivity.this, ProductDetails.class);
                                intent.putExtra("Pid", model.getProductId());
                                startActivity(intent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_layout, parent, false);
                        CartViewHolder holder = new CartViewHolder(view);
                        return holder;
                    }
                };
        recycleView.setAdapter(adapter);
        adapter.startListening();
    }

    private void showOrderDetails() {

        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child(path);

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    // showing the shipping details
                    userName.setText("Name : " + dataSnapshot.child("name").getValue().toString());
                    userAddress.setText("Address :" + dataSnapshot.child("address").getValue().toString());
                    userNumber.setText("Number : " + dataSnapshot.child("number").getValue().toString());
                    //utrNo.setText("UTR no. : " + dataSnapshot.child("UTR_No").getValue().toString());
                    orderDate.setText("Date : " + dataSnapshot.child("Date").getValue().toString());
                    orderTime.setText("Time : " + dataSnapshot.child("Time").getValue().toString());
                    orderStatus.setText("Status : " + dataSnapshot.child("status").getValue().toString());
                    deliveryCode.setText("Delivery Code : " + dataSnapshot.child("DeliveryCode").getValue().toString());

                    refundNoticeText.setText("In case of refund amount will be refunded on : " + dataSnapshot.child("UPI_ID").getValue().toString() + " upi id");

                    if (!dataSnapshot.child("appliedOfferName").getValue().toString().equals("NO")){
                        appliedOfferName.setVisibility(View.VISIBLE);
                        appliedOfferName.setText("Offer Name : " + dataSnapshot.child("appliedOfferName").getValue().toString());
                    }

                    if (dataSnapshot.child("deliveryType").getValue().toString().equals("instant")){
                        deliveryType.setVisibility(View.VISIBLE);
                        deliveryType.setText("Delivery Type : Instant");
                    }

                    // checking is order is cancelled or not
                    if (dataSnapshot.child("status").getValue().toString().equals("Ordered")){
                        CancelOrderButton.setVisibility(View.VISIBLE);
                    }
//                    else {
//                        refundTxt.setVisibility(View.VISIBLE);
//                        timeOfCancellationTxt.setVisibility(View.VISIBLE);
//                        refundTxt.setText("Refund : " + dataSnapshot.child("refund").getValue().toString());
//                        timeOfCancellationTxt.setText("Time of Cancellation : " + dataSnapshot.child("timeOfChange").getValue().toString());
//                    }

                    // showing refund details
                    if (!dataSnapshot.child("refund").getValue().toString().equals("no")){
                        refundTxt.setVisibility(View.VISIBLE);
                        timeOfCancellationTxt.setVisibility(View.VISIBLE);
                        refundTxt.setText("Refund : " + dataSnapshot.child("refund").getValue().toString());
                        timeOfCancellationTxt.setText("Time of Cancellation : " + dataSnapshot.child("timeOfChange").getValue().toString());
                    }

                    // showing the order details
                    amountPaid.setText("Paid : " + dataSnapshot.child("TotalAmount").getValue().toString());
                    deliveryCharge.setText("Delivery charge : " + dataSnapshot.child("deliveryCharge").getValue().toString());
                    discount.setText("Discount : " + dataSnapshot.child("discount").getValue().toString());

                }else {
                    Toast.makeText(OrderDetailsActivity.this, "no data found to show", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializingViews(){

        userName=(TextView)findViewById(R.id.order_details_user_name);
        userAddress=(TextView)findViewById(R.id.order_details_address);
        userNumber=(TextView)findViewById(R.id.order_details_number);
        utrNo=(TextView)findViewById(R.id.order_details_utr_no);
        orderDate=(TextView)findViewById(R.id.order_details_date);
        orderTime=(TextView)findViewById(R.id.order_details_time);
        orderStatus=(TextView)findViewById(R.id.order_details_status);
        amountPaid=(TextView)findViewById(R.id.order_details_paid_amount);
        deliveryCharge=(TextView)findViewById(R.id.order_details_delivery_charge);
        discount=(TextView)findViewById(R.id.order_details_discount);
        deliveryCode=(TextView)findViewById(R.id.order_details_delivery_code);
        deliveryType=(TextView)findViewById(R.id.order_details_delivery_type);
        appliedOfferName=(TextView)findViewById(R.id.order_details_applied_offer_name);
        refundNoticeText=(TextView)findViewById(R.id.refund_notice_text);

        refundTxt=(TextView)findViewById(R.id.order_details_refund);
        timeOfCancellationTxt=(TextView)findViewById(R.id.order_details_time_of_cancellation);

        CancelOrderButton=(Button)findViewById(R.id.order_details_cancel_order_button);

        recycleView=(RecyclerView)findViewById(R.id.order_details_recycler_view);
        recycleView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recycleView.setLayoutManager(layoutManager);
    }
}