package chaitu.android.ychat.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BaseActivity extends AppCompatActivity {
    DatabaseReference reference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String myMobile= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        reference= FirebaseDatabase.getInstance().getReference("Users").child(myMobile);
        reference.child("user_availability").setValue(1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.child("user_availability").setValue(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reference.child("user_availability").setValue(1);
    }
}
