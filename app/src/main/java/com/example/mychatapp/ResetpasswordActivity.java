package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

public class ResetpasswordActivity extends AppCompatActivity {
    MaterialEditText sendemail;
    Button email_btn;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetpassword);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Reset Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sendemail=(MaterialEditText)findViewById(R.id.sendemail);
        email_btn=(Button)findViewById(R.id.resetbutton);
        firebaseAuth=FirebaseAuth.getInstance();

        email_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String txtemail=sendemail.getText().toString();
                        if(txtemail.equals("")){
                            Toast.makeText(ResetpasswordActivity.this, "All Fields are required!", Toast.LENGTH_SHORT).show();
                        }else{
                            firebaseAuth.sendPasswordResetEmail(txtemail).addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(ResetpasswordActivity.this, "Please Check your email", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(ResetpasswordActivity.this,LoginActivity.class));
                                            }else{
                                                String error=task.getException().getMessage();
                                                Toast.makeText(ResetpasswordActivity.this, error, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }
}
