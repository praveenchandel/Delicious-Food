package com.food.foodforyou;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.food.foodforyou.Adapter.SimilarProductAdapter;
import com.food.foodforyou.Model.products;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;

public class OffersWithMinimumOff extends AppCompatActivity {

    private DatabaseReference productsRef;
    private ArrayList<products> offerProducts=new ArrayList<products>();
    private String minimumOff="";
    private SimilarProductAdapter listVi;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    private ImageView back_arrow_icon;
    private ImageView cart_icon;
    private TextView toolbar_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers_with_minimum_off);

        minimumOff=getIntent().getStringExtra("minimum_off");
        progressBar=(ProgressBar)findViewById(R.id.offers_with_minimum_off_progress_bar);

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

        if(minimumOff.equals("offers")){
            toolbar_name.setText("Dishes with Offers");
            minimumOff="0";
        }else {
            toolbar_name.setText("Minimum " + minimumOff + "% Off");
        }

        productsRef = FirebaseDatabase.getInstance().getReference().child("Products").child("JODHPUR");

        mAuth= FirebaseAuth.getInstance();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null){
            // check that email is verified or not
//            if (currentUser.isEmailVerified()) {
            Intent intent = new Intent(OffersWithMinimumOff.this, LoginActivity.class);
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

        if (isConnectionAvailable(OffersWithMinimumOff.this)){
            showProducts();
        }else {
            new LovelyStandardDialog(OffersWithMinimumOff.this)
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

    private void showProducts(){

        progressBar.setVisibility(View.VISIBLE);

        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                boolean isRun=false;

                for(DataSnapshot ds : snapshot.getChildren()) {
                    products mProduct=ds.getValue(products.class);

                    isRun=false;

                    if (ds.child("offers").child("offer1").child("status").getValue().toString().equals("YES")){
                        String miniOff=ds.child("offers").child("offer1").child("percentage_off").getValue().toString();
                        if (Integer.parseInt(miniOff)>=Integer.parseInt(minimumOff)){
                            offerProducts.add(mProduct);
                            isRun=true;
                        }
                    }
                    if (ds.child("offers").child("offer2").child("status").getValue().toString().equals("YES")  && !isRun){
                        String miniOff=ds.child("offers").child("offer2").child("percentage_off").getValue().toString();
                        if (Integer.parseInt(miniOff)>=Integer.parseInt(minimumOff)){
                            offerProducts.add(mProduct);
                        }
                    }
                }

                progressBar.setVisibility(View.GONE);

                listVi = new SimilarProductAdapter(OffersWithMinimumOff.this, offerProducts);
                ListView listView = (ListView) findViewById(R.id.offers_with_minimum_off);
                listView.setAdapter(listVi);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getApplicationContext(), ProductDetails.class);
                        intent.putExtra("Pid", offerProducts.get(position).getProductId());
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}