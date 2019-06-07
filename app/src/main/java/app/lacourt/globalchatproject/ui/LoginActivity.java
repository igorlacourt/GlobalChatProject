package app.lacourt.globalchatproject.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import app.lacourt.globalchatproject.R;

public class LoginActivity extends AppCompatActivity {
    private EditText countryCode;
    private EditText phoneNumber;
//    private EditText code;
    private Button sendButton;
    private TextView tvWarning;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String verificationId;
    private String dialogInput;
    private String completePhoneNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);

        isUserLoggedIn();

        dialogInput = "";

        countryCode = (EditText) findViewById(R.id.country);
        countryCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!countryCode.getText().toString().startsWith("+"))
                    countryCode.setText("+" + s);

                Selection.setSelection(countryCode.getText(), countryCode.getText().length());
            }
        });

        phoneNumber = (EditText) findViewById(R.id.phone_number);

//        code = (EditText) findViewById(R.id.code);
//        code.setVisibility(View.INVISIBLE);

        sendButton = (Button) findViewById(R.id.phone_number_login_button);

        tvWarning = (TextView) findViewById(R.id.tv_verification_failed);
        tvWarning.setText("Type your phone number.");
        tvWarning.setVisibility(View.VISIBLE);

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Log.d("verificationLog", "onVerificationCompleted: " + phoneAuthCredential.getSmsCode());

                signInWithPhoneAuthCredentail(phoneAuthCredential);


//                if (verificationId != null) {
//                   showInsertCodeDialog();
//
//                } else {
//                    Toast.makeText(LoginActivity.this, "verificationId is null!", Toast.LENGTH_LONG);
//                    Log.d("verificationLog", "verificationId is NULL");
//                }
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                String message = e.getMessage().toLowerCase();
                String errorMessage = "";

                if (message.contains("invalid format.")) {
                    errorMessage = "Invalid phone number format! \n" +
                            "It should be written in the format below: \n\n" +
                            "[+][country code][number including area code]";

                } else if (message.contains("timeout")) {
                    errorMessage = "Time out! \n" +
                            "Please, check your connection and try again.";
                    startPhoneVerification();

                } else if (message.contains("unusual activity")) {
                    errorMessage = "Temporarily blocked for unusual activity.\n" +
                            "You may have tried to login too many times. \n" +
                            "Try again later.";
                } else {
                    errorMessage = "Something is wrong.\n" +
                            "Try again later.";
                }

                Log.d("verificationLog", "onVerificationFailed: " + e.getMessage());
                tvWarning.setText(errorMessage);
                tvWarning.setVisibility(View.VISIBLE);
                verificationId = null;
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                Log.d("verificationLog", "onCodeSent: " + s);

                completePhoneNum = countryCode.getText().toString() + phoneNumber.getText().toString();
                verificationId = s;

                Log.d("verificationLog", "onCodeSent: completePhoneNum = " + completePhoneNum);
                if(completePhoneNum.equals("+16505553434"))
                    showInsertCodeDialog();

                tvWarning.setText(getString(R.string.warn_code_sent));
                tvWarning.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                Log.d("verificationLog", "onCodeAutoRetrievalTimeOut: " + s);
                Log.d("verificationLog", "verificationId = " + verificationId);
                tvWarning.setText(getString(R.string.code_time_out));
                tvWarning.setVisibility(View.VISIBLE);
                verificationId = null;
            }
        };

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });

        phoneNumber.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    send();
                    return true;
                }
                return false;
            }
        });



//        phoneNumber.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
//                    Toast.makeText(LoginActivity.this, "Enter working! :) code: " + code.getText().toString(), Toast.LENGTH_SHORT).show();
////                    send();
//                    return true;
//                }
//                return false;
//            }
//        });
    }

    private void showInsertCodeDialog() {
        LayoutInflater li = LayoutInflater.from(LoginActivity.this);
        final View insertCodeView = li.inflate(R.layout.insert_code, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                LoginActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(insertCodeView);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                EditText input = (EditText) insertCodeView.findViewById(R.id.dialog_code);
                                String inputCode = input.getText().toString();
                                verifyPhoneNumberWithCode(inputCode);
                                input = null;
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void verifyPhoneNumberWithCode(String inputCode) {

        if (!inputCode.isEmpty()) {

            if(inputCode.length() == 6) {
                Log.d("verificationLog", "verifyPhoneNumberWithCode: verificationId = " + verificationId);
                Log.d("verificationLog", "verifyPhoneNumberWithCode: inputCode = " + inputCode);

                try {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, inputCode);
                    signInWithPhoneAuthCredentail(credential);
                } catch (Exception e) {
                    Toast.makeText(this, "Wrong code.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, getString(R.string.warn_insert_code_correctly), Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(LoginActivity.this, getString(R.string.warn_insert_verif_code), Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithPhoneAuthCredentail(PhoneAuthCredential phoneAuthCredential) {
        tvWarning.setText("Verifying code...");

        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user != null) {
                        final DatabaseReference userDb = FirebaseDatabase.getInstance().getReference()
                                .child("user")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                        //Insert user infomation into the database
                        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    Log.d("myverif", "dataSnaphot exists!");
                                    //Insert new user into Real Time Database.
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("phone", user.getPhoneNumber());
                                    userMap.put("name", user.getPhoneNumber());
                                    userDb.updateChildren(userMap);



                                }
                                isUserLoggedIn();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d("myverif", "database call cencelled. Error: " + databaseError.getMessage());
                            }
                        });
                    }
                }
                else {

                    Log.d("myverif", "signInWithPhoneAuthCredentail task NOT successfull: " + task.getException());
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        tvWarning.setText(":/ Invalid code.");
                    }

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == getResources().getInteger(R.integer.VERIFICATION_RESULT))
            finish();
    }

    private void send() {
        Log.d("verificationLog", "setOnClickListener, onClick: Button clicked.");
        Log.d("verificationLog", "setOnClickListener, onClick: verificationId = " + verificationId);

        String phone = phoneNumber.getText().toString();
        String country = countryCode.getText().toString();

        completePhoneNum =  country + phone;

        if (phone.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Insert a phone number.", Toast.LENGTH_LONG).show();

        } else {
            if(completePhoneNum.equals("+16505553434") || completePhoneNum.equals("+16505551234") || completePhoneNum.equals("+16505554321"))
                whiteListPhoneVerification(completePhoneNum);
            else
                startPhoneVerification();

        }
    }

    private void whiteListPhoneVerification(String phone) {
        String smsCode = "123456";

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseAuthSettings firebaseAuthSettings = firebaseAuth.getFirebaseAuthSettings();

// Configure faking the auto-retrieval with the whitelisted numbers.
        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phone, smsCode);

        PhoneAuthProvider phoneAuthProvider = PhoneAuthProvider.getInstance();
        phoneAuthProvider.verifyPhoneNumber(
                phone,
                60,
                TimeUnit.SECONDS,
                this, /* activity */
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredentail(phoneAuthCredential);
                        Log.d("verificationLog", "whiteListPhoneVerification: Completed");
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Log.d("verificationLog", "whiteListPhoneVerification: Failed");
                    }
                });
    }

    private void startPhoneVerification() {
        tvWarning.setText("Verifying code");
        String country = countryCode.getText().toString();
        String phone = phoneNumber.getText().toString();

        completePhoneNum = country + phone;

        Log.d("completephone", "country + phone = " + completePhoneNum);

        if (completePhoneNum != null) {
            tvWarning.setText("Validating...");
            tvWarning.setVisibility(View.VISIBLE);

            Log.d("verificationLog", "startPhoneVerification, phoneNum = " + completePhoneNum);

            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    completePhoneNum,// Phone number to verify
                    30,// Timeout duration
                    TimeUnit.SECONDS,// Unit of timeout
                    this,// Activity (for callback binding)
                    callbacks// OnVerificationStateChangedCallbacks
            );

        } else {
            Log.d("verificationLog", "startPhoneVerification, phoneNum = " + completePhoneNum);
            Toast.makeText(this, getString(R.string.warn_insert_phone_number), Toast.LENGTH_SHORT).show();
        }

    }

    private void isUserLoggedIn() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainScreenActivity.class));
            finish();
            return;
        }
    }

    public void onExit(View view) {
        finish();
    }

}
