package chaitu.android.ychat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.ferfalk.simplesearchview.SimpleSearchView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import chaitu.android.ychat.R;
import chaitu.android.ychat.adapters.UserAdapter;
import chaitu.android.ychat.listeners.UserListener;
import chaitu.android.ychat.model.User;

public class UserActivity extends BaseActivity implements UserListener {
    SimpleSearchView searchView;
    RecyclerView recyclerView;
    UserAdapter userAdapter;
    List<User> userList;
    LottieAnimationView lottieAnimationView;
    ImageButton back;
    String myMobile="";

    public int userSelectedPosition=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        back=findViewById(R.id.back);
        back.setOnClickListener(view -> finish());

        searchView=findViewById(R.id.searchView);
        recyclerView=findViewById(R.id.userRecycler);
        lottieAnimationView=findViewById(R.id.animationView);
        userList=new ArrayList<>();

        myMobile= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();

        loadUsers();
        userAdapter=new UserAdapter(this,userList,this);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(userAdapter);
        userAdapter.notifyDataSetChanged();
        searchView.setOnQueryTextListener(new SimpleSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(@NonNull String query) {
                searchUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(@NonNull String newText) {
               searchUsers(newText);
                return true;
            }

            @Override
            public boolean onQueryTextCleared() {
                loadUsers();
                return false;
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }
    private void searchUsers(String query) {
        recyclerView.setVisibility(View.VISIBLE);
        lottieAnimationView.setVisibility(View.GONE);
        userList=new ArrayList<>();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    User user=new User();
                    user.setName(ds.child("name").getValue().toString());
                    user.setPhone(ds.child("mobile").getValue().toString());
                    user.setProfilePic(ds.child("profilePic").getValue().toString());
                    if(user.name.trim().toLowerCase().contains(query.toLowerCase())|| user.mobile.trim().toLowerCase().contains(query.toLowerCase()))
                       userList.add(user);
                }
                if(userList.isEmpty()){
                    lottieAnimationView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);

                    lottieAnimationView.playAnimation();
                }


                userAdapter=new UserAdapter(UserActivity.this,userList, UserActivity.this);
                recyclerView.setVisibility(View.VISIBLE);
                lottieAnimationView.setVisibility(View.GONE);
                recyclerView.setAdapter(userAdapter);
                userAdapter.notifyDataSetChanged();
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadUsers() {
        userList=new ArrayList<>();
        recyclerView.setVisibility(View.VISIBLE);
        lottieAnimationView.setVisibility(View.GONE);
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    User user=new User();
                    user.setName(ds.child("name").getValue().toString());
                    user.setPhone(ds.child("mobile").getValue().toString());
                    user.setProfilePic(ds.child("profilePic").getValue().toString());

                    if(myMobile!=null)
                      if(!user.mobile.equals(myMobile))
                        userList.add(user);
                }
                if(userList.isEmpty()){
                    lottieAnimationView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    lottieAnimationView.playAnimation();
                }

                userAdapter=new UserAdapter(UserActivity.this,userList, UserActivity.this);
                recyclerView.setVisibility(View.VISIBLE);
                lottieAnimationView.setVisibility(View.GONE);
                recyclerView.setAdapter(userAdapter);
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public void onBackPressed() {
        if (searchView.onBackPressed()) {
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onUserClicked(View view, User user, int position) {

            userSelectedPosition=position;
            Intent intent=new Intent(getApplicationContext(), ChatActivity.class);
            intent.putExtra("isViewOrUpdate",true);
            intent.putExtra("user",user);
            startActivity(intent);
            finish();


    }



    @Override
    public void onUserLongClicked(View view, User user, int position) {


    }
}