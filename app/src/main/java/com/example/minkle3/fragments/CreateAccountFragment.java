package com.example.minkle3.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.minkle3.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.minkle3.FragmentReplaceActivity;
import com.example.minkle3.MainActivity;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CreateAccountFragment extends Fragment {


    // Regex for email checking
    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static final Pattern VALID_PASSWORD_REGEX =
            Pattern.compile("\\w{6,}",Pattern.CASE_INSENSITIVE);


    private ProgressBar progressBar;
    private EditText nameSP,emailSP,PasswordSP,ConfirmPasswordSP,dob;
    private TextView loginTv;
    private Button signUpBtn;
    private FirebaseAuth auth;

    public CreateAccountFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        clicklistener();
    }

    private void init(View view)
    {
        nameSP = view.findViewById(R.id.nameSP);
        dob = view.findViewById(R.id.dobSP);
        emailSP = view.findViewById(R.id.emailSP);
        PasswordSP = view.findViewById(R.id.PasswordSP);
        ConfirmPasswordSP = view.findViewById(R.id.ConfirmPasswordSP);
        loginTv = view.findViewById(R.id.loginTv);
        signUpBtn = view.findViewById(R.id.signUpBtn);
        progressBar = view.findViewById(R.id.progressBar);
        auth = FirebaseAuth.getInstance();
    }
    public  boolean emailValidate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }
    public boolean passwordValidate(String passStr)
    {
        Matcher matcher = VALID_PASSWORD_REGEX.matcher(passStr);
        return matcher.find();
    }
    private void clicklistener()
    {

        loginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    ((FragmentReplaceActivity) getActivity()).setFragment(new LoginFragment());

            }
        });
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameSP.getText().toString();
                String email = emailSP.getText().toString();
                String password = PasswordSP.getText().toString();
                String confirmPassword = ConfirmPasswordSP.getText().toString();
                String Dob = dob.getText().toString(); // fix this shit

                if(name.isEmpty()||name.equals(" "))
                {
                    nameSP.setError("Name is not Valid");
                    return;
                }
                if(email.isEmpty() || !emailValidate(email))
                {
                    emailSP.setError("Email is not Valid");
                    return;
                }
                if(password.isEmpty()||!passwordValidate(password))
                {
                    PasswordSP.setError("password must be at least 6 characters long");
                    return;
                }
                if( !confirmPassword.equals(password))
                {
                    ConfirmPasswordSP.setError("password doesn't Match");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);


                createAccount(name,email,password);

            }
        });
    }

    private void createAccount(String name, String email, String password) {

        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = auth.getCurrentUser();

                            user.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        Toast.makeText(getContext(),"Email Verification Link is Send ", Toast.LENGTH_SHORT).show();

                                                    }
                                                }
                                            });
                            uploadUser(user,name, email);
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

    private void uploadUser(FirebaseUser user,String name, String email) {
        Map<String,Object> map = new HashMap<>();
        map.put("name",name);
        map.put("email",email);
        map.put("profileImage"," ");
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

                            startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                            getActivity().finish();

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



}