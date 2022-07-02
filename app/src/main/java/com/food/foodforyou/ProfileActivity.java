package com.food.foodforyou;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private EditText fullNameEditText, userPhoneEditText,upiEditText;
    private Button closeTextBtn, saveTextButton;
    private Spinner spinner_address;
    private ArrayAdapter<CharSequence> adapter;
    private int addressIDPosition=0;
    private int predefinedIDPosition=0;
    private ArrayList<String> addressID=new ArrayList<String>();

    private String selected_address="";
    private String predefineAddress="";
    private FirebaseAuth mAuth;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

         assignAllAddressIDs();

        mAuth= FirebaseAuth.getInstance();
        userID=mAuth.getCurrentUser().getUid();
        
        initializingView();

        Toolbar toolbar = findViewById(R.id.profile_toolbar);
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
            // check that email is verified or not
//            if (currentUser.isEmailVerified()) {
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
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

        if (isConnectionAvailable(ProfileActivity.this)){

            userInfoDisplay(fullNameEditText, userPhoneEditText);

        }else {
            new LovelyStandardDialog(ProfileActivity.this)
                    .setTopColorRes(R.color.colorPrimary)
                    .setIcon(R.drawable.ic_baseline_signal_cellular_connected_no_internet_4_bar_24)
                    .setTitle("No Internet Connection")
                    .setMessage("It seems like you don't have any internet connection.")
                    .setPositiveButton(android.R.string.ok,null)
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }

        closeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        saveTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    updateOnlyUserInfo();
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

    // assigning all IDs too their addresses
    private void assignAllAddressIDs(){
        addressID.add("nothing");
        addressID.add("-MPtZNP5-bT8e5HnjBxM");    //  AIIMS Jodhpur
        addressID.add("-MPtZeavJlKNgQp7ehLu");     //  Dr. SN MC Jodhpur
        addressID.add("-MOqbp3182gLOXU57LJw");    //  DSRRAU, Jodhpur
        addressID.add("-MOqbEMaPPVCDFRuJ7Dp");     // "FDDI, Jodhpur"
        addressID.add("-MOqa2Jn_1sZEZbegAGz");  // IIT, Jodhpur
        addressID.add("-MOqcOyCxrzypU7MovZy");     //  NIFT, Jodhpur
        addressID.add("-MOqcjTadsDNHpW5CSnU");     //  NLU, Jodhpur
        addressID.add("-MoX-diWp4l8wRtc055d");     // NIT Jamshedpur
        addressID.add("-MoX-pfSOUp7Qz1M391I");     // NIT Durgapur
    }

    private void updateOnlyUserInfo()
    {
        if(TextUtils.isEmpty(upiEditText.getText().toString())){
            upiEditText.setError("UPI ID is required ");
            upiEditText.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(fullNameEditText.getText().toString())){
            fullNameEditText.setError("Name is required ");
            fullNameEditText.requestFocus();
            return;
        }if(TextUtils.isEmpty(userPhoneEditText.getText().toString().trim())){
        userPhoneEditText.setError("number is required ");
        userPhoneEditText.requestFocus();
        return;
    }else if (userPhoneEditText.getText().toString().length()!=10){
        userPhoneEditText.setError("number must have 10 digits");
        userPhoneEditText.requestFocus();
        return;
    }else {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("name", fullNameEditText.getText().toString());
        if (!selected_address.equals("_") && selected_address != null) {
            userMap.put("address", selected_address);
            userMap.put("AddressID",addressID.get(addressIDPosition));
        } else {
            userMap.put("address", predefineAddress);
            userMap.put("AddressID",addressID.get(predefinedIDPosition));
        }

        //userMap. put("address", addressEditText.getText().toString());
        userMap.put("number", userPhoneEditText.getText().toString());
        userMap.put("UPI_ID", upiEditText.getText().toString());
        ref.child(userID).updateChildren(userMap);

        Toast.makeText(ProfileActivity.this, "Profile Info update successfully.", Toast.LENGTH_SHORT).show();
        finish();
    }
    }

    private void userInfoDisplay(final EditText fullNameEditText, final EditText userPhoneEditText)
    {
        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                        String name = dataSnapshot.child("name").getValue().toString();
                        String phone = dataSnapshot.child("number").getValue().toString();
                        String address = dataSnapshot.child("address").getValue().toString();

                        fullNameEditText.setText(name);
                        userPhoneEditText.setText(phone);
                        upiEditText.setText(dataSnapshot.child("UPI_ID").getValue().toString());

                        // already select the spinner
                        predefineAddress=address;
                     //   String compareValue = predefineAddress;
                        spinner_address.setAdapter(adapter);
                        if (!address.equals("")) {
                            int spinnerPosition = adapter.getPosition(address);
                            predefinedIDPosition=spinnerPosition;
                            spinner_address.setSelection(spinnerPosition);
                        }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class spinnerAddressClass implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            addressIDPosition=position;
            selected_address=parent.getItemAtPosition(position).toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private void initializingView() {

        fullNameEditText = (EditText) findViewById(R.id.change_fullName);
        userPhoneEditText = (EditText) findViewById(R.id.change_phoneNumber);
        closeTextBtn = (Button) findViewById(R.id.close_Setting);
        saveTextButton = (Button) findViewById(R.id.update_Setting);
        upiEditText=(EditText)findViewById(R.id.profile_activity_user_upi_id);

        spinner_address=(Spinner)findViewById(R.id.spinner_address);
       // adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,allAddresses);
          adapter=ArrayAdapter.createFromResource(this,R.array.addresses,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_address.setAdapter(adapter);

        spinner_address.setOnItemSelectedListener(new spinnerAddressClass());
    }
}