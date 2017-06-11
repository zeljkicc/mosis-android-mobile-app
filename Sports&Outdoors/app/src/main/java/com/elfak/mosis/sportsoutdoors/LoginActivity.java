package com.elfak.mosis.sportsoutdoors;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private Boolean setPassword = false;
    private Boolean setUsername = false;

    Handler guiThread;
    ProgressDialog progressDialog;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        guiThread = new Handler();
        progressDialog = new ProgressDialog(this);
        context = this;

        setResult(Activity.RESULT_CANCELED);

        final Button registerButton = (Button)findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerActivity = new Intent(v.getContext(), RegisterActivity.class);
                startActivityForResult(registerActivity, 333);
            }
        });

        final Button loginButton = (Button)findViewById(R.id.login_button);

        final EditText usernameEditText = (EditText)findViewById(R.id.usernameLogin);
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(usernameEditText.getText().equals("")){
                    usernameEditText.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(usernameEditText.getText() != null) {
                    setUsername = true;
                    if (setUsername && setPassword) {
                        loginButton.setEnabled(true);
                    }
                    else{

                        loginButton.setEnabled(false);
                    }
                }
                else{
                    setUsername = false;
                }



            }
        });

        final EditText passwordEditText = (EditText)findViewById(R.id.passwordLogin);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(passwordEditText.getText().equals("")){
                    passwordEditText.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(passwordEditText.getText() != null) {
                    setPassword = true;
                    if (setUsername && setPassword) {
                        loginButton.setEnabled(true);
                    }
                    else{

                        loginButton.setEnabled(false);
                    }
                }
                else{
                    setPassword = false;
                }



            }
        });


        loginButton.setEnabled(false);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final User user = new User();
                user.setPassword(passwordEditText.getText().toString());
                user.setUsername(usernameEditText.getText().toString());

                ExecutorService transThread = Executors.newSingleThreadExecutor();

                transThread.submit(new Runnable() {
                    @Override
                    public void run() {
                        guiStartProgressDialog("Login in progress", "User " + user.getUsername());


                        Intent intent = getIntent();
                        String lat = intent.getStringExtra("lat");
                        String lon = intent.getStringExtra("lon");

                        final User userReturn = AppHTTPHelper.checkLogin(user, lat, lon);
                        try{

                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                        guiDismissProgressDialog();
                        if(userReturn!=null){
                            UserData.getInstance().setUser(userReturn);
                            setResult(Activity.RESULT_OK);


                        }
                        finish();


                    }
                });
            }
        });


    }

    private void guiNotifyUser(final String message) {
        guiThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void guiStartProgressDialog(final String title, final String message) {
        guiThread.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.setTitle(title);
                progressDialog.setMessage(message);
                progressDialog.show();

            }
        });
    }

    private void guiDismissProgressDialog(){
        guiThread.post(new Runnable(){
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 333){
            if(resultCode == Activity.RESULT_OK){
                final AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
                popDialog.setIcon(R.drawable.ic_exit_to_app);
                popDialog.setTitle("Log in");
                popDialog.setMessage("You have successfully registered. Please login to validate your account.");

                popDialog.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        });

                popDialog.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {


                                dialog.dismiss();
                                finish();
                            }
                        });

                popDialog.create();
                popDialog.show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
