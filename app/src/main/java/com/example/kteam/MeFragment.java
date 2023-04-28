package com.example.kteam;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MeFragment extends Fragment {
    private User me;
    private ArrayList<MyTask> tasks;
    private ArrayList<TeamMember> teamMembers;
    private View view;
    private EditText firstName, email, phone;
    private Button delete;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me =new User();
        tasks=new ArrayList<MyTask>();
        teamMembers=new ArrayList<TeamMember>();
        if (getArguments() != null) {
            me = getArguments().getParcelable("user");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_me, container, false);
        //setting the view and the widgets
        mAuth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();
        firstName =view.findViewById(R.id.firstname);
        email =view.findViewById((R.id.email));
        phone =view.findViewById(R.id.phone);
        delete =view.findViewById(R.id.deleteButton);
        firstName.setText(me.getFirstName()+" "+me.getLastName());
        email.setText(me.getEmail());
        phone.setText(me.getPhone());
        //delete the user account
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //alert dialog to confirm deletion
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want delete your account?\nPS: After proceeding you can't undo the delete")
                        .setCancelable(false)
                        .setPositiveButton("Yes, Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mAuth.getCurrentUser().delete();
                                firestore.collection("users")
                                        .document( mAuth.getCurrentUser().getUid())
                                        .delete();
                                Toast.makeText(getContext(), "User removed!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getActivity(), Login.class));
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Dismiss the dialog and continue with the app
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });
        // Inflate the layout for this fragment
        return view;
    }
}