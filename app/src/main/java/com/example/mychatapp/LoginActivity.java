package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

public class LoginActivity extends AppCompatActivity {
    MaterialEditText email,password;
    Button logbutt;
    FirebaseAuth auth;
    TextView forgetpassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email=(MaterialEditText)findViewById(R.id.email);
        password=(MaterialEditText)findViewById(R.id.password);
        logbutt=(Button)findViewById(R.id.loginbutton);
        forgetpassword=(TextView)findViewById(R.id.forgetpassord);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        auth=FirebaseAuth.getInstance();
        forgetpassword.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(LoginActivity.this,ResetpasswordActivity.class));
                    }
                }
        );
        logbutt.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String txtemail=email.getText().toString();
                        String txtpass=password.getText().toString();
                        if(TextUtils.isEmpty(txtemail)||TextUtils.isEmpty(txtpass)){
                            Toast.makeText(LoginActivity.this,"All fields required",Toast.LENGTH_SHORT).show();
                        }else {
                            auth.signInWithEmailAndPassword(txtemail,txtpass).addOnCompleteListener(
                                    new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if(task.isSuccessful()){
                                                Intent intent= new Intent(LoginActivity.this,Main2Activity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                            else{
                                                Toast.makeText(LoginActivity.this,"Authentication Failed!",Toast.LENGTH_SHORT).show();
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
