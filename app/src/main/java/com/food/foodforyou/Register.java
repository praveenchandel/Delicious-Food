package com.food.foodforyou;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.HashMap;
import java.util.Objects;

public class Register extends AppCompatActivity {

    private EditText registerEmailId;
    private EditText registerName;
    private EditText registerNumber;
    private EditText registerPassword;
    private EditText registerConformPassword;

    private ProgressDialog loadingBar;

    private Button registerButton;
    private Button hasAnAccountTxt;
    private CheckBox acceptingCheckBox;
    private TextView seeTermAndCondition;
    private TextView aboutUsText;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String uID;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // this code will change the color of the status bar
        Window window = this.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimary1));

        mAuth=FirebaseAuth.getInstance();

        initializingViews();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnectionAvailable(Register.this)){
                    createNewAccount();
                }else {
                    new LovelyStandardDialog(Register.this)
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

        hasAnAccountTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        seeTermAndCondition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),RulesActivity.class);
                startActivity(intent);
            }
        });

        aboutUsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),AboutUs.class);
                startActivity(intent);
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


    // validating information
    private void createNewAccount(){

        final String userEmail=registerEmailId.getText().toString();
        final String userName=registerName.getText().toString();
        final String userNumber=registerNumber.getText().toString();
        final String userPassword=registerPassword.getText().toString();
        final String userConformPassword=registerConformPassword.getText().toString();

        if(TextUtils.isEmpty(userEmail)){
            registerEmailId.setError("Email is required ");
            registerEmailId.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(userName)){
            registerName.setError("user name is required ");
            registerName.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(userNumber)){
            registerNumber.setError("number is required ");
            registerNumber.requestFocus();
            return;
        }else if (userNumber.length()!=10){
            registerNumber.setError("number must have 10 digits");
            registerNumber.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(userPassword)){
            registerPassword.setError("password is required ");
            registerPassword.requestFocus();
            return;
        }else if(!userPassword.equals(userConformPassword)){
            registerConformPassword.setError("password didn't match");
            registerConformPassword.requestFocus();
            return;
        }

        if (!acceptingCheckBox.isChecked()){
            Toast.makeText(this, "Please accept our terms and conditions...", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingBar.setTitle("Creating Account");
        loadingBar.setMessage("Please wait while we are checking the credentials. ");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            currentUser=FirebaseAuth.getInstance().getCurrentUser();
                            uID=currentUser.getUid();

                            mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uID);

                            HashMap<String,Object> userMAp=new HashMap<>();
                            userMAp.put("name",userName);
                            userMAp.put("number",userNumber);
                            userMAp.put("email",userEmail);
                            userMAp.put("address","IIT, Jodhpur");
                            userMAp.put("AddressID","-MOqa2Jn_1sZEZbegAGz");
                            userMAp.put("UPI_ID","NO");

                            mDatabase.setValue(userMAp)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                loadingBar.dismiss();
                                                Toast.makeText(Register.this, "your account is successfully created", Toast.LENGTH_SHORT).show();

                                                Intent intent = new Intent(Register.this, HomeActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }else {
                                                loadingBar.dismiss();
                                                Toast.makeText(Register.this, "enable to set account values", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // if there is any problem with credentials

                                try {
                                    throw Objects.requireNonNull(task.getException());
                                } catch(FirebaseAuthWeakPasswordException e) {
                                    registerPassword.setError("Weak Password");
                                    registerPassword.requestFocus();
                                } catch(FirebaseAuthInvalidCredentialsException e) {
                                    registerEmailId.setError("Invalid email id");
                                    registerEmailId.requestFocus();
                                } catch(FirebaseAuthUserCollisionException e) {
                                    registerEmailId.setError("User is already registered");
                                    registerEmailId.requestFocus();
                                } catch(Exception e) {
                                    Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            loadingBar.dismiss();
                        }
                    }
                });
    }


    // initializing all the views
    private void initializingViews() {

        registerEmailId=(EditText)findViewById(R.id.register_email);
        registerName=(EditText)findViewById(R.id.register_name);
        registerNumber=(EditText)findViewById(R.id.register_number);
        registerPassword=(EditText)findViewById(R.id.register_password);
        registerConformPassword=(EditText)findViewById(R.id.register_conform_password);

        registerButton=(Button) findViewById(R.id.register_button);
        hasAnAccountTxt=(Button) findViewById(R.id.register_has_account);

        loadingBar=new ProgressDialog(this);

        acceptingCheckBox=(CheckBox)findViewById(R.id.register_accepting_check_box);
        seeTermAndCondition=(TextView)findViewById(R.id.register_term_and_condition);
        aboutUsText=(TextView)findViewById(R.id.register_about_us_text);
    }
}