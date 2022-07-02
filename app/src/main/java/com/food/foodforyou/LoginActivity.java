package com.food.foodforyou;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmailId;
    private EditText loginPassword;
    private Button loginButton;
    private TextView createNewAccount;
    private TextView forgetPassword;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        // this code will change the color of the status bar
        Window window = this.getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimary1));

        mAuth=FirebaseAuth.getInstance();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            // check that email is verified or not
//            if (currentUser.isEmailVerified()) {
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
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

        initializingViews();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnectionAvailable(LoginActivity.this)){
                    loginIn();
                }else {
                    new LovelyStandardDialog(LoginActivity.this)
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

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isConnectionAvailable(LoginActivity.this)){

                    final EditText resetMail = new EditText(v.getContext());
                    final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                    passwordResetDialog.setTitle("Reset Password ?");
                    passwordResetDialog.setMessage("Enter Your Email To Received Reset Link.");
                    passwordResetDialog.setView(resetMail);

                    passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // extract the email and send reset link
                            String mail = resetMail.getText().toString().trim();

                            // if email is not empty then we will send the mail
                            if (TextUtils.isEmpty(mail)) {
                                Toast.makeText(getApplication(), "Enter your registered email id", Toast.LENGTH_SHORT).show();
                            }else {

                                loadingBar.setTitle("Sending Mail");
                                loadingBar.setMessage("Please wait while we are sending the mail. ");
                                loadingBar.setCanceledOnTouchOutside(false);
                                loadingBar.show();

                                mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        loadingBar.dismiss();
                                        Toast.makeText(LoginActivity.this, "Reset Link Sent To Your Email.", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loadingBar.dismiss();
                                        Toast.makeText(LoginActivity.this, "Error ! Reset Link is Not Sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });

                    passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // close the dialog
                        }
                    });

                    passwordResetDialog.create().show();

                }else {
                    new LovelyStandardDialog(LoginActivity.this)
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

        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),Register.class);
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

    private void loginIn() {
        final String userEmail=loginEmailId.getText().toString().trim();
        final String userPassword=loginPassword.getText().toString().trim();

        if(TextUtils.isEmpty(userEmail)){
            loginEmailId.setError("Email is required ");
            loginEmailId.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(userPassword)){
            loginPassword.setError("Password is required ");
            loginPassword.requestFocus();
            return;
        }

        loadingBar.setTitle("Logging In");
        loadingBar.setMessage("Please wait while we are checking the credentials. ");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        mAuth.signInWithEmailAndPassword(userEmail,userPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this, "successfully logged in..", Toast.LENGTH_SHORT).show();

                            user=mAuth.getCurrentUser();

                            // check that email is verified or not
//                            if (user.isEmailVerified()) {
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
//                            }else{
//                                Intent intent = new Intent(LoginActivity.this, verifyUser.class);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                startActivity(intent);
//                                finish();
                           // }
                        }else {

                            try {
                                throw Objects.requireNonNull(task.getException());
                            } catch (FirebaseAuthInvalidUserException e) {
                                Toast.makeText(LoginActivity.this,"Didn't find Account", Toast.LENGTH_SHORT).show();
                            }catch (FirebaseAuthInvalidCredentialsException  e) {
                                Toast.makeText(LoginActivity.this,"Invalid Credentials..", Toast.LENGTH_SHORT).show();
                            }catch(Exception e) {
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            loadingBar.dismiss();
                            }
                    }
                });
    }

    // initializing all the views
    private void initializingViews() {

        loginEmailId=(EditText)findViewById(R.id.login_email);
        loginPassword=(EditText)findViewById(R.id.login_password);

        loginButton=(Button) findViewById(R.id.login_button);
        createNewAccount=(TextView) findViewById(R.id.login_new_account);
        forgetPassword=(TextView) findViewById(R.id.login_forget_password);

        loadingBar=new ProgressDialog(this);
    }
}