package com.elfak.mosis.sportsoutdoors;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SELECTED_PICTURE = 2;

    ImageView userPhotoImageView;

    User user;

    Handler guiThread;
    ProgressDialog progressDialog;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        user = new User();

        guiThread = new Handler();
        progressDialog = new ProgressDialog(this);
        context = this;

        final EditText usernameEditText = (EditText) findViewById(R.id.username_register);
        final EditText passwordEditText = (EditText) findViewById(R.id.password_register);
        final EditText firstnameEditText = (EditText) findViewById(R.id.first_name_register);
        final EditText lastnameEditText = (EditText) findViewById(R.id.last_name_register);
        final EditText phonenumberEditText = (EditText) findViewById(R.id.phone_number_register);


        final Button registerButton = (Button) findViewById(R.id.button_register_ok);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                user.setPassword(passwordEditText.getText().toString());
                user.setUsername(usernameEditText.getText().toString());
                user.setFirstname(firstnameEditText.getText().toString());
                user.setLastname(lastnameEditText.getText().toString());
                user.setPhonenumber(phonenumberEditText.getText().toString());

                ExecutorService transThread = Executors.newSingleThreadExecutor();

                transThread.submit(new Runnable() {
                    @Override
                    public void run() {
                        guiStartProgressDialog("Register in progress", "User: " + user.getUsername());
                        try {
                            final String message = AppHTTPHelper.registerUser(user);

                            if(message.equals("Success")){
                                guiNotifyUser("Successfully registered.");
                                setResult(Activity.RESULT_OK);
                                finish();
                            }
                            else{
                                guiNotifyUser(message);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        guiDismissProgressDialog();

                    }
                });
            }
        });

        final Button backButton = (Button)findViewById(R.id.button_register_back);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        final Button cameraButton = (Button)findViewById(R.id.camera_button);
        userPhotoImageView = (ImageView)findViewById(R.id.userPhotoImageView);
        if(!hasCamera())
            cameraButton.setEnabled(false);

    }

    public boolean hasCamera(){
        //return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
        return true;
    }

    public void launchCamera(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");

            userPhotoImageView.setImageBitmap(photo);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            user.setUserphoto(encodedImage);
        }

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

    private void guiDismissProgressDialog() {
        guiThread.post(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        });
    }

}
