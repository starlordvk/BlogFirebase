package xyz.varun.blogfirebase;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Random;

import dmax.dialog.SpotsDialog;

public class Account_Setup extends AppCompatActivity {

    private ImageButton profile_setup_img_btn;
    private EditText setup_name;
    private Button submit_setup_btn;
    private static final int GALLERY_REQUEST=2;
    private Uri imguri;
    private Uri resultUri;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private StorageReference storageReference;
    private SpotsDialog spotsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account__setup);

        profile_setup_img_btn=(ImageButton)findViewById(R.id.profile_img_setup_btn);
        setup_name=(EditText)findViewById(R.id.setup_name);
        submit_setup_btn=(Button)findViewById(R.id.setup_submit_btn);
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        auth=FirebaseAuth.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference().child("profile_images");
        spotsDialog=new SpotsDialog(this,R.style.Custom4);

        profile_setup_img_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST);
            }
        });

        submit_setup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishsetup();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && requestCode == RESULT_OK) {
            imguri = data.getData();

            CropImage.activity(imguri)
                    .setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                profile_setup_img_btn.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void finishsetup() {
         final String setup_name_val=setup_name.getText().toString();
        final String user_id=auth.getCurrentUser().getUid();
        if(!TextUtils.isEmpty(setup_name_val) && resultUri!=null)
        {

            spotsDialog.show();
            StorageReference filepath=storageReference.child(random());
            filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    String downloadUrl=taskSnapshot.getDownloadUrl().toString();


            databaseReference.child(user_id).child("name").setValue(setup_name_val);
            databaseReference.child(user_id).child("image").setValue(downloadUrl);

                    spotsDialog.dismiss();

                    Intent intent = new Intent(Account_Setup.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
        }



    });
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
