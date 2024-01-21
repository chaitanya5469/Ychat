package chaitu.android.ychat.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import chaitu.android.ychat.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);
        final TextInputEditText mobileInput=findViewById(R.id.inputMobile);
        mobileInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().length()>10){
                    mobileInput.setError("Phone number should not be more than 10 digits");
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        final ProgressBar pb=findViewById(R.id.pb);
        final TextView tv=findViewById(R.id.tabMode);
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            Intent intent=new Intent(getApplicationContext(), UpdateProfile.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }


        Button sendBtn=findViewById(R.id.send);
        sendBtn.setOnClickListener(view -> {
            if(mobileInput.getText().toString().trim().isEmpty()){
                Toast.makeText(this, "Please Enter Mobile number", Toast.LENGTH_SHORT).show();
                return;
            }
            pb.setVisibility(View.VISIBLE);
            sendBtn.setVisibility(View.INVISIBLE);
           PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    Toast.makeText(LoginActivity.this, "Completed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    // This callback is invoked in an invalid request for verification is made,
                    // for instance if the the phone number format is not valid.
                    Log.w(TAG, "onVerificationFailed", e);
                    tv.setText(e.toString());
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                        Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    } else if (e instanceof FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                        // reCAPTCHA verification attempted with null Activity
                        Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();

                    }
                    pb.setVisibility(View.GONE);
                   sendBtn.setVisibility(View.VISIBLE);

                    // Show a message and update the UI
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
                    pb.setVisibility(View.GONE);
                    sendBtn.setVisibility(View.VISIBLE);
                    Intent intent=new Intent(LoginActivity.this, VerifyOTPActivity.class);
                    intent.putExtra("mobile",mobileInput.getText().toString().trim());
                    intent.putExtra("verificationId",verificationId);
                    startActivity(intent);
                    finish();
                    // Save verification ID and resending token so we can use them later

                }
            };
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                            .setPhoneNumber("+91"+mobileInput.getText().toString().trim())       // Phone number to verify
                            .setTimeout(120L, TimeUnit.SECONDS) // Timeout and unit
                                          // (optional) Activity for callback binding
                            .setActivity(this)
                            // If no activity is passed, reCAPTCHA verification can not be used.
                            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                            .build();

            PhoneAuthProvider.verifyPhoneNumber(options);

        });

    }
}