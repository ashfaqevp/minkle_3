package com.example.minkle3.fragments;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
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
        dobPicker();
        clicklistener();
    }


    private void dobPicker()
    {


        dob.addTextChangedListener(new TextWatcher() {

            String ddmmyyyy = "DDMMYYYY";
            Calendar cal = Calendar.getInstance();
            String current = "";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
                    String cleanC = current.replaceAll("[^\\d.]|\\.", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    //Fix for pressing delete next to a forward slash
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8){
                        clean = clean + ddmmyyyy.substring(clean.length());
                    }else{
                        //This part makes sure that when we finish entering numbers
                        //the date is correct, fixing it otherwise
                        int day  = Integer.parseInt(clean.substring(0,2));
                        int mon  = Integer.parseInt(clean.substring(2,4));
                        int year = Integer.parseInt(clean.substring(4,8));

                        mon = mon < 1 ? 1 : mon > 12 ? 12 : mon;
                        cal.set(Calendar.MONTH, mon-1);
                        year = (year<1900)?1900:(year>2100)?2100:year;
                        cal.set(Calendar.YEAR, year);
                        // ^ first set year for the line below to work correctly
                        //with leap years - otherwise, date e.g. 29/02/2012
                        //would be automatically corrected to 28/02/2012

                        day = (day > cal.getActualMaximum(Calendar.DATE))? cal.getActualMaximum(Calendar.DATE):day;
                        clean = String.format("%02d%02d%02d",day, mon, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = sel < 0 ? 0 : sel;
                    current = clean;
                    dob.setText(current);
                    dob.setSelection(sel < current.length() ? sel : current.length());
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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
                String Dob = dob.getText().toString();



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
                if(Dob.isEmpty())
                {

                    return;
                }
                progressBar.setVisibility(View.VISIBLE);


                createAccount(name,email,password,Dob);

            }
        });
    }


    private void createAccount(String name, String email, String password,String Dob) {

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
                            uploadUser(user,name, email,Dob);
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

    private void uploadUser(FirebaseUser user,String name, String email,String Dob) {
        Map<String,Object> map = new HashMap<>();
        map.put("name",name);
        map.put("Dob" ,Dob);
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