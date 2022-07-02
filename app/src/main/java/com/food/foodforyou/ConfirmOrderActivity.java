package com.food.foodforyou;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.food.foodforyou.Model.AvailableAddresses;
import com.food.foodforyou.Model.orderProductDetails;
import com.food.foodforyou.paytm.ServiceWrapper;
import com.food.foodforyou.paytm.Token_Res;
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
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.paytm.pgsdk.TransactionManager;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmOrderActivity extends AppCompatActivity implements PaymentResultListener {

    private EditText userName;
    private EditText userNumber;
    private TextView addressTxt;
    private Button confirmFinalOrder;
    private TextView priceTxt;
    private TextView discountTxt;
    private TextView deliveryChargeTxt;
    private TextView totalAmountTxt;
    private SwitchCompat extraSwitchCompact;
    private TextView standardDeliveryDescription;
    private TextView instantDeliveryDescription;
    private TextView instantDeliveryPrice;
    private TextView acceptingOrdersNotice;
    private EditText addressLineOne;
    private ArrayAdapter<CharSequence> adapter;
    private String totalAmount="";
   // private String shipmentAddress="";
    private FirebaseAuth mAuth;
    private String userID;
    private String predefineAddress="";
    private String predefineAddressID="";
    private int totalAmountToBePaid=0;
    private ProgressDialog loadingBar;

    private String nodeYear="";
    private String nodeMonth="";
    private String nodeDate="";
    private String saveCurrentTime;
    private String userUPIId="";
    private DatabaseReference ordersReference;
    final int UPI_PAYMENT = 0;
    private AvailableAddresses addressDetails;

    private long backPressTime=0;
    private Toast backToast;

    private String TAG ="ConfirmOrderActivity";
    private String userEmail="";
    private String userPhoneNumber="";

    // for paytm integration
    private ProgressBar progressBar;
    //private final String midString ="jUgpya83544587380018";
    private final String midString ="Uzprse66223934407808";
    private String txnAmountString="";
    private String orderIdString="praveenchandel1233";
    private String txnTokenString="";
    private final Integer ActivityRequestCode = 2;
    private String key="";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        mAuth= FirebaseAuth.getInstance();
        userID= mAuth.getCurrentUser().getUid();

        initializingViews();

        ordersReference= FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar_confirm_activity);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mAuth=FirebaseAuth.getInstance();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null){
            Intent intent = new Intent(ConfirmOrderActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        if (isConnectionAvailable(ConfirmOrderActivity.this)){
            displayUserInformation();
        }else {
            new LovelyStandardDialog(ConfirmOrderActivity.this)
                    .setTopColorRes(R.color.colorPrimary)
                    .setIcon(R.drawable.ic_baseline_signal_cellular_connected_no_internet_4_bar_24)
                    .setTitle("No Internet Connection")
                    .setMessage("It seems like you don't have any internet connection.")
                    .setPositiveButton(android.R.string.ok,null)
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }

        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,yyyy");
        Calendar calendar = Calendar.getInstance();
        String saveCurrentDate = currentDate.format(calendar.getTime());
        Log.e("date",saveCurrentDate);

        confirmFinalOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check();
            }
        });

        extraSwitchCompact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // update price details
                setDeliveryCharges();
            }
        });
    }
    
    // this will check all the details for the order
    private void check(){
        if(TextUtils.isEmpty(userName.getText().toString())){
            userName.setError("Name is required ");
            userName.requestFocus();
            return;
        }else if(TextUtils.isEmpty(userNumber.getText().toString())){
            userNumber.setError("number is required ");
            userNumber.requestFocus();
            return;
        }else if (userNumber.getText().toString().length()!=10){
            userNumber.setError("number must have 10 digits");
            userNumber.requestFocus();
            return;
        }else if (TextUtils.isEmpty(addressLineOne.getText().toString())){
            addressLineOne.setError("Please fill address");
            addressLineOne.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(predefineAddressID)){
            return;
        }
        generatePath();
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

    // generate the path for the database
    private void generatePath(){

        // we need unique order id every time so here we are generating unique order id
        key= ordersReference.child("Orders").child(nodeYear).child("Months").child(nodeMonth)
                .child("Days").child(nodeDate).child("Orders").push().getKey();

        Calendar calendar = Calendar.getInstance();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,yyyy");
        nodeDate = currentDate.format(calendar.getTime());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate1=new SimpleDateFormat("MMM,yyyy");
        nodeMonth = currentDate1.format(calendar.getTime());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate2=new SimpleDateFormat("yyyy");
        nodeYear = currentDate2.format(calendar.getTime());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calendar.getTime());



        if (isConnectionAvailable(ConfirmOrderActivity.this)){

         // getToken();  // taking payments from pay tm

         payUsingUpi();  // taking payments from google pay

        //  startPayment();  // taking payments from razor pay

          //  placeOrder("transaction");
        }else {
           Toast.makeText(ConfirmOrderActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
       }
       }

       
       
       
       ///////////////////////  We are using google pay now ///////////////////////////


    void payUsingUpi() {

        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", "8769564547-1@okbizaxis")
                //.appendQueryParameter("pa", "8824524101@okbizaxis")
                .appendQueryParameter("pn", "Delicious Food")
                //.appendQueryParameter("mc", "Eating Places and Restaurants")
                //.appendQueryParameter("tid", "02125412")
                .appendQueryParameter("tr", "584584")
                .appendQueryParameter("tn", "Delicious Food")
                .appendQueryParameter("am", totalAmount)
                .appendQueryParameter("cu", "INR")
                .build();

        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

        // check if intent resolves
        if(null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(ConfirmOrderActivity.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("main ", "response "+resultCode );

         /*
       E/main: response -1
       E/UPI: onActivityResult: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
       E/UPIPAY: upiPaymentDataOperation: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
       E/UPI: payment successfull: 922118921612
       */

        if (requestCode == UPI_PAYMENT) {
            if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                if (data != null) {
                    String trxt = data.getStringExtra("response");
                    Log.e("UPI", "onActivityResult: " + trxt);
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add(trxt);
                    upiPaymentDataOperation(dataList);
                } else {
                    Log.e("UPI", "onActivityResult: " + "Return data is null");
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
            } else {
                //when user simply back without payment
                Log.e("UPI", "onActivityResult: " + "Return data is null");
                ArrayList<String> dataList = new ArrayList<>();
                dataList.add("nothing");
                upiPaymentDataOperation(dataList);
            }
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(ConfirmOrderActivity.this)) {
            String str = data.get(0);
            String finalResponseForServer=data.toString();
            Log.e("UPIPAY", "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }
            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Toast.makeText(ConfirmOrderActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "payment successfull: "+approvalRefNo);
                // TODO: send the utr no to the database
                placeOrder(finalResponseForServer);
                //transationID.setText("app. no.= " + approvalRefNo  + "\n  refNo.");
            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(ConfirmOrderActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "Cancelled by user: "+approvalRefNo);

            }
            else {
                Toast.makeText(ConfirmOrderActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "failed payment: "+approvalRefNo);
            }
        } else {
            Log.e("UPI", "Internet issue: ");

            Toast.makeText(ConfirmOrderActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }



    /////////////////////////////// till now the code is for google pay payment //////////////////////////////



    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable();
        }
        return false;
    }

    // taking payment form rozar pay

    public void startPayment() {
        /**
         * You need to pass current activity in order to let Razorpay create CheckoutActivity
         */
        final Activity activity = this;

        // initialize razor pay checkout
        final Checkout co = new Checkout();

        // set key id
         co.setKeyID("rzp_live_3p9WONXLCgoiP6");
         // setting image
        co.setImage(R.drawable.food_for_you_razor);

        try {

            // initialize json object
            JSONObject options = new JSONObject();
            options.put("name", "Food For You");
            options.put("description", "Mayank Rathor");
            //You can omit the image option to fetch the image from dashboard
            //options.put("image", "https://rzp-mobile.s3.amazonaws.com/images/rzp.png");
            options.put("currency", "INR");
            String payment = totalAmount;

            // amount is in paise so please multiple it by 100
            //Payment failed Invalid amount (should be passed in integer paise. Minimum value is 100 paise, i.e. ₹ 1)
            double total = Double.parseDouble(payment);
            total = total * 100;
            options.put("amount", total);
            JSONObject preFill = new JSONObject();
            preFill.put("email", userEmail);
            preFill.put("contact", userPhoneNumber);
            options.put("prefill", preFill);
            co.open(activity, options);
        } catch (Exception e) {
            Toast.makeText(activity, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    @Override
    public void onPaymentSuccess(String s) {
        // payment successfull pay_DGU19rDsInjcF2
        Log.e(TAG, " payment successfull "+ s.toString());
        Toast.makeText(this, "Payment successfully done! " +s, Toast.LENGTH_SHORT).show();
        placeOrder(s);
    }
    @Override
    public void onPaymentError(int i, String s) {
        Log.e(TAG,  "error code "+String.valueOf(i)+" -- Payment failed "+s.toString()  );
        try {
            Toast.makeText(this, "Payment error please try again", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("OnPaymentError", "Exception in onPaymentError", e);
        }
    }

    /////////////////////      till now the code is to take the payments from the razor pay  ////////////////////


    ////////////////////  code to taking payments from paytm //////////////////////////////////


//    private  void getToken(){
//        Log.e(TAG, " get token start");
//        progressBar.setVisibility(View.VISIBLE);
//
//        // check sum generation server
//        // 000webhost
//
//        orderIdString=key;
//
//        ServiceWrapper serviceWrapper = new ServiceWrapper(null);
//        Call<Token_Res> call = serviceWrapper.getTokenCall("12345", midString, orderIdString, totalAmount);
//        call.enqueue(new Callback<Token_Res>() {
//            @Override
//            public void onResponse(Call<Token_Res> call, Response<Token_Res> response) {
//                Log.e(TAG, " respo "+ response.isSuccessful() + response );
//                progressBar.setVisibility(View.GONE);
//                try {
//                    if (response.isSuccessful() && response.body()!=null){
//                        if (!response.body().getBody().getTxnToken().equals("")) {
//                            Log.e(TAG, " transaction token : "+response.body().getBody().getTxnToken());
//
//                            if (response.body().getBody().getTxnToken()==null){
//                                Toast.makeText(ConfirmOrderActivity.this, "getting token null, enable to pay right now, please try again later...", Toast.LENGTH_SHORT).show();
//                            }else {
//                                startPaytmPayment(response.body().getBody().getTxnToken());
//                            }
//
//                        }else {
//                            Log.e(TAG, " Token status false");
//                        }
//                    }
//                }catch (Exception e){
//                    Log.e(TAG, " error in Token Res "+e.toString());
//                }
//            }
//            @Override
//            public void onFailure(Call<Token_Res> call, Throwable t) {
//                progressBar.setVisibility(View.GONE);
//                Log.e(TAG, " response error "+t.toString());
//            }
//        });
//    }
//
//    public void startPaytmPayment (String token){
//
//        txnTokenString = token;
//        // for test mode use it
//        // String host = "https://securegw-stage.paytm.in/";
//        // for production mode use it
//        String host = "https://securegw.paytm.in/";
//        String orderDetails = "MID: " + midString + ", OrderId: " + orderIdString + ", TxnToken: " + txnTokenString
//                + ", Amount: " + totalAmount;
//        //Log.e(TAG, "order details "+ orderDetails);
//        String callBackUrl = host + "theia/paytmCallback?ORDER_ID="+orderIdString;
//
//       // https://securegw.paytm.in/theia/paytmCallback?ORDER_ID=
//        Log.e(TAG, " callback URL "+callBackUrl);
//        PaytmOrder paytmOrder = new PaytmOrder(orderIdString, midString, txnTokenString, totalAmount, callBackUrl);
//        TransactionManager transactionManager = new TransactionManager(paytmOrder, new PaytmPaymentTransactionCallback(){
//            @Override
//            public void onTransactionResponse(Bundle bundle) {
//                Log.e(TAG, "transaction from web Response (onTransactionResponse) : "+bundle.toString());
//
//                if(bundle.getString("STATUS").equals("TXN_SUCCESS")){
//                    placeOrder(bundle.toString());
//                    Toast.makeText(ConfirmOrderActivity.this, "Transaction successful..", Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(ConfirmOrderActivity.this, "Transaction unsuccessful, please try again ...", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void networkNotAvailable() {
//                Log.e(TAG, "network not available ");
//                Toast.makeText(ConfirmOrderActivity.this, "network not available ", Toast.LENGTH_SHORT).show();
//            }
//            @Override
//            public void onErrorProceed(String s) {
//                Log.e(TAG, " onErrorProcess "+s.toString());
//                Toast.makeText(ConfirmOrderActivity.this, " onErrorProcess ", Toast.LENGTH_SHORT).show();
//            }
//            @Override
//            public void clientAuthenticationFailed(String s) {
//                Log.e(TAG, "Clientauth "+s);
//                Toast.makeText(ConfirmOrderActivity.this, "Client auth "+s, Toast.LENGTH_SHORT).show();
//            }
//            @Override
//            public void someUIErrorOccurred(String s) {
//                Log.e(TAG, " UI error "+s);
//                Toast.makeText(ConfirmOrderActivity.this, " UI error "+s, Toast.LENGTH_SHORT).show();
//            }
//            @Override
//            public void onErrorLoadingWebPage(int i, String s, String s1) {
//                Log.e(TAG, " error loading web "+s+"--"+s1);
//                Toast.makeText(ConfirmOrderActivity.this, " error loading web "+s+"--"+s1, Toast.LENGTH_SHORT).show();
//            }
//            @Override
//            public void onBackPressedCancelTransaction() {
//                Log.e(TAG, "backPress ");
//                Toast.makeText(ConfirmOrderActivity.this, " Transaction cancelled by user ", Toast.LENGTH_SHORT).show();
//            }
//            @Override
//            public void onTransactionCancel(String s, Bundle bundle) {
//                Log.e(TAG, " transaction cancel "+s);
//                Toast.makeText(ConfirmOrderActivity.this, " transaction cancel "+s, Toast.LENGTH_SHORT).show();
//            }
//        });
//        transactionManager.setShowPaymentUrl(host + "theia/api/v1/showPaymentPage");
//        transactionManager.startTransaction(this, ActivityRequestCode);
//    }
//
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == ActivityRequestCode && data != null) {
//
//            Log.e(TAG, "transaction from App Response (onTransactionResponse) : " + data.getStringExtra("response"));
//
//            // data is something like
//            // data response - {"BANKNAME":"WALLET","BANKTXNID":"1395841115",
//// "CHECKSUMHASH":"7jRCFIk6mrep+IhnmQrlrL43KSCSXrmM+VHP5pH0hekXaaxjt3MEgd1N9mLtWyu4VwpWexHOILCTAhybOo5EVDmAEV33rg2VAS/p0PXdk\u003d",
//// "CURRENCY":"INR","GATEWAYNAME":"WALLET","MID":"EAc0553138556","ORDERID":"100620202152",
//// "PAYMENTMODE":"PPI","RESPCODE":"01","RESPMSG":"Txn Success","STATUS":"TXN_SUCCESS",
//// "TXNAMOUNT":"2.00","TXNDATE":"2020-06-10 16:57:45.0","TXNID":"20200610111212800110168328631290118"}
//
//            // checking is the transaction is successful or not
//            String input = data.getStringExtra("response");
//            assert input != null;
//            boolean isFound = input.contains("TXN_SUCCESS");
//
//                if (isFound) {
//                    placeOrder(data.getStringExtra("response"));
//                    Toast.makeText(ConfirmOrderActivity.this, "Transaction successful..", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(ConfirmOrderActivity.this, "Transaction unsuccessful, please try again ...", Toast.LENGTH_SHORT).show();
//                }
//        }
//    }



    // till now this code is used to take payments from paytm


    // will run when payment is done by paytm app
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Log.e(TAG, " result code " + resultCode);
//        // -1 means successful  // 0 means failed
//        // one error is - nativeSdkForMerchantMessage : networkError
//
//        if (requestCode == ActivityRequestCode && data != null) {
//            Bundle bundle = data.getExtras();
//            if (bundle != null) {
//
//                Log.e(TAG, "transaction from paytm app Response (onTransactionResponse) : "+bundle.toString());
//
//                if(bundle.getString("STATUS").equals("TXN_SUCCESS")){
//                    placeOrder(bundle.toString());
//                    Toast.makeText(ConfirmOrderActivity.this, "Transaction successful..", Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(ConfirmOrderActivity.this, "Transaction unsuccessful, please try again ...", Toast.LENGTH_SHORT).show();
//                }
//
//                // creating log for all the bundle keys
////                for (String key : bundle.keySet()) {
////                    Log.e(TAG, key + " : " + (bundle.get(key) != null ? bundle.get(key) : "NULL"));
////                }
//            }else{
//                Toast.makeText(this, "Didn't get any data", Toast.LENGTH_SHORT).show();
//            }
//
//
//            Log.e(TAG, " data " + data.getStringExtra("nativeSdkForMerchantMessage"));
//            Log.e(TAG, " data response - " + data.getStringExtra("response"));
//
//            // sending the whole response to the server
//            // so that we have all the information about the transaction
//
//            // String info= Objects.requireNonNull(data.getExtras()).getString("STATUS");
//
//            // making json string response to json object
////            if (data.getStringExtra("response") != null) {
////
////                JSONParser parser = new JSONParser();
////
////                try {
////                    JSONObject json = (JSONObject) parser.parse(data.getStringExtra("response"));
////
////                    // if transaction is successful then okay
////                    String stat = json.getString("STATUS");
////
////                    // placing the order
////                    if (stat.equals("TXN_SUCCESS")) {
////                        placeOrder(data.getStringExtra("response"));
////                        Toast.makeText(this, "Transaction successful..", Toast.LENGTH_SHORT).show();
////                    } else {
////                        Toast.makeText(this, "Transaction unsuccessful..", Toast.LENGTH_SHORT).show();
////                    }
////
////                } catch (JSONException | ParseException e) {
////                    e.printStackTrace();
////                }
////            }
//
//
////            if (info.equals("TXN_SUCCESS") && info!=null){
////            placeOrder(data.getStringExtra("response"));
////            }else{
////                Toast.makeText(this, "Transaction unsuccessful..", Toast.LENGTH_SHORT).show();
////            }
//
///*
// data response - {"BANKNAME":"WALLET","BANKTXNID":"1395841115",
// "CHECKSUMHASH":"7jRCFIk6mrep+IhnmQrlrL43KSCSXrmM+VHP5pH0hekXaaxjt3MEgd1N9mLtWyu4VwpWexHOILCTAhybOo5EVDmAEV33rg2VAS/p0PXdk\u003d",
// "CURRENCY":"INR","GATEWAYNAME":"WALLET","MID":"EAc0553138556","ORDERID":"100620202152",
// "PAYMENTMODE":"PPI","RESPCODE":"01","RESPMSG":"Txn Success","STATUS":"TXN_SUCCESS",
// "TXNAMOUNT":"2.00","TXNDATE":"2020-06-10 16:57:45.0","TXNID":"20200610111212800110168328631290118"}
//  */
//            //Toast.makeText(this, data.getStringExtra("nativeSdkForMerchantMessage")
//            //    + data.getStringExtra("response"), Toast.LENGTH_SHORT).show();
//
//        } else {
//            Log.e(TAG, " payment failed");
//            Toast.makeText(this, " payment failed", Toast.LENGTH_SHORT).show();
//        }
//    }

    ////////////////////  code to taking payments from paytm //////////////////////////////////

//    private void takingPaymentThrowPayTm(){
//
//        final String M_id="";    // merchant id
//        final String customer_id=mAuth.getUid();   // logged in user id
//        final String order_id= UUID.randomUUID().toString().substring(0,28);   // 28 char long random string
//        String url="https://checked-approvals.000webhostapp.com/Paytm/PaytmChecksum.php";
//        final String callBackUrl="https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";
//
//        // adding string request to queue
//        RequestQueue requestQueue= Volley.newRequestQueue(ConfirmOrderActivity.this);
//
//        // using volley we are creating a string request from server
//
//        StringRequest stringRequest=new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//
//                // if request is successful
//                // then we are extracting checkSumHash object
//
//                try {
//                    JSONObject jsonObject=new JSONObject(response);
//
//                    if (jsonObject.has("CHECKSUMHASH")){
//                        String checkSumHash=jsonObject.getString("CHECKSUMHASH");
//
//                        //PaytmPGService paytmPGService=PaytmPGService.getStagingService();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//                // if request got cancel
//                Toast.makeText(ConfirmOrderActivity.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
//            }
//        }){
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//
//                Map<String,String> paramMap=new HashMap<String, String>();
//                paramMap.put("MID",M_id);
//                paramMap.put("ORDER_ID",order_id);
//                paramMap.put("CUST_ID",customer_id);
//                paramMap.put("CHANNEL_ID","WAP");
//                paramMap.put("TXT_AMOUNT",totalAmount);
//                paramMap.put("WEBSITE","WEBSTAGING");
//                paramMap.put("INDUSTRY_TYPE_ID","Retail");
//                paramMap.put("CALLBACK_URL",callBackUrl);
//
//                return paramMap;
//            }
//        };
//
//        // adding string request to request queue
//        requestQueue.add(stringRequest);
//    }



    private void placeOrder(final String UTRno){

        loadingBar.setTitle("Do not Press back");
        loadingBar.setMessage("Please wait we are Placing your order to seller");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        //ordersReference= FirebaseDatabase.getInstance().getReference();


        // generating path before accepting order
//        final HashMap<String,Object> yearMAp=new HashMap<>();
//        yearMAp.put("date",nodeYear);

        final HashMap<String,Object> monthMAp=new HashMap<>();
        monthMAp.put("date",nodeMonth);

        final HashMap<String,Object> dateMAp=new HashMap<>();
        dateMAp.put("date",nodeDate);

//        final HashMap<String,Object> generatePathMAp=new HashMap<>();
//        generatePathMAp.put("Orders/" + nodeYear,yearMAp);
//        generatePathMAp.put("Orders/" + nodeYear + "/Months/" + nodeMonth,monthMAp);
//        generatePathMAp.put("Orders/" + nodeYear + "/Months/" + nodeMonth + "/Days/" + nodeDate,dateMAp);

//        ordersReference.child("Orders").child(nodeYear).updateChildren(yearMAp)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//
//                if (task.isSuccessful()){
                    // if year is set

                    ordersReference.child("Orders").child(nodeYear)
                            .child("Months").child(nodeMonth).updateChildren(monthMAp)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){
                                        // if month is set

                                        ordersReference.child("Orders").child(nodeYear).child("Months")
                                                .child(nodeMonth).child("Days").child(nodeDate).updateChildren(dateMAp)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()){
                                                            // if date is set


        // generate a 6 digit delivery code
        int randomNumber = new Random().nextInt(900000) + 100000;

//        final String key= ordersReference.child("Orders").child(nodeYear).child("Months").child(nodeMonth)
//                .child("Days").child(nodeDate).child("Orders").push().getKey();

        final HashMap<String,Object> orderMAp=new HashMap<>();
        orderMAp.put("orderID",key);
        orderMAp.put("UTR_No",UTRno);
        orderMAp.put("name",userName.getText().toString());
        orderMAp.put("number",userNumber.getText().toString());
        orderMAp.put("address",predefineAddress);
        orderMAp.put("addressID",predefineAddressID);
        orderMAp.put("addressLineOne",addressLineOne.getText().toString());
        orderMAp.put("TotalAmount",totalAmount);
        orderMAp.put("Time",saveCurrentTime);
        orderMAp.put("Date",nodeDate);
        if (extraSwitchCompact.isChecked()){
            orderMAp.put("deliveryType","instant");
        }else {
            orderMAp.put("deliveryType", "standard");
        }
        orderMAp.put("DeliveryCode",String.valueOf(randomNumber));
        orderMAp.put("deliveryCharge",String.valueOf(CartViewActivity.lastAmount.getTotalDeliveryCharge()));
        orderMAp.put("discount",String.valueOf(CartViewActivity.lastAmount.getTotalDiscount()));
        orderMAp.put("status","Ordered");
        orderMAp.put("refund","no");
        orderMAp.put("soldOn","no");
        orderMAp.put("timeOfChange","");
        orderMAp.put("partiallyCancel","NO");
        orderMAp.put("userID",userID);
        orderMAp.put("time_stamp",String.valueOf(System.currentTimeMillis()));
        orderMAp.put("appliedOfferName",CartViewActivity.appliedOfferName);
        orderMAp.put("UPI_ID",userUPIId);

        final int i=CartViewActivity.allProductInCartList.size();
        int j=0;
        while(j<i) {

            // adding product details
            orderProductDetails details = CartViewActivity.allProductInCartList.get(j);

            HashMap<String, Object> orderDetailsMAp = new HashMap<>();
            orderDetailsMAp.put("ProductId", details.userCart.getProductId());
            orderDetailsMAp.put("productName", details.userCart.getProductName());
            orderDetailsMAp.put("Quantity",details.getQuantity());
            if (details.userCart.getSelected_type().equals("Half")){
                orderDetailsMAp.put("productPrice", details.userCart.getHalf_price());
            }else {
                orderDetailsMAp.put("productPrice", details.userCart.getProductPrice());
            }
            orderDetailsMAp.put("selected_type",details.userCart.getSelected_type());
            orderDetailsMAp.put("restaurantName", details.userCart.getRestaurantName());
            orderDetailsMAp.put("category", details.userCart.getCategory());
            orderDetailsMAp.put("consumerType", details.userCart.getConsumerType());
            orderDetailsMAp.put("status","Ordered");
            j++;
            if (details.userCart.getSelected_type().equals("Half")){
                orderMAp.put("/details/" + details.userCart.getProductId() + "Half/",orderDetailsMAp);
            }else {
                orderMAp.put("/details/" + details.userCart.getProductId() + "/",orderDetailsMAp);
            }
        }

        final String currentTimeInMillisecond=String.valueOf(System.currentTimeMillis());

        assert key != null;
        ordersReference.child("Orders").child(nodeYear).child("Months").child(nodeMonth)
                .child("Days").child(nodeDate).child("Orders").child(key).updateChildren(orderMAp)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            // updating order in user order list
                            HashMap<String, Object> userOrderDetailsMAp = new HashMap<>();
                            userOrderDetailsMAp.put("year", nodeYear);
                            userOrderDetailsMAp.put("month",nodeMonth);
                            userOrderDetailsMAp.put("date",nodeDate);
                            userOrderDetailsMAp.put("nodeID",key);
                            userOrderDetailsMAp.put("name",userName.getText().toString());
                            userOrderDetailsMAp.put("amount", totalAmount);
                            userOrderDetailsMAp.put("time_stamp",String.valueOf(System.currentTimeMillis()));

                            ordersReference.child("Users").child(userID).child("Orders").child(currentTimeInMillisecond).updateChildren(userOrderDetailsMAp)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                // removing all the items from user cart
                                                ordersReference.child("Cart List").child(userID).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    Toast.makeText(ConfirmOrderActivity.this, "Congrats you order placed successfully...", Toast.LENGTH_SHORT).show();
                                                                    loadingBar.dismiss();
                                                                    Intent intent=new Intent(getApplicationContext(),CompleteOrderActivity.class);
                                                                    startActivity(intent);
                                                                }else {
                                                                    loadingBar.dismiss();
                                                                    Toast.makeText(ConfirmOrderActivity.this, "Error while removing items from your cart", Toast.LENGTH_SHORT).show();
                                                                    Intent intent=new Intent(getApplicationContext(),CompleteOrderActivity.class);
                                                                    startActivity(intent);
                                                                }
                                                                finish();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        loadingBar.dismiss();
                                                        Toast.makeText(ConfirmOrderActivity.this, "Error while removing items from your cart", Toast.LENGTH_SHORT).show();
                                                        Intent intent=new Intent(getApplicationContext(),CompleteOrderActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                            }else {
                                                loadingBar.dismiss();
                                                Toast.makeText(ConfirmOrderActivity.this, " Your order has been placed but due to some error it will not show in your order history. Don't worry your order will be delivered..", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loadingBar.dismiss();
                                    Toast.makeText(ConfirmOrderActivity.this, " Your order has been placed but due to some error it will not show in your order history. Don't worry your order will be delivered..", Toast.LENGTH_SHORT).show();
                                }
                            });
                            }else {
                            loadingBar.dismiss();
                            Toast.makeText(ConfirmOrderActivity.this, " Your order has been placed but due to some error it will not show in your order history. Don't worry your order will be delivered..", Toast.LENGTH_SHORT).show();
                        }
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(ConfirmOrderActivity.this, " some error occurred while updating orders for user ", Toast.LENGTH_SHORT).show();
            }
        });
                                    }else {
                                                            loadingBar.dismiss();
                                                            Toast.makeText(ConfirmOrderActivity.this, " some error occurred while setting date ", Toast.LENGTH_SHORT).show();
                                                        }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                loadingBar.dismiss();
                                                Toast.makeText(ConfirmOrderActivity.this, " some error occurred while setting date ", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                        }else{
                            loadingBar.dismiss();
                            Toast.makeText(ConfirmOrderActivity.this, "some error occurred while setting month " , Toast.LENGTH_SHORT).show();
                        }
                    }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(ConfirmOrderActivity.this, "some error occurred while setting month  " , Toast.LENGTH_SHORT).show();
            }
        });
//                }else {
//                    loadingBar.dismiss();
//                    Toast.makeText(ConfirmOrderActivity.this, "some error occurred while setting year" , Toast.LENGTH_SHORT).show();
//                }
//            }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                loadingBar.dismiss();
//                Toast.makeText(ConfirmOrderActivity.this, "some error occurred while setting year" , Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    // showing user information
    private void displayUserInformation()
    {
        final DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference();

        UsersRef.child("Users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                       // String image = dataSnapshot.child("image").getValue().toString();
                        String name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                        userPhoneNumber = Objects.requireNonNull(dataSnapshot.child("number").getValue()).toString();
                        predefineAddress = Objects.requireNonNull(dataSnapshot.child("address").getValue()).toString();
                        predefineAddressID= Objects.requireNonNull(dataSnapshot.child("AddressID").getValue()).toString();
                        userEmail= Objects.requireNonNull(dataSnapshot.child("email").getValue()).toString();

                       // Picasso.get().load(image).into(profileImageView);
                        userName.setText(name);
                        userNumber.setText(userPhoneNumber);
                        addressTxt.setText(predefineAddress);

//                        if (dataSnapshot.child("UPI_ID").getValue().toString().equals("NO")){
//                            acceptingOrdersNotice.setText("Please enter your UPI id in your profile. In case of refund, amount will be transferred in that.");
//                        }
//                        else {

                            userUPIId= Objects.requireNonNull(dataSnapshot.child("UPI_ID").getValue()).toString();
                            // access delivery charges of this address

                    if (predefineAddressID.equals("-MoX-diWp4l8wRtc055d") || predefineAddressID.equals("-MoX-pfSOUp7Qz1M391I")) {

                        UsersRef.child("delivery_addresses").child("JODHPUR").child(predefineAddressID)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {

                                            // now we have all details about this address
                                            addressDetails = snapshot.getValue(AvailableAddresses.class);
                                            // if instant delivery present then run this
                                            if (addressDetails.getInstant_delivery_available().equals("YES")) {
                                                instantDeliveryPrice.setVisibility(View.VISIBLE);
                                                extraSwitchCompact.setVisibility(View.VISIBLE);
                                                instantDeliveryDescription.setVisibility(View.VISIBLE);
                                                instantDeliveryPrice.setText("+₹" + addressDetails.getInstant_extra_cost());
                                                instantDeliveryDescription.setText(addressDetails.getInstant_delivery_details());
                                            }
                                            standardDeliveryDescription.setText(addressDetails.getStandard_delivery_details());
                                            CompareTimeShouldWeTake();
                                            setDeliveryCharges();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                    }else{
                        // set the address to nit jsr

                        HashMap<String,Object> addressMap=new HashMap<>();
                        addressMap.put("AddressID","-MoX-diWp4l8wRtc055d");
                        addressMap.put("address","NIT Jamshedpur");

                        UsersRef.child("Users").child(userID).updateChildren(addressMap);

                        Toast.makeText(ConfirmOrderActivity.this, "Unable to deliver at this address...", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                        //}
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {

            }
        });
    }

    // comparing times that we should take order or not
    private void CompareTimeShouldWeTake(){
        String time1=addressDetails.getLast_time_of_ordering();

        Calendar calendar = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        String time = currentTime.format(calendar.getTime());

        if (time1.compareTo(time)>0){
            confirmFinalOrder.setVisibility(View.VISIBLE);
            confirmFinalOrder.setEnabled(true);
            acceptingOrdersNotice.setText("Accepting Orders Before " + time1);
        }else {
            acceptingOrdersNotice.setText("Currently not accepting orders");
        }
    }

    // setting the delivery charges
    private void setDeliveryCharges(){

        int total=0;
        // for the punjabi chicken we are taking 50 rupees delivery charge
        if (extraSwitchCompact.isChecked()){
            String deliveryCharge = addressDetails.getStandard_cost();

            // setting extra 50 rupees if punjabi chicken is in the cart
            if (CartViewActivity.isPunjabiChicInn){
                total= Integer.parseInt(deliveryCharge) * CartViewActivity.lastAmount.getTotalRestaurants() + Integer.parseInt(addressDetails.getInstant_extra_cost()) + 50;
                deliveryChargeTxt.setText(deliveryCharge + "X" + CartViewActivity.lastAmount.getTotalRestaurants() + " +50+ " + addressDetails.getInstant_extra_cost() + "= ₹" + total);
            }else{
                total= Integer.parseInt(deliveryCharge) * CartViewActivity.lastAmount.getTotalRestaurants() + Integer.parseInt(addressDetails.getInstant_extra_cost());
                deliveryChargeTxt.setText(deliveryCharge + "X" + CartViewActivity.lastAmount.getTotalRestaurants()+ " +" + addressDetails.getInstant_extra_cost() + "= ₹" + total);
            }

            // total= Integer.parseInt(deliveryCharge) * CartViewActivity.lastAmount.getTotalRestaurants() + Integer.parseInt(addressDetails.getInstant_extra_cost());
            CartViewActivity.lastAmount.setTotalDeliveryCharge(total);
           // deliveryChargeTxt.setText(deliveryCharge + "X" + CartViewActivity.lastAmount.getTotalRestaurants()+ "+" + addressDetails.getInstant_extra_cost() + "= ₹" + total);
        }else {
            // if instant delivery is not selected
            if (Integer.parseInt(addressDetails.getStandard_cost()) == 0) {

                // setting extra 50 rupees if punjabi chicken is in the cart
                if (CartViewActivity.isPunjabiChicInn){
                    deliveryChargeTxt.setText("50");
                    CartViewActivity.lastAmount.setTotalDeliveryCharge(50);
                }else{
                    deliveryChargeTxt.setText("FREE");
                    CartViewActivity.lastAmount.setTotalDeliveryCharge(0);
                }

            } else {
                String deliveryCharge = addressDetails.getStandard_cost();

                // setting extra 50 rupees if punjabi chicken is in the cart
                if(CartViewActivity.isPunjabiChicInn){
                    total = Integer.parseInt(deliveryCharge) * CartViewActivity.lastAmount.getTotalRestaurants()+50;
                    deliveryChargeTxt.setText(deliveryCharge + "X" + CartViewActivity.lastAmount.getTotalRestaurants() + " +50 = ₹" + total);
                }else {
                    total = Integer.parseInt(deliveryCharge) * CartViewActivity.lastAmount.getTotalRestaurants();
                    deliveryChargeTxt.setText(deliveryCharge + "X" + CartViewActivity.lastAmount.getTotalRestaurants() + "= ₹" + total);
                }

                CartViewActivity.lastAmount.setTotalDeliveryCharge(total);
            }
        }

        totalAmountToBePaid=CartViewActivity.lastAmount.getTotalPrice()-CartViewActivity.lastAmount.getTotalDiscount()+CartViewActivity.lastAmount.getTotalDeliveryCharge();
        totalAmount=String.valueOf(totalAmountToBePaid);
        totalAmountTxt.setText(String.valueOf(totalAmountToBePaid));
    }

    private void initializingViews() {
        userName=(EditText)findViewById(R.id.shipment_name);
        userNumber=(EditText)findViewById(R.id.shipment_phone_number);
        addressTxt=(TextView)findViewById(R.id.shipment_address);
        confirmFinalOrder=(Button)findViewById(R.id.confirm_final_order_btn);
        loadingBar=new ProgressDialog(this);

        priceTxt=(TextView)findViewById(R.id.confirm_order_price);
        discountTxt=(TextView)findViewById(R.id.confirm_order_discount);
        deliveryChargeTxt=(TextView)findViewById(R.id.confirm_order_delivery_charges);
        totalAmountTxt=(TextView)findViewById(R.id.confirm_order_total_amount);

        priceTxt.setText("₹" + CartViewActivity.lastAmount.getTotalPrice());
        discountTxt.setText("-₹" + CartViewActivity.lastAmount.getTotalDiscount());

        extraSwitchCompact=(SwitchCompat)findViewById(R.id.confirm_order_switch);

        standardDeliveryDescription=(TextView)findViewById(R.id.confirm_order_standard_delivery_description);
        instantDeliveryDescription=(TextView)findViewById(R.id.confirm_order_instant_delivery_description);
        instantDeliveryPrice=(TextView)findViewById(R.id.confirm_order_instant_extra_charge);

        acceptingOrdersNotice=(TextView)findViewById(R.id.accepting_orders_notice);
        addressLineOne=(EditText)findViewById(R.id.shipment_address_line1);

        progressBar=(ProgressBar)findViewById(R.id.confirm_order_progress_bar);
    }
}