package com.example.minkle3;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.example.minkle3.fragments.CreateAccountFragment;
import com.example.minkle3.fragments.LoginFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FragmentReplaceActivity extends AppCompatActivity {

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private FrameLayout frameLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_replace);

        frameLayout = findViewById(R.id.frameLayout);

        setFragment(new LoginFragment());

    }
    public boolean emailValidate(String passStr)
    {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(passStr);
        return matcher.find();
    }

    public void setFragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);



        if(fragment instanceof CreateAccountFragment)
        {
            fragmentTransaction.addToBackStack(null);

        }
        fragmentTransaction.replace(frameLayout.getId(),fragment);
        fragmentTransaction.commit();

    }
}