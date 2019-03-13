package com.bornstunner.firebaseauth;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {
    private static final int CHOOSE_IMAGE = 100;
    ImageView camera;
    EditText displayName;
    Button save, logout;
    Uri uriProfileImage;
    ProgressBar PB;
    String profileImageUrl;

    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        camera = findViewById(R.id.ivCamera);
        displayName = findViewById(R.id.etDisplayName);
        save = findViewById(R.id.btnSave);
        logout = findViewById(R.id.btnLogout);
        PB = findViewById(R.id.pb);

        auth = FirebaseAuth.getInstance();

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageChoose();

            }
        });

        getUserInformation();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (auth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        }
    }

    private void getUserInformation() {
        FirebaseUser user = auth.getCurrentUser();

        if (user != null){

            if (user.getPhotoUrl() != null){
                Log.d("isURLgotten", user.getPhotoUrl().toString());
                Glide.with(this)
                        .load(user.getPhotoUrl().toString())
                        .into(camera);

            }
            if (user.getDisplayName() != null){
                displayName.setText(user.getDisplayName());

            }
        }
    }

    private void saveUserInformation() {
        String dn = displayName.getText().toString();
        if (dn.isEmpty()){
            displayName.setError("Name is Required");
            displayName.requestFocus();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();

        if (user != null && profileImageUrl !=null){
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(dn)
                    .setPhotoUri(Uri.parse(profileImageUrl))
                    .build();

            user.updateProfile(profile)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ProfileActivity.this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriProfileImage = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                camera.setImageBitmap(bitmap);

                uploadImageReference();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageReference() {
        StorageReference mStorageRef =
                FirebaseStorage.getInstance().getReference("profilePid/"+System.currentTimeMillis()+".jpg");

        if (uriProfileImage != null){
            PB.setVisibility(View.VISIBLE);
            mStorageRef.putFile(uriProfileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            PB.setVisibility(View.GONE);
                            profileImageUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            PB.setVisibility(View.GONE);
                            Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }
    }

    private void selectImageChoose(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }
}
