package xyz.varun.blogfirebase;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.client.core.Context;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference databaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView=(RecyclerView)findViewById(R.id.main_rec_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setHasFixedSize(true);
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Blog");
        databaseReference.keepSynced(true);
        databaseUsers=FirebaseDatabase.getInstance().getReference().child("Users");
        databaseUsers.keepSynced(true);

        auth=FirebaseAuth.getInstance();
        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()== null)
                {
                    Intent loginintent=new Intent(MainActivity.this,LoginActivity.class);
                    loginintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginintent);
                }


            }
        };

        checkUserExists();
    }

    @Override
    protected void onStart() {
        super.onStart();

        auth.addAuthStateListener(authStateListener);

        checkUserExists();

        FirebaseRecyclerAdapter<Blog,BlogViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(Blog.class,
                R.layout.blogrow,BlogViewHolder.class,databaseReference) {
            @Override
            protected void populateViewHolder(BlogViewHolder viewHolder, Blog model, int position) {
                viewHolder.setTitle(model.getTitle());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setImg(getApplicationContext(),model.getImage());
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }

    private void checkUserExists() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            final String user_id = auth.getCurrentUser().getUid();
            databaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {

                        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
    }






    public static class BlogViewHolder extends RecyclerView.ViewHolder{
            View view;
        public BlogViewHolder(View itemView) {
            super(itemView);
            view=itemView;
        }
        public void setTitle(String title)
        {
            TextView post_title=(TextView)view.findViewById(R.id.post_title);
            post_title.setText(title);
        }
        public void setDescription(String description)
        {
            TextView post_desc=(TextView)view.findViewById(R.id.post_desc);
            post_desc.setText(description);
        }
        public void setImg(final android.content.Context context, final String image)
        {
           final ImageView post_img=(ImageView)view.findViewById(R.id.post_img);
             Picasso.with(context).load(image).into(post_img);
            Picasso.with(context).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(post_img, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(context).load(image).into(post_img);
                }

            });


        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_add){
            startActivity(new Intent(MainActivity.this,PostActivity.class));
        }
        if(item.getItemId()==R.id.logout){
            logout();
        }

        return super.onOptionsItemSelected(item);


    }

    private void logout() {
        auth.signOut();
    }
}
