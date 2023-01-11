package com.example.schoolplanner;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class SecureEntry extends AppCompatActivity {

    private EditText loginName, password;
    private Button submitLogin;
    //for db connectivity
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_entry);

        //so editText layout elements do not trigger input keyboard on loading page
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //database connection
        dbHelper = new DBHelper(this);
        //connect layout items to local layout variables
        loginName = findViewById(R.id.editTextUserName);
        password = findViewById(R.id.editTextPassword);

        //listener for submit button
        submitLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = loginName.getText().toString();
                String userPassword = password.getText().toString();
                //check values against database, pass through or error mssg
                //right now the login data is for testing only, admin code for creating users is outside scope of this project

            }
        });
    }
}