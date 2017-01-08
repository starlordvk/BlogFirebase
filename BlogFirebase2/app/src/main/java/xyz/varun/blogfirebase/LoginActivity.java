package xyz.varun.blogfirebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private Button login_btn;
    private Button new_acnt_btn;
    private SignInButton google_login_btn;
    private static final int GOOGLE_SIGN_IN=1;
    private static final String TAG="LoginActivity";
    FirebaseAuth firebaseAuth;
    SpotsDialog spotsDialog;
    DatabaseReference databaseRef;
    private GoogleApiClient mGoogleApiClient;


    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email=(EditText)findViewById(R.id.email_et);
        password=(EditText)findViewById(R.id.password_et);
        login_btn=(Button)findViewById(R.id.login_btn);
        google_login_btn=(SignInButton) findViewById(R.id.google_login);
        new_acnt_btn=(Button)findViewById(R.id.new_account_btn);
        spotsDialog = new SpotsDialog(this,R.style.Custom3);
        firebaseAuth=FirebaseAuth.getInstance();
        databaseRef= FirebaseDatabase.getInstance().getReference("Users");
        databaseRef.keepSynced(true);


        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();

            }
        });


        // Configuring  Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient=new GoogleApiClient.Builder(this).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

            }
        })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();

        google_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            spotsDialog.show();
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                spotsDialog.dismiss();
                Toast.makeText(LoginActivity.this,"Error in Google Login",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            spotsDialog.dismiss();
                            checkUserExists();

                        }
                        // ...

                    }
                });
    }


    private void login() {
        String email_val=email.getText().toString().trim();
        String password_val=password.getText().toString().trim();
        if(!TextUtils.isEmpty(email_val) && !TextUtils.isEmpty(password_val))
        {
            spotsDialog.show();
            firebaseAuth.signInWithEmailAndPassword(email_val,password_val).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        spotsDialog.dismiss();
                        checkUserExists();
                    }
                    else
                    {
                        spotsDialog.dismiss();
                        Toast.makeText(LoginActivity.this,"Error in Login",Toast.LENGTH_LONG).show();
                    }
                }

            });
        }



    }

    private void checkUserExists() {
        if (firebaseAuth.getCurrentUser() != null) {
            final String user_id = firebaseAuth.getCurrentUser().getUid();
            databaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(user_id)) {

                        Intent loginintent = new Intent(LoginActivity.this, MainActivity.class);
                        loginintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginintent);
                    } else {

                        Intent loginintent = new Intent(LoginActivity.this,Account_Setup.class);
                        loginintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginintent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
