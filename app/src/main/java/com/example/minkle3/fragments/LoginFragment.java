package com.example.minkle3.fragments;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_FIRST_USER;
import static android.app.Activity.RESULT_OK;
import static com.example.minkle3.fragments.CreateAccountFragment.VALID_EMAIL_ADDRESS_REGEX;


import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minkle3.FragmentReplaceActivity;
import com.example.minkle3.MainActivity;
import com.example.minkle3.R;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;


public class LoginFragment extends Fragment {


    private static final int RC_SIGN_IN = 1;
    private EditText emailSI,PasswordSI;
    private Button loginBtn,loginGoogleBtn;
    private TextView passwordReset,signUpNow;
    private ProgressBar progressBar;

    private static final String TAG = "GoogleLoginActivity";
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private boolean showOneTapUI = true;
    FirebaseAuth auth;



    public LoginFragment() {
        // Required empty public constructor
    }



    public boolean emailValidate(String passStr)
    {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(passStr);
        return matcher.find();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        auth = FirebaseAuth.getInstance();
        clickListener();


        //Google SigningCode

        oneTapClient = Identity.getSignInClient(getActivity());

        auth = FirebaseAuth.getInstance();


        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.default_web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

        //...


    }

    private void clickListener() {

        // Login Button Functions
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailSI.getText().toString();
                String passWord = PasswordSI.getText().toString();

                if(email.isEmpty() || !emailValidate(email))
                {
                    emailSI.setError("Please Enter a Valid Email");
                    return;
                }
                if(passWord.isEmpty()|| passWord.length()<6)
                {
                    PasswordSI.setError("Please Enter a Valid Password");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                auth.signInWithEmailAndPassword(email,passWord)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful())
                                {
                                    FirebaseUser user = auth.getCurrentUser();
                                    if(!user.isEmailVerified())
                                    {
                                        Toast.makeText(getContext(),"Please Verify Your Email!",Toast.LENGTH_SHORT).show();

                                    }
                                    sendUserToMainActivity();

                                }
                                else
                                {
                                    progressBar.setVisibility(View.GONE);

                                    String exception = task.getException().toString();
                                    Toast.makeText(getContext(),exception, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


            }
        });

        //GoogleLogin Button Function
        loginGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                googleSign();



            }
        });

        //PasswordReset Button Function
        passwordReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((FragmentReplaceActivity)getActivity()).setFragment(new PasswordResetFragment());
            }
        });

        //SignUp Fucntion
        signUpNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((FragmentReplaceActivity)getActivity()).setFragment(new CreateAccountFragment());

            }
        });



    }

    //Like the Name Says Google SignIn Code Don't Mess Unless Needed

    private void googleSign()
    {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener( getActivity(), new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult result) {
                        try {
                            loginResultHandler.launch(new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build());
                        } catch(android.content.ActivityNotFoundException e){
                            e.printStackTrace();
                            Log.e(TAG, "Couldn't start One Tap UI: " + e.getLocalizedMessage());
                        }
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // No saved credentials found. Launch the One Tap sign-up flow, or
                        // do nothing and continue presenting the signed-out UI.
                        Log.d(TAG, e.getLocalizedMessage());
                    }
                });
    }
    private ActivityResultLauncher<IntentSenderRequest> loginResultHandler = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
        // handle intent result here
        if (result.getResultCode() == RESULT_OK) Log.d(TAG, "RESULT_OK.");
        if (result.getResultCode() == RESULT_CANCELED) Log.d(TAG, "RESULT_CANCELED.");
        if (result.getResultCode() == RESULT_FIRST_USER) Log.d(TAG, "RESULT_FIRST_USER.");
        try {
            SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
            String idToken = credential.getGoogleIdToken();
            String username = credential.getId();
            String password = credential.getPassword();
            if (idToken !=  null) {
                // Got an ID token from Google. Use it to authenticate
                // with your backend.

                // When sign in account is not equal to null
                // Initialize auth credential
                AuthCredential authCredential= GoogleAuthProvider
                        .getCredential(idToken
                                ,null);
                Log.d(TAG, "Got ID token.");

                // Check credential
                auth.signInWithCredential(authCredential)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Check condition
                                if(task.isSuccessful())
                                {
                                    updateUi(auth.getCurrentUser(),idToken);

                                }
                                else
                                {
                                    // When task is Unsuccessful

                                }
                            }
                        });
            } else if (password != null) {
                // Got a saved username and password. Use them to authenticate
                // with your backend.
                Log.d(TAG, "Got password.");
            }
        } catch (ApiException e) {
            switch (e.getStatusCode()) {
                case CommonStatusCodes.CANCELED:
                    Log.d(TAG, "One-tap dialog was closed.");
                    // Don't re-prompt the user.
                    showOneTapUI = false;
                    break;
                case CommonStatusCodes.NETWORK_ERROR:
                    Log.d(TAG, "One-tap encountered a network error.");
                    // Try again or just ignore.
                    break;
                default:
                    Log.d(TAG, "Couldn't get credential from result."
                            + e.getLocalizedMessage());
                    break;
            }
        }
    });

    private void updateUi(FirebaseUser user,String idToken) {



        Map<String,Object> map = new HashMap<>();
        map.put("name",auth.getCurrentUser().getDisplayName());
        map.put("email",auth.getCurrentUser().getEmail());
        map.put("profileImage",String.valueOf(auth.getCurrentUser().getPhotoUrl()));
        map.put("uid",user.getUid());
        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            assert getActivity() !=null;
                            progressBar.setVisibility(View.GONE);

                            sendUserToMainActivity();
                        }
                        else
                        {
                            progressBar.setVisibility(View.GONE);

                            String exception = task.getException().toString();
                            Toast.makeText(getContext(),"Error: "+exception, Toast.LENGTH_SHORT).show();

                        }
                    }
                });



    }


    //Password Reset function




    //This Code Send the User Back to Main Activity

    private void sendUserToMainActivity() {
        if(getActivity() == null)
        {
            return;
        }
        startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
        getActivity().finish();
    }

    //Initializer For All the Declared Variables

    private void init(View view)
    {
        emailSI = view.findViewById(R.id.emailSI);
        PasswordSI = view.findViewById(R.id.PasswordSI);
        loginBtn = view.findViewById(R.id.loginBtn);
        loginGoogleBtn = view.findViewById(R.id.loginGoogleBtn);
        passwordReset = view.findViewById(R.id.passwordReset);
        signUpNow = view.findViewById(R.id.signUpNow);
        progressBar = view.findViewById(R.id.progressBar);




    }
}