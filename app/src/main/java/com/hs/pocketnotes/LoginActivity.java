package com.hs.pocketnotes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthMultiFactorException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import util.JournalApi;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private Button createAcctButton;

    private AutoCompleteTextView emailAddres;
    private EditText password;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private  FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressBar= findViewById(R.id.login_progress);

        getSupportActionBar().setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();

        emailAddres = findViewById(R.id.email);
        password = findViewById(R.id.password);

        loginButton = findViewById(R.id.email_sign_in_button);
        createAcctButton = findViewById(R.id.create_acct_button_login);

        createAcctButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,CreateAccountActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginEmailPasswoerdUser(emailAddres.getText().toString().trim(),password.getText().toString().trim());
                

            }

            private void loginEmailPasswoerdUser(String email, String pwd) {

                progressBar.setVisibility(View.VISIBLE);

                if(!TextUtils.isEmpty(email)
                && !TextUtils.isEmpty(pwd)){

                    firebaseAuth.signInWithEmailAndPassword(email,pwd)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    assert  user != null;
                                    String currentUserId = user.getUid();

                                    collectionReference.whereEqualTo("userId",currentUserId)
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                                                    if(error != null){
                                                        return;
                                                    }
                                                    assert  queryDocumentSnapshots != null;
                                                    if(!queryDocumentSnapshots.isEmpty()){

                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        for(QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                                                            JournalApi journalApi = JournalApi.getInstance();
                                                            journalApi.setUsername(snapshot.getString("username"));
                                                            journalApi.setUserId(snapshot.getString("userId"));

                                                            // Goto ListActivity
                                                            startActivity(new Intent(LoginActivity.this,
                                                                    PostJournalActivity.class));
                                                        }

                                                    }
                                                }
                                            });

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.INVISIBLE);

                                }
                            });

                }else{
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this,
                            "Please enter email and Password",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }
}