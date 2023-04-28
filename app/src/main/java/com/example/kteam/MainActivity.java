package com.example.kteam;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.kteam.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private User me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        me=getIntent().getParcelableExtra("user");
        //welcome the user
        Toast.makeText(MainActivity.this, "Welcome "+me.getFirstName()+" "+me.getLastName()+"!", Toast.LENGTH_SHORT).show();
        //check fragment based on bottom navigation view
        replaceFragment(new TeamFragment());
        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.myteam:
                    replaceFragment(new TeamFragment());
                    break;
                case R.id.task:
                    replaceFragment(new TaskFragment());
                    break;
                case R.id.history:
                    replaceFragment(new HistoryFragment());
                    break;
                case R.id.reports:
                    replaceFragment(new ReportsFragment());
                    break;
                case R.id.me:
                    replaceFragment(new MeFragment());
                    break;
            }
            return true;
        });

    }
    private void replaceFragment(Fragment fragment) {
        //to send user info to the fragments
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", me);
        fragment.setArguments(bundle);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame_layout, fragment);
        ft.commit();
    }
    @Override
    public void onStart() {
        super.onStart();

    }
    @Override
    public void onBackPressed() {
        //logout on back press with alert dialogue confirmation
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Perform logout operation here
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this, Login.class));
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Dismiss the dialog and continue with the app
                        dialog.dismiss();
                    }
                });
        AlertDialog alrt = bld.create();
        alrt.show();

    }
}

