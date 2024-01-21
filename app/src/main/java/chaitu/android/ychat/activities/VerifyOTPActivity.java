package chaitu.android.ychat.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import chaitu.android.ychat.R;

public class VerifyOTPActivity extends AppCompatActivity {
    private EditText input1,input2,input3,input4,input5,input6;
    private TextView mobile;
    String mobileNo;
    private Button verifyBtn;
    private ProgressBar pb;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otpactivity);
        mobile=findViewById(R.id.mobile);
        mobileNo=getIntent().getStringExtra("mobile");
        verificationId=getIntent().getStringExtra("verificationId");
        mobile.setText(String.format("+91-%s",mobileNo));


        input1=findViewById(R.id.input1);
        input2=findViewById(R.id.input2);
        input3=findViewById(R.id.input3);
        input4=findViewById(R.id.input4);
        input5=findViewById(R.id.input5);
        input6=findViewById(R.id.input6);
        verifyBtn=findViewById(R.id.verify);
        pb=findViewById(R.id.pb);
        setupInputs();
        verifyBtn.setOnClickListener(view -> {
            if(input1.getText().toString().trim().isEmpty()||
                    input2.getText().toString().trim().isEmpty()||
                    input3.getText().toString().trim().isEmpty()||
                    input4.getText().toString().trim().isEmpty()||
                    input5.getText().toString().trim().isEmpty()||
                    input6.getText().toString().trim().isEmpty()){
                Toast.makeText(this, "Please Enter a valid code", Toast.LENGTH_SHORT).show();
                return;
            }
            String code=input1.getText().toString()+
                    input2.getText().toString()+
                    input3.getText().toString()+
                    input4.getText().toString()+
                    input5.getText().toString()+
                    input6.getText().toString();
            if(verificationId!=null){
                pb.setVisibility(View.VISIBLE);
                verifyBtn.setVisibility(View.INVISIBLE);
                PhoneAuthCredential phoneAuthCredential= PhoneAuthProvider.getCredential(verificationId,code);

                FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(task -> {
                    pb.setVisibility(View.GONE);
                    verifyBtn.setVisibility(View.VISIBLE);
                    if (task.isSuccessful()) {
                        Intent intent=new Intent(getApplicationContext(), UpdateProfile.class);
                        intent.putExtra("mobile",mobileNo);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    }else {
                        Toast.makeText(this, "Invalid verification Code", Toast.LENGTH_SHORT).show();
                    }
                });

            }

        });
        findViewById(R.id.resendOtp).setOnClickListener(view -> {
            PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    // This callback is invoked in an invalid request for verification is made,
                    // for instance if the the phone number format is not valid.
                    Log.w(TAG, "onVerificationFailed", e);

                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                        Toast.makeText(VerifyOTPActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    } else if (e instanceof FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        Toast.makeText(VerifyOTPActivity.this, "Try again tomorrow", Toast.LENGTH_SHORT).show();
                    } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                        // reCAPTCHA verification attempted with null Activity
                        Toast.makeText(VerifyOTPActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

                    }
                    pb.setVisibility(View.GONE);
                    verifyBtn.setVisibility(View.VISIBLE);

                    // Show a message and update the UI
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
                    pb.setVisibility(View.GONE);
                    verifyBtn.setVisibility(View.VISIBLE);

                    // Save verification ID and resending token so we can use them later

                }
            };
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                            .setPhoneNumber("+91"+mobile)       // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            // (optional) Activity for callback binding
                            // If no activity is passed, reCAPTCHA verification can not be used.
                            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                            .build();

            PhoneAuthProvider.verifyPhoneNumber(options);
        });

    }



    private void setupInputs() {
        input1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    input2.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        input2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    input3.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        input3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    input4.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        input4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    input5.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        input5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.toString().trim().isEmpty()){
                    input6.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}