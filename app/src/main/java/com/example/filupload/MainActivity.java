package com.example.filupload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {


    Button selectFile,upLoad;
    TextView  notification;
    Uri pdfUri; //uri is actualURLs that are meant for local storage

    FirebaseStorage storage;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage=FirebaseStorage.getInstance(); //returns an object of firebase storage
        database=FirebaseDatabase.getInstance(); //returns an obj of firebase database

        selectFile=findViewById(R.id.selectFile);
        upLoad=findViewById(R.id.upload);
        notification=findViewById(R.id.notification);

        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )
                {
                    selectPdf();
                }
                else
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            9);
            }
        });

        upLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pdfUri!=null) //the user has selected the file
                uploadFile(pdfUri);
                else
                    Toast.makeText(MainActivity.this,"Select a file",Toast.LENGTH_SHORT).show();
            }
        });
    }

            private void uploadFile(Uri pdfUri) {
                progressDialog=new ProgressDialog(this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setTitle("Uploading file...");
                progressDialog.setProgress(0);
                progressDialog.show();

                final String fileName=System.currentTimeMillis()+"";
                StorageReference reference=storage.getReference(); //returns the root path
                reference.child("Upload").child(fileName).putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String url;
                        url = taskSnapshot.getUploadSessionUri().toString(); //return the url of you iploaded file
                        //store the url real time database

                        DatabaseReference dbreference=database.getReference(); //returns the path to root
                        dbreference.child(fileName).setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    Toast.makeText(MainActivity.this,"File successfuly uploaded",Toast.LENGTH_SHORT).show();
                                }
                                else
                                    Toast.makeText(MainActivity.this,"File not successfuly uploaded",Toast.LENGTH_SHORT).show();
                            }
                        });


                    }

        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"File not successfuly uploaded",Toast.LENGTH_SHORT).show();

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        //track the progress of our upload ..
                        int currentProgress= (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        progressDialog.setProgress(currentProgress);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==9  && grantResults[0]==PackageManager.PERMISSION_GRANTED)
        {
            selectPdf();
        }
        else
            Toast.makeText(MainActivity.this,"please provide permission", Toast.LENGTH_SHORT).show();
    }

    private void selectPdf() {

        //to offer user to select a file using file manager
        //we will be using an Intent

        Intent i = new Intent();
        i.setType("application/pdf");
        i.setAction(Intent.ACTION_GET_CONTENT); //to fetch file
        startActivityForResult(i,86);

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //check whether user has selected a file or not (ex: pdf)

        if(requestCode==86 && requestCode==RESULT_OK && data!=null)
        {
          pdfUri=data.getData(); //return the uri of selected file
            notification.setText("A file is selected : "+data.getData().getLastPathSegment());
        }
        else
        {
            Toast.makeText(MainActivity.this,"Please select a file",Toast.LENGTH_SHORT).show();
        }
    }
}
