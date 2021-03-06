package com.example.mwils.april;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class CreatePhysio extends AppCompatActivity {

    //initialising the variables for the items in the activity
    private Button btnCreateUser; EditText email; EditText password; EditText confirm;
    private CheckBox admin, physio;

    //initialising a variable for firebase auth
    private FirebaseAuth auth;

    //initialising a variable for firestore database
    private FirebaseFirestore db;


    //initialising a variable for Firestore Collection reference
    private CollectionReference colref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_physio);

        setUpFields();
        setUpOnClick();
    }//end onCreate

    /**
     * This nethod assigns the EditText elements on the page to variables
     * to be used in the creation of a new user account. It also assigns a variable
     * to the actual create user button to be used in the onClick event.
     */
    private void setUpFields(){
        //Assigning the variables to the text fields in the activity
        email = findViewById(R.id.etNewEmail);
        password = findViewById(R.id.etPassword);
        confirm = findViewById(R.id.etPasswordConfirm);
        admin = findViewById(R.id.cbAdmin);
        physio = findViewById(R.id.cbPhysio);

        //Assigning btnCreateUser to the button in the activity
        btnCreateUser = findViewById(R.id.btnConfirmCreate);
    }//end setUpFields

    /**
     * This method sets up the onClick event for the Create User button
     * This button only calls the createUser() method
     */
    private void setUpOnClick(){
        //Setting onClickListener to create the new user
        btnCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser();
            }//end onClick method
        });//end onClickListener method

        //Setting Change Listeners to the two checked boxes so they both can't be checked at the same time
        admin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (admin.isChecked()){
                    physio.setChecked(false);
                }//end if statement
            }//end onCheckedChanged
        });//end onCheckedChangeListener

        physio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (physio.isChecked()){
                    physio.setChecked(false);
                }//end if statement
            }//end onCheckedChanged
        });//end onCheckecdChangeListener
    }//end setUpOnClick

    /**
     * This method checks that all the fields have been entered
     * It'll also check that the password and the confirmation are the same
     */
    private Boolean validate(){
        boolean result = false;

        //Retrieves the string values of the text entered
        //into each field in the activity
        String emailS = email.getText().toString().trim();
        String passwordS = password.getText().toString();
        String confirmS = confirm.getText().toString();



        //Checks if any of the fields are empty
        if(emailS.isEmpty() || passwordS.isEmpty() || confirmS.isEmpty()){
            Toast.makeText(this,"All fields are required",Toast.LENGTH_SHORT).show();
        }//end if statement
        //if the fields aren't empty, check the password and the confirmation match
        else if(passwordS.equals(confirmS)){
            result = true;
        }//end nested if statement
        else{
            Toast.makeText(this, "Passwords do not match",Toast.LENGTH_SHORT).show();
        }//end nestes else statement

        return result;
    }//end validate method

    /**
     * This method opens a connnection to the authentication database and creates a new user
     * Once the user has been added, it then takes the User ID and adds that to
     * the User database, while also marking them as an Admin user if the checkbox
     * has been ticked
     */
    private void createUser(){
        //creating an instance of Firebase Auth
        auth = FirebaseAuth.getInstance();

        //creating an instance of Firebase Database
        db = FirebaseFirestore.getInstance();
        //checks that the fields have been populated
        if(validate()){
            //extracting the Strings from the EditText fields
            String newEmail = email.getText().toString().trim();
            String newPass = password.getText().toString();

            //Calls the instance of Firebase auth and creates the user
            auth.createUserWithEmailAndPassword(newEmail,newPass)
                    .addOnCompleteListener(CreatePhysio.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //lets the user know if the creation failed
                            if(!task.isSuccessful()){
                                Toast.makeText(CreatePhysio.this, "User Creation failed: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }//end if statement
                            else {
                                //if the creation was successful, let the user know, and add them to the users database
                                //also resets the fields in case the user wanted to make another user
                                boolean newadmin = admin.isChecked(), newphysio = physio.isChecked();
                                email.setText("");
                                password.setText("");
                                confirm.setText("");
                                admin.setChecked(false);
                                physio.setChecked(false);
                                Toast.makeText(CreatePhysio.this,"User Creation Successful!", Toast.LENGTH_SHORT).show();

                                //Creating a map object to hold the admin toggle for the user
                                Map<String, Object> user = new HashMap<>();
                                user.put("isAdmin", newadmin);
                                user.put("isPhysio", newphysio);
                                String uid = task.getResult().getUser().getEmail();
                                db.collection("Users").document(uid).set(user);
                                //this if statement checks if the user is as a new physio
                                //if they are, it creates a collection based of their email address
                                //to be later used to store the collection of clients that physio has
                                if(physio.isChecked()){
                                    String phEmail = task.getResult().getUser().getEmail();
                                    Map<String, Object> physio = new HashMap<>();
                                    physio.put("Physio Email", phEmail);
                                    //Due to the face that Firebase doesn't allow me to create an empty document
                                    //I'll have to set the physio to be a client of themselves
                                    db.collection(phEmail).document(phEmail).set(physio);
                                }//end if statement
                            }//end else statement
                        }//end onComplete method
                    });//end onCompleteListener
        }//end if statement
    }//end createUser
}//end Class
