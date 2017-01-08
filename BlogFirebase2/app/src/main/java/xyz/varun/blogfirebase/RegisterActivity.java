package xyz.varun.blogfirebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {

    private EditText name;
    private EditText email;
    private EditText password;
    private Button register_btn;
    private FirebaseAuth auth;
    private SpotsDialog spotsDialog;
    private DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        name=(EditText)findViewById(R.id.name_et);
        email=(EditText)findViewById(R.id.email_et);
        password=(EditText)findViewById(R.id.password_et);
        register_btn=(Button)findViewById(R.id.register_btn);
        auth=FirebaseAuth.getInstance();
        spotsDialog=new SpotsDialog(this,R.style.Custom2);
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();

            }
        });
    }

    private void register() {
        final String name_val=name.getText().toString().trim();
        String email_val=email.getText().toString().trim();
        String password_val=password.getText().toString().trim();
        if(!TextUtils.isEmpty(name_val) && !TextUtils.isEmpty(email_val) && !TextUtils.isEmpty(password_val))
        {
            spotsDialog.show();
            auth.createUserWithEmailAndPassword(email_val,password_val).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        String user_id=auth.getCurrentUser().getUid();
                        databaseReference.child(user_id);
                        DatabaseReference current_user_db=databaseReference.child(user_id);
                        current_user_db.child("name").setValue(name_val);
                        spotsDialog.dismiss();
                        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            });


        }

    }
}
