package com.example.cenk.graduation.UploadPhoto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cenk.graduation.Menu.HomePage;
import com.example.cenk.graduation.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class Uploaded extends AppCompatActivity {


    EditText comText;
    Button goBackBtn, uploadBtn;
    ImageView uploadedImage;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private StorageReference mStorageRef;
    Uri selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploaded);

        comText = (EditText) findViewById(R.id.commentText);
        goBackBtn =(Button) findViewById(R.id.goBackBtn);
        uploadBtn =(Button) findViewById(R.id.uploadBtn);
        uploadedImage =(ImageView) findViewById(R.id.uploadImage);
        firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = firebaseDatabase.getReference();
        auth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();


        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), HomePage.class);
                startActivity(intent);
            }
        });

    }


    public void upload (View view){

        UUID uuid=UUID.randomUUID();
        String imageName = "images/"+uuid+".jpg";

        StorageReference storageReference =mStorageRef.child(imageName);
        storageReference.putFile(selected).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                FirebaseUser user =auth.getCurrentUser();
                String userComment = comText.getText().toString();
                String downloadURL = taskSnapshot.getDownloadUrl().toString();
                String userEmail=user.getEmail().toString();



                UUID uuid= UUID.randomUUID();
                String uuidString =uuid.toString();
                myRef.child("UploadedPosts").child(uuidString).child("userComment").setValue(userComment);
                myRef.child("UploadedPosts").child(uuidString).child("uploadedImage").setValue(downloadURL);
                myRef.child("UploadedPosts").child(uuidString).child("userEmail").setValue(userEmail);



                Toast.makeText(getApplicationContext(),"Photo is uploaded!",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), HomePage.class);
                startActivity(intent);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),e.getLocalizedMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        });

    }


    public void chooseImage (View view){

        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,2);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 1){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent= new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,2);

            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==2 && resultCode == RESULT_OK && data != null){

            data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selected);
                uploadedImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
}
