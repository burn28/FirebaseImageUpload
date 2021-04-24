package com.example.firebaseuploadimage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Button chooseButton;
    private Button uploadButton;
    private ImageView imageView;
    private ProgressBar progressBar;
    private TextView showUploadsText;
    private EditText fileNameEdit;

    private Uri imageUri;
    private StorageReference storageRef;
    private DatabaseReference databaseRef;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chooseButton = findViewById(R.id.button_choose_image);
        uploadButton = findViewById(R.id.button_upload);
        imageView = findViewById(R.id.image_view);
        progressBar = findViewById(R.id.progress_bar);
        showUploadsText = findViewById(R.id.text_view_show);
        fileNameEdit = findViewById(R.id.editTextFileName);

        storageRef = FirebaseStorage.getInstance().getReference("uploads");
        databaseRef = FirebaseDatabase.getInstance().getReference("uploads");


        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uploadTask != null && uploadTask.isInProgress()){
                    Toast.makeText(MainActivity.this, "Upload In Progress", Toast.LENGTH_SHORT).show();
                }
                else{
                    uploadFile();
                }
            }
        });

        showUploadsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageActivity();
            }
        });
    }

    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
        //startActivity will passed int requestCode which is PICK_IMAGE to onActivity after the activity end
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null
            && data.getData() != null){
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(imageView);
        }
    }

    private String getFileExtension(Uri uri){ //to get image file extension (jpg/jpeg/png)
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile(){
        if(imageUri != null){
            if(!TextUtils.isEmpty(Objects.requireNonNull(fileNameEdit.getText()).toString().trim())){
                StorageReference fileRef = storageRef.child(System.currentTimeMillis() + "."
                        + getFileExtension(imageUri));
                uploadTask = fileRef.putFile(imageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setProgress(0);
                                    }
                                },1000);
                                Toast.makeText(MainActivity.this,"Upload Successful",Toast.LENGTH_LONG).show();
                                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Upload upload = new Upload(fileNameEdit.getText().toString().trim(), uri.toString());
                                        String uploadId = databaseRef.push().getKey(); //Will create a new child with unique id
                                        assert uploadId != null;
                                        databaseRef.child(uploadId).setValue(upload); //upload image to the unique id
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                double progress = (100.0 * snapshot.getBytesTransferred() /
                                        snapshot.getTotalByteCount());
                                progressBar.setProgress((int)progress);
                            }
                        });
            }else{//when not passing a filename
                Toast.makeText(this,"Please enter a file name", Toast.LENGTH_SHORT).show();
            }
        }else {//when no file selected
            Toast.makeText(this,"No File Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImageActivity(){
        Intent intent = new Intent(this, ImagesActivity.class);
        startActivity(intent);
    }
}