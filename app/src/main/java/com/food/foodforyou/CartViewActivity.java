package com.food.foodforyou;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.food.foodforyou.Adapter.CartViewAdapter;
import com.food.foodforyou.Model.Amount;
import com.food.foodforyou.Model.offers;
import com.food.foodforyou.Model.orderProductDetails;
import com.food.foodforyou.Model.products;
import com.food.foodforyou.Model.restaurants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CartViewActivity extends AppCompatActivity{

    private Button nextProcessState;
    private Button offersButton;

    private TextView totalAmountTxt;
    private TextView discountTxt;
    private TextView totalPriceTxt;

    private int OverAllTotalPrice=0;
    private boolean isAnyProductIsNotAvailable=false;

    private String userID;
    public static String appliedOfferName="NO";
    private CartViewAdapter listVi;
    private ProgressBar progressBar;
    private ProgressDialog loadingBar;

    private ImageView nothingFoundImage;
    private TextView nothingFoundText;

    private String deliveryCharge="0";
    private String couponDiscount="0";

    public static ArrayList<orderProductDetails> allProductInCartList=new ArrayList<orderProductDetails>();
    public static ArrayList<offers> allOffersInCartList=new ArrayList<offers>();

    public static Amount lastAmount=new Amount();
    private ArrayList<String> restaurantNameList=new ArrayList<String>();
    private ArrayList<String> restaurantPriceList=new ArrayList<String>();

    public static Set<String> uniqueGas;
    private HashSet<String> allAvailableRestaurants=new HashSet<String>();

    private DatabaseReference restaurantsRef;
    private DatabaseReference cartOffers;
    private boolean isOffersNotAccessed=true;
    public static boolean isPunjabiChicInn=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_view);

        lastAmount.setTotalDiscount(0);
        lastAmount.setTotalPrice(0);
        lastAmount.setTotalRestaurants(0);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userID= mAuth.getCurrentUser().getUid();

        Toolbar toolbar = findViewById(R.id.toolbar_cart_activity);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);

        initializeViews();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

         mAuth =FirebaseAuth.getInstance();

        FirebaseUser currentUser= mAuth.getCurrentUser();
        if(currentUser==null){

                Intent intent = new Intent(CartViewActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        }

        if (isConnectionAvailable(CartViewActivity.this)){
            accessingAvailableRestaurants();

        }else {
            new LovelyStandardDialog(CartViewActivity.this)
                    .setTopColorRes(R.color.colorPrimary)
                    .setIcon(R.drawable.ic_baseline_signal_cellular_connected_no_internet_4_bar_24)
                    .setTitle("No Internet Connection")
                    .setMessage("It seems like you don't have any internet connection.")
                    .setPositiveButton(android.R.string.ok,null)
                    .show();
            }

        nextProcessState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isAnyProductIsNotAvailable) {

                    // if there is any product that is not available then we will tell user to
                    // remove it from your cart
                    new LovelyStandardDialog(CartViewActivity.this)
                            .setTopColorRes(R.color.colorPrimary)
                            .setIcon(R.drawable.ic_baseline_face_24)
                            .setTitle("Unavailable item found in your cart")
                            .setMessage("There is items in your cart that is not available right now. Please remove them and move ahead.")
                            .setPositiveButton(android.R.string.ok, null)
                            .setNegativeButton(android.R.string.no, null)
                            .show();

                } else {

                    // if all the item are available that is present in cart
                if (isConnectionAvailable(CartViewActivity.this)) {
                    if (OverAllTotalPrice > 0) {
                        uniqueGas = new HashSet<String>(restaurantNameList);

                        // if Punjabi chic Inn restaurant present in cart
                        // we will send n-1 restaurants and add 50 rupees delivery charge extra
                        isPunjabiChicInn = uniqueGas.contains("Punjabi chic Inn");
                        if (isPunjabiChicInn) {
                            lastAmount.setTotalRestaurants(uniqueGas.size() - 1);
                        } else {
                            lastAmount.setTotalRestaurants(uniqueGas.size());
                        }

                        Intent intent = new Intent(CartViewActivity.this, ConfirmOrderActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(CartViewActivity.this, "Cart is empty...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    new LovelyStandardDialog(CartViewActivity.this)
                            .setTopColorRes(R.color.colorPrimary)
                            .setIcon(R.drawable.ic_baseline_signal_cellular_connected_no_internet_4_bar_24)
                            .setTitle("No Internet Connection")
                            .setMessage("It seems like you don't have any internet connection.")
                            .setPositiveButton(android.R.string.ok, null)
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
            }
            }
        });

        offersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isConnectionAvailable(CartViewActivity.this)){

                    // accessing offers from restaurants
                    if (isOffersNotAccessed){
                        accessingOffersFromRestaurants();
                        isOffersNotAccessed=false;
                    }else {
                        Intent intent=new Intent(CartViewActivity.this,OfffersActivity.class);
                        startActivity(intent);
                    }
                }else {
                    new LovelyStandardDialog(CartViewActivity.this)
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


    // accessing all the offers from restaurants
    private void accessingOffersFromRestaurants(){

        progressBar.setVisibility(View.VISIBLE);

        restaurantsRef = FirebaseDatabase.getInstance().getReference().child("Restaurants").child("JODHPUR");

        uniqueGas = new HashSet<String>(restaurantNameList);
        for (final String restaurant : uniqueGas) {

            int amountOfRestaurant=0;

            // calculating the price of all the products in cart of that restaurant
            for (int i=0;i<restaurantNameList.size();i++){
                if (restaurantNameList.get(i).equals(restaurant)){
                    amountOfRestaurant=amountOfRestaurant + Integer.parseInt(restaurantPriceList.get(i));
                }
            }

            final int finalAmountOfRestaurant = amountOfRestaurant;
            restaurantsRef.child(restaurant)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                offers mRestaurantOffers1=new offers(snapshot.child("offers").child("offer1").getValue(offers.class));
                                mRestaurantOffers1.setProductName("Restaurant : " + restaurant);

                                offers mRestaurantOffers2=new offers(snapshot.child("offers").child("offer2").getValue(offers.class));
                                mRestaurantOffers2.setProductName("Restaurant : " + restaurant);

                                mRestaurantOffers1.setProduct_price(String.valueOf(finalAmountOfRestaurant));
                                mRestaurantOffers2.setProduct_price(String.valueOf(finalAmountOfRestaurant));
                                mRestaurantOffers1.setProduct_quantity(String.valueOf(finalAmountOfRestaurant));
                                mRestaurantOffers2.setProduct_quantity(String.valueOf(finalAmountOfRestaurant));

                                if (mRestaurantOffers1.getStatus().equals("YES")){
                                    allOffersInCartList.add(mRestaurantOffers1);
                                }
                                if (mRestaurantOffers2.getStatus().equals("YES")){
                                    allOffersInCartList.add(mRestaurantOffers2);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }

        // accessing all offers from cart
        cartOffers=FirebaseDatabase.getInstance().getReference().child("CartOffers").child("JODHPUR").child("offers");

        cartOffers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                offers mCartOffers1=new offers(snapshot.child("offer1").getValue(offers.class));
                mCartOffers1.setProductName("Discount on Total Purchase");

                offers mCartOffers2=new offers(snapshot.child("offer2").getValue(offers.class));
                mCartOffers2.setProductName("Discount on Total Purchase" );

                offers mCartOffers3=new offers(snapshot.child("offer3").getValue(offers.class));
                mCartOffers3.setProductName("Discount on Total Purchase");

                offers mCartOffers4=new offers(snapshot.child("offer4").getValue(offers.class));
                mCartOffers4.setProductName("Discount on Total Purchase" );

                mCartOffers1.setProduct_price(String.valueOf(lastAmount.getTotalPrice()));
                mCartOffers2.setProduct_price(String.valueOf(lastAmount.getTotalPrice()));
                mCartOffers3.setProduct_price(String.valueOf(lastAmount.getTotalPrice()));
                mCartOffers4.setProduct_price(String.valueOf(lastAmount.getTotalPrice()));

                mCartOffers1.setProduct_quantity(String.valueOf(lastAmount.getTotalPrice()));
                mCartOffers2.setProduct_quantity(String.valueOf(lastAmount.getTotalPrice()));
                mCartOffers3.setProduct_quantity(String.valueOf(lastAmount.getTotalPrice()));
                mCartOffers4.setProduct_quantity(String.valueOf(lastAmount.getTotalPrice()));

                if (mCartOffers1.getStatus().equals("YES")){
                    allOffersInCartList.add(mCartOffers1);
                }
                if (mCartOffers2.getStatus().equals("YES")){
                    allOffersInCartList.add(mCartOffers2);
                }
                if (mCartOffers3.getStatus().equals("YES")){
                    allOffersInCartList.add(mCartOffers3);
                }
                if (mCartOffers4.getStatus().equals("YES")){
                    allOffersInCartList.add(mCartOffers4);
                }

                progressBar.setVisibility(View.GONE);
                Intent intent=new Intent(CartViewActivity.this,OfffersActivity.class);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void accessingAvailableRestaurants(){

        progressBar.setVisibility(View.VISIBLE);

        restaurantsRef = FirebaseDatabase.getInstance().getReference().child("Restaurants").child("JODHPUR");

        restaurantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                if(snapshot.exists()){

                    for (final DataSnapshot ds : snapshot.getChildren()){

                        restaurants rest=ds.getValue(restaurants.class);

                        assert rest != null;
                        if(rest.getStatus()==8){
                            allAvailableRestaurants.add(rest.getRestaurant_name());
                        }
                    }

                    AccessingAllProductsThatAreInCart();
                }else{

                    // if nothing in cart
                    nothingFoundImage.setVisibility(View.VISIBLE);
                    nothingFoundText.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void AccessingAllProductsThatAreInCart(){

        //progressBar.setVisibility(View.VISIBLE);

        final DatabaseReference cartListRef= FirebaseDatabase.getInstance().getReference().child("Cart List");

        // clearing the list, bewaring from adding multiple time
        allOffersInCartList.clear();
        allProductInCartList.clear();
        OverAllTotalPrice=0;

        cartListRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()){
                    // if nothing in cart
                    nothingFoundImage.setVisibility(View.VISIBLE);
                    nothingFoundText.setVisibility(View.VISIBLE);
                }

                for (final DataSnapshot ds : snapshot.getChildren()) {

                    final DatabaseReference productRef= FirebaseDatabase.getInstance().getReference().child("Products").child("JODHPUR");

                    productRef.child(Objects.requireNonNull(ds.getKey())).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                boolean isHaveToConsider=true;
                                boolean isHalfHasToTook=false;

                                // accessing the offer details
                                products mProduct=snapshot.getValue(products.class);

                                // accessing that is there any product that is not available
                                if(mProduct.getStatus().equals("unavailable")){
                                    isAnyProductIsNotAvailable=true;
                                    mProduct.setStatus("Currently Unavailable");
                                }else{
                                    mProduct.setStatus("");
                                }

                                // if the restaurant is not delivering

                                if(!allAvailableRestaurants.contains(mProduct.getRestaurantName())){
                                    isAnyProductIsNotAvailable=true;
                                    mProduct.setStatus("Currently Unavailable");
                                }

                                offers mProductOffers1=new offers(snapshot.child("offers").child("offer1").getValue(offers.class));
                                mProductOffers1.setProductName(mProduct.getProductName());

                                offers mProductOffers2=new offers(snapshot.child("offers").child("offer2").getValue(offers.class));
                                mProductOffers2.setProductName(mProduct.getProductName());

                                int oneProductFullPrice=0;
                                // if user selected the half product
                                //if (ds.child("selected_type").getValue().toString().equals("Half")){

                                // if user select half plate of the product
                                if (ds.child("half_quantity").exists() && Integer.parseInt(ds.child("half_quantity").getValue().toString())>0) {

                                    mProduct.setSelected_type("Half");
                                    isHalfHasToTook=true;

                                    // if the admin removes the half price of the product then we
                                    // simply remove the product from the user cart to avoid the crashes
                                    // and finish the activity for smooth working of price and offers
                                    if (mProduct.getHalf_price().equals("no")) {

                                        isHaveToConsider = false;

                                        loadingBar.setTitle("Find a problem");
                                        loadingBar.setMessage("Please wait while we are resolving the problem");
                                        loadingBar.setCanceledOnTouchOutside(false);
                                        loadingBar.show();

                                        cartListRef.child(userID).child(ds.getKey()).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            loadingBar.dismiss();
                                                            Toast.makeText(CartViewActivity.this, "Problem solved ...", Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        } else {
                                                            loadingBar.dismiss();
                                                            Toast.makeText(CartViewActivity.this, "failed to perform operation right now, please try again later...", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                loadingBar.dismiss();
                                            }
                                        });
                                    } else {
                                        oneProductFullPrice = (Integer.parseInt(mProduct.getHalf_price()) * Integer.parseInt(ds.child("half_quantity").getValue().toString()));
                                        //cartViewHolder.txtProductPrice.setText("₹ " + mProduct.getHalf_price() + " half");
                                        mProductOffers1.setProduct_price(mProduct.getHalf_price());
                                        mProductOffers2.setProduct_price(mProduct.getHalf_price());
                                    }
//

                                    restaurantNameList.add(mProduct.getRestaurantName());
                                    restaurantPriceList.add(String.valueOf(oneProductFullPrice));

                                    OverAllTotalPrice = OverAllTotalPrice + oneProductFullPrice;
                                    lastAmount.setTotalPrice(OverAllTotalPrice);

                                    //   updatePriceDetails();

                                    mProductOffers1.setProduct_quantity(ds.child("half_quantity").getValue().toString());
                                    mProductOffers2.setProduct_quantity(ds.child("half_quantity").getValue().toString());


//                                    if (mProductOffers1.getStatus().equals("YES")) {
//                                        allOffersInCartList.add(mProductOffers1);
//                                    }
//                                    if (mProductOffers2.getStatus().equals("YES")) {
//                                        allOffersInCartList.add(mProductOffers2);
//                                    }

//                                cartViewHolder.txtProductQuantity.setText("x" + cart.getQuantity() + "\n ₹ " + oneProductFullPrice);
//                                cartViewHolder.txtProductName.setText( mProduct.getProductName());
//                                cartViewHolder.txtProductSellerName.setText("restaurant : " + mProduct.getRestaurantName());

                                    orderProductDetails userCart = new orderProductDetails(mProduct, ds.child("half_quantity").getValue().toString());

                                    if (isHaveToConsider) {
                                        allProductInCartList.add(userCart);
                                    }
                                }

                                // adding product in list again if there is full price product is also there

                                // accessing the offer details
                                products mProduct1=mProduct;
                                offers mProductOffers11=new offers(snapshot.child("offers").child("offer1").getValue(offers.class));
                                mProductOffers11.setProductName(mProduct.getProductName());

                                offers mProductOffers21=new offers(snapshot.child("offers").child("offer2").getValue(offers.class));
                                mProductOffers21.setProductName(mProduct.getProductName());

                                int oneProductFullPrice1=0;

                                if (ds.child("full_quantity").exists() && Integer.parseInt(ds.child("full_quantity").getValue().toString())>0) {

                                    mProduct1.setSelected_type("NO");

                                    oneProductFullPrice1 = (Integer.parseInt(mProduct.getProductPrice()) * Integer.parseInt(ds.child("full_quantity").getValue().toString()));
                                    //cartViewHolder.txtProductPrice.setText("₹ " + mProduct.getHalf_price() + " half");
                                    mProductOffers11.setProduct_price(mProduct.getProductPrice());
                                    mProductOffers21.setProduct_price(mProduct.getProductPrice());

                                    restaurantNameList.add(mProduct1.getRestaurantName());
                                    restaurantPriceList.add(String.valueOf(oneProductFullPrice1));

                                    OverAllTotalPrice = OverAllTotalPrice + oneProductFullPrice1;
                                    lastAmount.setTotalPrice(OverAllTotalPrice);

                                    //   updatePriceDetails();

                                    mProductOffers11.setProduct_quantity(ds.child("full_quantity").getValue().toString());
                                    mProductOffers21.setProduct_quantity(ds.child("full_quantity").getValue().toString());

                                    // if user took half and full both plates
                                    if (isHalfHasToTook) {
                                        mProductOffers1.setProduct_quantity(String.valueOf(Integer.parseInt(ds.child("half_quantity").getValue().toString())+Integer.parseInt(ds.child("full_quantity").getValue().toString())));
                                        mProductOffers2.setProduct_quantity(String.valueOf(Integer.parseInt(ds.child("half_quantity").getValue().toString())+Integer.parseInt(ds.child("full_quantity").getValue().toString())));

                                        mProductOffers1.setProduct_price(mProduct.getProductPrice());
                                        mProductOffers2.setProduct_price(mProduct.getProductPrice());
                                        if (mProductOffers1.getStatus().equals("YES")) {
                                            allOffersInCartList.add(mProductOffers1);
                                        }
                                        if (mProductOffers2.getStatus().equals("YES")) {
                                            allOffersInCartList.add(mProductOffers2);
                                        }
                                    }else{

                                        // if user took only full plate
                                        if (mProductOffers11.getStatus().equals("YES")) {
                                            allOffersInCartList.add(mProductOffers11);
                                        }
                                        if (mProductOffers21.getStatus().equals("YES")) {
                                            allOffersInCartList.add(mProductOffers21);
                                        }
                                    }

//                                cartViewHolder.txtProductQuantity.setText("x" + cart.getQuantity() + "\n ₹ " + oneProductFullPrice);
//                                cartViewHolder.txtProductName.setText( mProduct.getProductName());
//                                cartViewHolder.txtProductSellerName.setText("restaurant : " + mProduct.getRestaurantName());

                                    orderProductDetails userCart1 = new orderProductDetails(mProduct1, ds.child("full_quantity").getValue().toString());

                                    allProductInCartList.add(userCart1);
                                }else {

                                    // if user took only half plate product
                                    if(isHalfHasToTook){
                                        if (mProductOffers1.getStatus().equals("YES")) {
                                            allOffersInCartList.add(mProductOffers1);
                                        }
                                        if (mProductOffers2.getStatus().equals("YES")) {
                                            allOffersInCartList.add(mProductOffers2);
                                        }
                                    }
                                }

                                listVi = new CartViewAdapter(CartViewActivity.this, allProductInCartList);
                                ListView listView = (ListView) findViewById(R.id.cart_list);
                                listView.setAdapter(listVi);

                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                                        CharSequence options[]=new CharSequence[] {
                                                "EDIT","REMOVE"
                                        };
                                        AlertDialog.Builder builder=new AlertDialog.Builder(CartViewActivity.this);
                                        builder.setTitle("Card Options");

                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (which == 0)
                                                {
                                                    // finishing this activity here because total price problem
                                                    Intent intent = new Intent(CartViewActivity.this, ProductDetails.class);
                                                    intent.putExtra("Pid", allProductInCartList.get(position).userCart.getProductId());
                                                    startActivity(intent);
                                                    finish();
                                                }
                                                if(which==1){
                                                    // then check that is full is available if yes then remove half only
                                                    // if full is not available then remove the node
                                                    // also keep in mind array index out of bound exception

                                                    if(allProductInCartList.get(position).userCart.getSelected_type().equals("Half")){

                                                        // keeping in mind that we don't get the array index out of bound exception
                                                        if(allProductInCartList.size()>position+1){

                                                            // checking that the product name is same or not
                                                            // if it is same it means there is full plate is also in cart
                                                            // so in this case we will only remove the half plate from the cart
                                                            if(allProductInCartList.get(position+1).userCart.getProductName().equals(allProductInCartList.get(position).userCart.getProductName())){

                                                                cartListRef.child(userID)
                                                                        .child(allProductInCartList.get(position).userCart.getProductId()).child("half_quantity").removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){
                                                                                    Toast.makeText(CartViewActivity.this, "Item removed successfully...", Toast.LENGTH_SHORT).show();
                                                                                    Intent intent=new Intent(CartViewActivity.this,CartViewActivity.class);
                                                                                    startActivity(intent);
                                                                                    finish();
                                                                                }
                                                                            }
                                                                        });

                                                            }else{

                                                                // other wise remove the node
                                                                cartListRef.child(userID)
                                                                        .child(allProductInCartList.get(position).userCart.getProductId()).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){
                                                                                    Toast.makeText(CartViewActivity.this, "Item removed successfully...", Toast.LENGTH_SHORT).show();
                                                                                    Intent intent=new Intent(CartViewActivity.this,CartViewActivity.class);
                                                                                    startActivity(intent);
                                                                                    finish();
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }else{

                                                            // other wise remove the node
                                                            cartListRef.child(userID)
                                                                    .child(allProductInCartList.get(position).userCart.getProductId()).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                Toast.makeText(CartViewActivity.this, "Item removed successfully...", Toast.LENGTH_SHORT).show();
                                                                                Intent intent=new Intent(CartViewActivity.this,CartViewActivity.class);
                                                                                startActivity(intent);
                                                                                finish();
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }

                                                    // then check that is full is available if yes then remove half only
                                                    // if full is not available then remove the node
                                                    // also keep in mind array index out of bound exception
                                                    if(allProductInCartList.get(position).userCart.getSelected_type().equals("NO")){

                                                        // we will check that the item is not the first node of the list
                                                        // if yes then we will remove the node

                                                        if(position>0){

                                                            // now we will check that the product name is same or not
                                                            if(allProductInCartList.get(position).userCart.getProductName().equals(allProductInCartList.get(position-1).userCart.getProductName())){

                                                                // if there is half plate in the cart then we will only remove the full plate

                                                                cartListRef.child(userID)
                                                                        .child(allProductInCartList.get(position).userCart.getProductId()).child("full_quantity").removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){
                                                                                    Toast.makeText(CartViewActivity.this, "Item removed successfully...", Toast.LENGTH_SHORT).show();
                                                                                    Intent intent=new Intent(CartViewActivity.this,CartViewActivity.class);
                                                                                    startActivity(intent);
                                                                                    finish();
                                                                                }
                                                                            }
                                                                        });
                                                            }else{
                                                                // other wise remove the node
                                                                cartListRef.child(userID)
                                                                        .child(allProductInCartList.get(position).userCart.getProductId()).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){
                                                                                    Toast.makeText(CartViewActivity.this, "Item removed successfully...", Toast.LENGTH_SHORT).show();
                                                                                    Intent intent=new Intent(CartViewActivity.this,CartViewActivity.class);
                                                                                    startActivity(intent);
                                                                                    finish();
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        }else{
                                                            // other wise remove the node
                                                            cartListRef.child(userID)
                                                                    .child(allProductInCartList.get(position).userCart.getProductId()).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                Toast.makeText(CartViewActivity.this, "Item removed successfully...", Toast.LENGTH_SHORT).show();
                                                                                Intent intent=new Intent(CartViewActivity.this,CartViewActivity.class);
                                                                                startActivity(intent);
                                                                                finish();
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                        builder.show();
                                    }
                                });

                                updatePriceDetails();

                            }else {
                                Toast.makeText(CartViewActivity.this, "It seems like item removed", Toast.LENGTH_SHORT).show();
                            }

                            }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        updatePriceDetails();
    }


    private void updatePriceDetails(){
        totalPriceTxt.setText("₹" + String.valueOf(lastAmount.getTotalPrice()));
        discountTxt.setText("-₹" + String.valueOf(lastAmount.getTotalDiscount()));
        int m=lastAmount.getTotalPrice() - lastAmount.getTotalDiscount();
        totalAmountTxt.setText("₹" + String.valueOf(m));
    }

    private void initializeViews(){

        nextProcessState=(Button)findViewById(R.id.next_btn);
        offersButton=(Button) findViewById(R.id.cart_view_offers_text);

        totalPriceTxt=(TextView)findViewById(R.id.cart_view_total);
        totalAmountTxt=(TextView)findViewById(R.id.cart_view_total_price);
        discountTxt=(TextView)findViewById(R.id.cart_view_discount);

        nothingFoundImage=(ImageView)findViewById(R.id.cart_activity_nothing_found_image);
        nothingFoundText=(TextView)findViewById(R.id.cart_activity_nothing_found_text);

        progressBar=(ProgressBar)findViewById(R.id.cart_list_view_progress_bar);

        loadingBar=new ProgressDialog(this);

    }
}