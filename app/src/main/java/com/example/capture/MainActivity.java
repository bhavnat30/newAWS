package com.example.capture;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Button btnChoose;
    private Button btnUpload;
    private Button btnDownload;
    FirebaseStorage storage;
    StorageReference storageReference;
    private ImageView imageView;
    private Uri filePath;
    private static final int GALLERY_INTENT = 2;
    private static final int CAMERA_REQUEST = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        storage=FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        btnChoose = (Button) findViewById(R.id.btnChoose);
        btnUpload=(Button)findViewById(R.id.btnUpload);
        btnDownload=(Button)findViewById(R.id.btnDownload);
        imageView=(ImageView)findViewById(R.id.imgView);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadImage();
            }
        });
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               chooseImage();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   uploadImage();
            }
        });
    }


    private void uploadImage(){
           if(filePath!=null)
           {
               final ProgressDialog progressDialog=new ProgressDialog(this);
               progressDialog.setTitle("Uploading...");
               progressDialog.show();

               StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());

               ref.putFile(filePath)
                       .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                           @Override
                           public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                               progressDialog.dismiss();
                               Toast.makeText(MainActivity.this,"Uploaded", Toast.LENGTH_SHORT).show();
                           }
                       }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       progressDialog.dismiss();
                       Toast.makeText(MainActivity.this,"Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                   }
               }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                       double progress=(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                       progressDialog.setMessage("Uploading"+(int)progress+"%");

                   }
               });
           }




    }
    private void downloadImage(){
        String urlImage="gs://bookthecar-d67de.appspot.com/images/3c9db418-3228-49c6-86c0-c3a06efd9433";
        Glide.with(MainActivity.this)
                .load(urlImage)
                .into(imageView);

    }

    @TargetApi(22)
    private void chooseImage() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),GALLERY_INTENT);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
          if(requestCode==CAMERA_REQUEST)
          {
              filePath=Uri.parse("content://com.android.providers.media.documents/document");


              Bitmap bitmap=(Bitmap)data.getExtras().get("data");
              imageView.setImageBitmap(bitmap);
          }
        if(requestCode== GALLERY_INTENT && resultCode==RESULT_OK
        && data!= null && data.getData()!=null)
        {
            filePath=data.getData();
            Log.i("FilePath!!!!",filePath.toString());
            try{
                Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }


      }

    public void OpenCamera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CAMERA_REQUEST);
    }



    }



