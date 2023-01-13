package com.example.schoolplanner;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SecureEntry extends AppCompatActivity {

    //for passing values to another activity
    public static final String USER_ID = "com.example.schoolplanner.USER_ID";

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
        submitLogin = findViewById(R.id.loginButton);

        //create a test login--functionality for administratively creating new users is outside the scope of this project
        dbHelper.insertUserData("name", "password");

        //listener for submit button
        submitLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = loginName.getText().toString();
                String userPassword = password.getText().toString();
                Boolean validPassword = false;
                //check values against database, pass through or error mssg
                //right now the login data is for testing only, admin code for creating users is outside scope of this project
                //use assessmentID to get corresponding data
                Cursor cursor = dbHelper.getDataByName(userName, "LoginData");
                if (cursor.getCount() == 0) {
                    Toast.makeText(SecureEntry.this, "Access Denied", Toast.LENGTH_SHORT).show();
                } else {
                    while (cursor.moveToNext()) {
                        if (cursor.getString(2).equals(userPassword)) {
                            validPassword = true;
                            openMainActivity(cursor.getInt(0)); //position 0 in the array is userID
                        }
                    }
                    if (validPassword == false){
                        Toast.makeText(SecureEntry.this, "Access Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    ///////////////////////////////methods///////////////////////////////////
    public void openMainActivity(int userID){
        Intent intent =new Intent(this, MainActivity.class);
        intent.putExtra(USER_ID, userID );  //not used at this time, could support multiple user accounts
        startActivity(intent);
    }
}