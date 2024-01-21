package chaitu.android.ychat.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import chaitu.android.ychat.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfile extends AppCompatActivity {
    Uri uri;
    String downloadUri="";
    ProgressBar progressBar;
    Button saveBtn;
    String token;
    TextInputEditText nameEt;
    CircleImageView imageView;
    ActivityResultLauncher<PickVisualMediaRequest>launcher=registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri o) {
            if(o==null){
                uri= null;
                Toast.makeText(UpdateProfile.this, "No image selected", Toast.LENGTH_SHORT).show();
            }else{
                uri=o;
                Glide.with(UpdateProfile.this).load(uri).into(imageView);
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        if(getIntent().getBooleanExtra("isUpdate",false)){
            String mobile=getIntent().getStringExtra("mobile");
            loadDetails(mobile);
        }
         imageView=findViewById(R.id.circleImageView);
         nameEt =findViewById(R.id.name);
        saveBtn=findViewById(R.id.save);
        progressBar =findViewById(R.id.pBar);

        saveBtn.setOnClickListener(view -> {
            String name= nameEt.getText().toString().trim();
            if(name.isEmpty()){
                nameEt.setError("Enter a valid name");
            }else if(name.length()<4){
                nameEt.setError("Name must be 4 characters atleast");
            }else{
                progressBar.setVisibility(View.VISIBLE);
                saveBtn.setVisibility(View.GONE);

                if(uri!=null)
                  uploadFile(name,uri);
                else updateDatabase(name,downloadUri);
            }
        });
        imageView.setOnClickListener(view -> launcher.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));

    }

    private void loadDetails(String mobile) {
        FirebaseDatabase.getInstance().getReference("Users").child(mobile).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String dpUrl=  snapshot.child("profilePic").getValue(String.class);
                        String name=  snapshot.child("name").getValue(String.class);
                        nameEt.setText(name);
                        if (!dpUrl.isEmpty()) {
                            try {
                                Glide.with(UpdateProfile.this).load(dpUrl).into(imageView);
                            } catch (Exception e) {
                                Log.d("tag",e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




    }

    private void updateDatabase(String name, String downloadUri) {
        String mobileNo= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Users");
        HashMap<String,String>map=new HashMap<>();
        map.put("mobile",mobileNo);
        map.put("name",name);
        map.put("token",token);
        map.put("profilePic",downloadUri);
        reference.child(mobileNo).setValue(map).addOnSuccessListener(unused -> {
            progressBar.setVisibility(View.GONE);
            saveBtn.setVisibility(View.VISIBLE);
            Intent intent=new Intent(UpdateProfile.this, ConversationActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void uploadFile(String name, Uri uri) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/"+uri.getLastPathSegment());

       storageReference.putFile(uri).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, task.getException().toString(), Toast.LENGTH_SHORT).show();
            }
            // Continue with the task to get the download URL
            return storageReference.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                 downloadUri = task.getResult().toString();
                 updateDatabase(name,downloadUri);
            } else {
                progressBar.setVisibility(View.GONE);
                saveBtn.setVisibility(View.VISIBLE);
                // Handle failures
            }
        });




    }


}