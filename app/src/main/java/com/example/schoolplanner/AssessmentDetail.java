package com.example.schoolplanner;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.util.Random;

public class AssessmentDetail extends AppCompatActivity {

    //for passing values to another activity
    public static final String ASSESSMENT_INFO = "com.example.schoolplanner.ASSESSMENT_INFO";
    String assessmentInfo;

    EditText titleText;
    private RadioButton performanceAssessmentRadio, objectiveAssessmentRadio;
    private Switch sAlert, eAlert;
    private TextView textViewStartDate, textViewEndDate;
    private ImageView imageStartCalendar, imageStartClock,imageEndCalendar, imageEndClock;
    //for holding dates and times for reminders
    private int startDate, startMonth,startYear, startHour, startMinute;
    private int endDate, endMonth,endYear, endHour, endMinute;
    //for date picker
    private int mDate, mMonth, mYear, mHour, mMinute;
    private boolean startDateSet, endDateSet;
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
    int startAlert, endAlert;

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
                nameOfAssessmentSelected = cursor.getString(1);
                assessmentStartDate = cursor.getString(2);
                assessmentEndDate = cursor.getString(3);
                assessmentType = cursor.getString(4);
                startAlert = cursor.getInt(5);
                endAlert = cursor.getInt(6);
            }
        }

        //set name for this activity in title bar using value passed over by previous activity
        titleText = findViewById(R.id.textTitle);
        titleText.setText(nameOfAssessmentSelected);

        //set values in edit texts
        textViewStartDate = findViewById(R.id.assessmentStartDate);
        textViewStartDate.setText(assessmentStartDate);
        imageStartCalendar = findViewById(R.id.assessmentStartCalendar);
        imageStartClock = findViewById(R.id.assessmentStartClock);
        textViewEndDate = findViewById(R.id.assessmentEndDate);
        textViewEndDate.setText(assessmentEndDate);
        imageEndCalendar = findViewById(R.id.assessmentEndCalendar);
        imageEndClock = findViewById(R.id.assessmentEndClock);
        //set radio buttons based on assessment type value from DB
        performanceAssessmentRadio = (RadioButton) findViewById(R.id.assessmentPerformanceRadioButton);
        if(assessmentType.equals("performance")){performanceAssessmentRadio.setChecked(true);}
        else{performanceAssessmentRadio.setChecked(false);}
        objectiveAssessmentRadio = (RadioButton) findViewById(R.id.assessmentObjectiveRadioButton);
        if(assessmentType.equals("objective")){objectiveAssessmentRadio.setChecked(true);}
        else{objectiveAssessmentRadio.setChecked(false);}
        //set value of start alert switch
        sAlert = (Switch) findViewById(R.id.setStartAlert);
        if(startAlert == 0){sAlert.setChecked(false);}
        else{sAlert.setChecked(true);} //it's holding the request ID for the reminder intent
        //set value of end alert switch
        eAlert = (Switch) findViewById(R.id.setEndAlert);
        if(endAlert == 0){eAlert.setChecked(false);}
        else{eAlert.setChecked(true);} //it's holding the request ID for the reminder intent
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        //set assessmentInfo
        assessmentInfo = "Default Alert";

        //set listeners
        //listener for start date
        imageStartCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar Cal = Calendar.getInstance();
                mDate = Cal.get(Calendar.DATE);
                mMonth = Cal.get(Calendar.MONTH);
                mYear = Cal.get(Calendar.YEAR);
                //launch date picker dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(AssessmentDetail.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        textViewStartDate.setText((month+1)+"-"+dayOfMonth+"-"+year); //jan month 0, so must add 1
                        //set values for reminder
                        startDate = dayOfMonth;
                        startMonth = month;
                        startYear = year;
                    }
                }, mYear, mMonth, mDate);
                //set startDateSet to true
                startDateSet=true;
                datePickerDialog.show();
            }
        });
        //listener for start time
        imageStartClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startDateSet) { //date has already been chosen
                    final Calendar Cal = Calendar.getInstance();
                    mHour = Cal.get(Calendar.HOUR_OF_DAY);
                    mMinute = Cal.get(Calendar.MINUTE);
                    // Launch time picker dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(AssessmentDetail.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            //parse date string, this will get rid of old time if it's there
                            String stringToParse = textViewStartDate.getText().toString();
                            String delims = "[ -]+"; //parse on hyphens and spaces
                            String[] tokens = stringToParse.split(delims);
                            //rebuild date string and add date, also fix formatting issue with minutes not showing lead zeros when less than ten
                            if (minute<10){
                                textViewStartDate.setText(tokens[0] + "-" + tokens[1] + "-" + tokens[2] + " " + hourOfDay + ":0" + minute);
                            }
                            else {
                                textViewStartDate.setText(tokens[0] + "-" + tokens[1] + "-" + tokens[2] + " " + hourOfDay + ":" + minute);
                            }
                            startHour = hourOfDay;
                            startMinute = minute;
                        }
                    }, mHour, mMinute, false);
                    //set timeSet to true
                    //timeSet=true;
                    timePickerDialog.show();
                }
                else{
                    Toast.makeText(AssessmentDetail.this, "Please choose the date first.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //listener for end date
        imageEndCalendar.setOnClickListener(new View.OnClickListener() {
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
                        //set values for reminder
                        endDate = dayOfMonth;
                        endMonth = month;
                        endYear = year;
                    }
                }, mYear, mMonth, mDate);
                //set endDateSet to true
                endDateSet=true;
                datePickerDialog.show();
            }
        });
        //listener for end time
        imageEndClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (endDateSet) { //date has already been chosen
                    final Calendar Cal = Calendar.getInstance();
                    mHour = Cal.get(Calendar.HOUR_OF_DAY);
                    mMinute = Cal.get(Calendar.MINUTE);
                    // Launch time picker dialog
                    TimePickerDialog timePickerDialog = new TimePickerDialog(AssessmentDetail.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            //parse date string, this will get rid of old time if it's there
                            String stringToParse = textViewEndDate.getText().toString();
                            String delims = "[ -]+"; //parse on hyphens and spaces
                            String[] tokens = stringToParse.split(delims);
                            //rebuild date string and add date, also fix formatting issue with minutes not showing lead zeros when less than ten
                            if (minute<10){
                                textViewEndDate.setText(tokens[0] + "-" + tokens[1] + "-" + tokens[2] + " " + hourOfDay + ":0" + minute);
                            }
                            else {
                                textViewEndDate.setText(tokens[0] + "-" + tokens[1] + "-" + tokens[2] + " " + hourOfDay + ":" + minute);
                            }
                            endHour = hourOfDay;
                            endMinute = minute;
                        }
                    }, mHour, mMinute, false);
                    //set timeSet to true
                    //timeSet=true;
                    timePickerDialog.show();
                }
                else{
                    Toast.makeText(AssessmentDetail.this, "Please choose the date first.", Toast.LENGTH_SHORT).show();
                }
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
                        //for a start alert
                        Boolean startSwitchState = sAlert.isChecked();
                        int startAlert = 0; //default
                        if(startSwitchState) {
                            //delete previous reminder and set new one anytime switchState is true on save
                            cancelReminder("start");
                            startAlert = setStartReminder(); //set to the returned request ID for that reminder intent, needs to be saved for deletion if wanted
                            }
                        else{} //cancel old reminder here since no longer checked
                        //for an end alert
                        Boolean endSwitchState = eAlert.isChecked();
                        int endAlert = 0; //default
                        if(endSwitchState) {endAlert = setEndReminder();} //set to the returned request ID for that reminder intent, needs to be saved for deletion if wanted
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
                        goHome();
                        return true;
                }
                return false;
            }
        });
    }

    //methods

    private int setStartReminder(){

        //get present time for testing that reminder time is in the future, alarms set for the past will go off immediately
        long presentTime= System.currentTimeMillis();
        //variables for calendar
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(startYear,startMonth,startDate,startHour,startMinute,0);
        long startConvertedToMillis = calendar1.getTimeInMillis();
        //compare present millis to new reminder time
        if(startConvertedToMillis>presentTime){
            //set value of assessmentInfo to be passed as intent extra
            assessmentInfo = "Assessment for "+ titleText.getText().toString()+" has begun.";
            Intent intent = new Intent(AssessmentDetail.this,ReminderBroadcast.class);
            intent.putExtra(ASSESSMENT_INFO, assessmentInfo );
            //random number for request code for intent
            Random r = new Random();
            int randomRequestCode = r.nextInt(10000 - 1);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(AssessmentDetail.this,randomRequestCode,intent,0);
            AlarmManager alarmManager = (AlarmManager) (AssessmentDetail.this).getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, startConvertedToMillis,pendingIntent);
            //for testing
            Toast.makeText(AssessmentDetail.this, Integer.toString(randomRequestCode), Toast.LENGTH_SHORT).show();
            //return the randomRequestCode to store for later deletion of intent
            return randomRequestCode;
        }
        else return 1; //indicates time was in the past, reminder not set
    }
    private int setEndReminder(){

        //variables for calendar
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(endYear,endMonth,endDate,endHour,endMinute,0);
        long startConvertedToMillis = calendar1.getTimeInMillis();
        //set value of assessmentInfo to be passed as intent extra
        assessmentInfo = "Assessment for "+ titleText.getText().toString()+" has ended.";
        Intent intent = new Intent(AssessmentDetail.this,ReminderBroadcast.class);
        intent.putExtra(ASSESSMENT_INFO, assessmentInfo );
        //random number for request code for intent
        Random r = new Random();
        int randomRequestCode = r.nextInt(10000 - 1);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(AssessmentDetail.this,randomRequestCode,intent,0);
        AlarmManager alarmManager = (AlarmManager)(AssessmentDetail.this).getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, startConvertedToMillis,pendingIntent);

        Toast.makeText(AssessmentDetail.this, Integer.toString(randomRequestCode), Toast.LENGTH_SHORT).show();
        //Toast.makeText(AssessmentDetail.this, String.valueOf(startConvertedToMillis), Toast.LENGTH_SHORT).show();

        //return the randomRequestCode to store for later deletion of intent
        return randomRequestCode;
    }


    private void cancelReminder(String reminderType){

        int requestCode = 0; //for holding code stored in startAlert or endAlert
        Cursor cursor = dbHelper.getDataByName(nameOfAssessmentSelected, "AssessmentInfo");
        if (cursor.getCount() == 0) {
            Toast.makeText(AssessmentDetail.this, "Request Code Error.", Toast.LENGTH_SHORT).show();
        }
        else {
            while (cursor.moveToNext()) {
                if (reminderType.equals("start")) {
                    requestCode = cursor.getInt(5); //5 is startAlert
                    //for testing
                    Toast.makeText(AssessmentDetail.this, String.valueOf(requestCode), Toast.LENGTH_SHORT).show();
                }
                if (reminderType.equals("end")) {
                    requestCode = cursor.getInt(6); //6 is endAlert
                }
            }
        }
        //use request code to cancel reminder
        AlarmManager alarmManager = (AlarmManager)(AssessmentDetail.this).getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(AssessmentDetail.this, ReminderBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(AssessmentDetail.this, requestCode, myIntent, 0);
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);
    }

    public void goHome(){
        Intent intent =new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}