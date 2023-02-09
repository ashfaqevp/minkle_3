package com.example.minkle3.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.minkle3.FragmentReplaceActivity;
import com.example.minkle3.R;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;


public class PasswordResetFragment extends Fragment {


    EditText Email;
    Button submitRP;
    FirebaseAuth auth;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_reset, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        clickListener();


    }

    private void clickListener() {
        submitRP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Email.getText().toString();
                if(email.isEmpty()||!((FragmentReplaceActivity)getActivity()).emailValidate(email))
                {
                    Email.setError("Please Enter a Valid Email");
                    return;

                }
                else
                {
                    passwordReset(email);
                }
            }
        });
    }



    private void passwordReset(String email)
    {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(getContext(),"Password Reset Email Sent!",Toast.LENGTH_SHORT).show();
                            ((FragmentReplaceActivity)getActivity()).setFragment(new LoginFragment());
                        }
                        else
                        {
                            String exception = task.getException().toString();
                            Toast.makeText(getContext(),exception, Toast.LENGTH_SHORT).show();


                        }
                    }
                });
    }



    private void init(View view) {

        Email = view.findViewById(R.id.EmailRP);
        submitRP = view.findViewById(R.id.submitRP);
        auth = FirebaseAuth.getInstance();
    }

}