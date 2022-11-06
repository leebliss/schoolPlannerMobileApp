package com.example.schoolplanner;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.Calendar;

public class AssessmentDetail extends AppCompatActivity {

    EditText titleText;
    private RadioButton performanceAssessmentRadio, objectiveAssessmentRadio;
    private Switch sAlert, eAlert;
    private TextView textViewStartDate, textViewEndDate;
    //for date picker
    private int mDate, mMonth, mYear;
    //TextView displayTermsText;
    //Button save;
    //for bottom navigation menu
    private BottomNavigationView bottomNavigationView;
    //for db connectivity
    DBHelper dbHelper;

    ArrayAdapter adapter;
    //for holding index of selected item
    int currentlySelectedItem;
    //for parsing name of selected item
    String nameOfSelectedItem;
    String assessmentNameOnly;
    //for holding assessmentID sent over in the intent
    int assessmentID;
    String nameOfAssessmentSelected; //this is the assessment featured in this detail
    String assessmentStartDate;
    String assessmentEndDate;
    String assessmentType;
    String startAlert;
    String endAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_detail);

        //so editText layout elements do not trigger input keyboard on loading page
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //database connection
        dbHelper = new DBHelper(this);
        assessmentNameOnly = "";

        //get course ID from the intent passed over from previous activity
        Intent intent = getIntent();
        assessmentID = intent.getIntExtra(CourseDetail.ASSESSMENT_ID, 1000);

        //use assessmentID to get corresponding data
        Cursor cursor = dbHelper.getDataByID(assessmentID, "AssessmentInfo");
        if (cursor.getCount() == 0) {
            Toast.makeText(AssessmentDetail.this, "No matches found", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                nameOfAssessmentSelected = cursor.getString(1); //this is the course featured in this term detail
                assessmentStartDate = cursor.getString(2);
                assessmentEndDate = cursor.getString(3);
                assessmentType = cursor.getString(4);
                startAlert = cursor.getString(5);
                endAlert = cursor.getString(6);
            }
        }

        //set name for this activity in title bar using value passed over by previous activity
        titleText = findViewById(R.id.textTitle);
        titleText.setText(nameOfAssessmentSelected);

        //set values in edit texts
        textViewStartDate = findViewById(R.id.assessmentStartDate);
        //String startHolder = "Start Date: "+courseStartDate;
        textViewStartDate.setText(assessmentStartDate);

        textViewEndDate = findViewById(R.id.assessmentEndDate);
        //String endHolder = "End Date: "+courseEndDate;
        textViewEndDate.setText(assessmentEndDate);
        //set radio buttons based on assessment type value from DB
        performanceAssessmentRadio = (RadioButton) findViewById(R.id.assessmentPerformanceRadioButton);
        if(assessmentType.equals("performance")){performanceAssessmentRadio.setChecked(true);}
        else{performanceAssessmentRadio.setChecked(false);}
        objectiveAssessmentRadio = (RadioButton) findViewById(R.id.assessmentObjectiveRadioButton);
        if(assessmentType.equals("objective")){objectiveAssessmentRadio.setChecked(true);}
        else{objectiveAssessmentRadio.setChecked(false);}
        //set value of start alert switch
        sAlert = (Switch) findViewById(R.id.setStartAlert);
        if(startAlert.equals("true")){sAlert.setChecked(true);}
        else{sAlert.setChecked(false);}
        //set value of end alert switch
        eAlert = (Switch) findViewById(R.id.setEndAlert);
        if(endAlert.equals("true")){eAlert.setChecked(true);}
        else{eAlert.setChecked(false);}
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        //buttons
        //save = findViewById(R.id.btnSave);

        //set listeners
        //listener for start date
        textViewStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar Cal = Calendar.getInstance();
                mDate = Cal.get(Calendar.DATE);
                mMonth = Cal.get(Calendar.MONTH);
                mYear = Cal.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AssessmentDetail.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        textViewStartDate.setText((month+1)+"-"+dayOfMonth+"-"+year);
                    }
                }, mYear, mMonth, mDate);
                datePickerDialog.show();
            }
        });
        //listener for end date
        textViewEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar Cal = Calendar.getInstance();
                mDate = Cal.get(Calendar.DATE);
                mMonth = Cal.get(Calendar.MONTH);
                mYear = Cal.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(AssessmentDetail.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        textViewEndDate.setText((month+1)+"-"+dayOfMonth+"-"+year);
                    }
                }, mYear, mMonth, mDate);
                datePickerDialog.show();
            }
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(MenuItem item){
                switch (item.getItemId()){
                    case R.id.save:
                        //get values to save changes
                        String assessmentName = titleText.getText().toString();
                        String assessmentStart = textViewStartDate.getText().toString();
                        String assessmentEnd = textViewEndDate.getText().toString();
                        //change boolean radiobutton values to strings for storage
                        //for the performance assessment radio
                        Boolean performanceRadioState = performanceAssessmentRadio.isChecked();
                        String assessmentType = ""; //default
                        if(performanceRadioState) {assessmentType = "performance";}
                        //for the objective assessment radio
                        Boolean objectiveRadioState = objectiveAssessmentRadio.isChecked();
                        if(objectiveRadioState) {assessmentType = "objective";}
                        //change boolean switch values to strings for storage
                        //for a start alert
                        Boolean switchState = sAlert.isChecked();
                        String startAlert = "false"; //default
                        if(switchState) {startAlert = "true";}
                        //for an end alert
                        switchState = eAlert.isChecked();
                        String endAlert = "false"; //default
                        if(switchState) {endAlert = "true";}
                        //save the changes
                        Boolean checkSavedChanges = dbHelper.updateAssessmentData(assessmentID,assessmentName,assessmentStart,assessmentEnd,assessmentType,startAlert,endAlert);
                        //test for success
                        if(checkSavedChanges) {
                            Toast.makeText(AssessmentDetail.this, "Changes saved.", Toast.LENGTH_SHORT).show();
                            //refreshList();
                        }
                        else{
                            Toast.makeText(AssessmentDetail.this, "Error, changes not saved.", Toast.LENGTH_SHORT).show();
                        }
                        return true;

                    case R.id.home:
                        //code to return home coming soon
                        return true;
                }
                return false;
            }
        });

        /*
        //this is for saving a detail screen when done with changes
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //get values to save changes
                String assessmentName = titleText.getText().toString();
                String assessmentStart = textViewStartDate.getText().toString();
                String assessmentEnd = textViewEndDate.getText().toString();
                //change boolean radiobutton values to strings for storage
                //for the performance assessment radio
                Boolean performanceRadioState = performanceAssessmentRadio.isChecked();
                String assessmentType = ""; //default
                if(performanceRadioState) {assessmentType = "performance";}
                //for the objective assessment radio
                Boolean objectiveRadioState = objectiveAssessmentRadio.isChecked();
                if(objectiveRadioState) {assessmentType = "objective";}
                //change boolean switch values to strings for storage
                //for a start alert
                Boolean switchState = sAlert.isChecked();
                String startAlert = "false"; //default
                if(switchState) {startAlert = "true";}
                //for an end alert
                switchState = eAlert.isChecked();
                String endAlert = "false"; //default
                if(switchState) {endAlert = "true";}
                //save the changes
                Boolean checkSavedChanges = dbHelper.updateAssessmentData(assessmentID,assessmentName,assessmentStart,assessmentEnd,assessmentType,startAlert,endAlert);
                //test for success
                if(checkSavedChanges) {
                    Toast.makeText(AssessmentDetail.this, "Changes saved.", Toast.LENGTH_SHORT).show();
                    //refreshList();
                }
                else{
                    Toast.makeText(AssessmentDetail.this, "Error, changes not saved.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        */
    }
}