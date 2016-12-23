package xyz.varun.blogfirebase;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.Random;

import dmax.dialog.SpotsDialog;

public class PostActivity extends AppCompatActivity {

    private ImageButton add_img_btn;
    private EditText add_title;
    private EditText add_desc;
    private Button post_btn;
    private Uri imguri;
    private static int GALLERY_REQUEST =2;
    private StorageReference storage;
    private SpotsDialog spotsDialog;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        add_img_btn=(ImageButton) findViewById(R.id.add_img_btn);
        add_title=(EditText)findViewById(R.id.add_title);
        add_desc=(EditText)findViewById(R.id.add_description);
        post_btn=(Button)findViewById(R.id.post_btn);
       spotsDialog=new SpotsDialog(this,R.style.Custom);





        storage= FirebaseStorage.getInstance().getReference();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Blog");

        add_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);

            }
        });

        post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });
    }

    private void startPosting() {

       //dialog.setMessage("Posting");
        //dialog.show();

        final String title_val=add_title.getText().toString().trim();
        final String desc_val=add_desc.getText().toString().trim();

        if(!TextUtils.isEmpty(title_val)&&!TextUtils.isEmpty(desc_val) && imguri!=null)
        {
            spotsDialog.show();
                StorageReference filepath=storage.child("images").child(random());
                filepath.putFile(imguri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Uri downloadUrl=taskSnapshot.getDownloadUrl();
                        DatabaseReference newPost=databaseReference.push();
                        newPost.child("Title").setValue(title_val);
                        newPost.child("Description").setValue(desc_val);
                        newPost.child("Image").setValue(downloadUrl.toString());

                        spotsDialog.dismiss();
                        startActivity(new Intent(PostActivity.this,MainActivity.class));
                    }
                });
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_REQUEST && resultCode==RESULT_OK) {
             imguri = data.getData();

            CropImage.activity(imguri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                add_img_btn.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }




        }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(12);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) +26 );
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
    }

